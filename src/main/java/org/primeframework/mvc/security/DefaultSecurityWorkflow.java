/*
 * Copyright (c) 2015-2023, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.primeframework.mvc.security;

import java.io.IOException;
import java.util.Collection;

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ConstraintOverrideMethodConfiguration;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.annotation.AnonymousAccess;
import org.primeframework.mvc.security.annotation.ConstraintOverride;
import org.primeframework.mvc.security.guice.SecuritySchemeFactory;
import org.primeframework.mvc.util.ReflectionUtils;
import org.primeframework.mvc.workflow.WorkflowChain;

/**
 * Default security workflow that uses the {@link MVCConfiguration} and the {@link Action} annotation to manage the security constraints for actions.
 *
 * @author Brian Pontarelli
 */
public class DefaultSecurityWorkflow implements SecurityWorkflow {
  private final ActionInvocationStore actionInvocationStore;

  private final SecuritySchemeFactory factory;

  @Inject
  public DefaultSecurityWorkflow(ActionInvocationStore actionInvocationStore, SecuritySchemeFactory factory) {
    this.actionInvocationStore = actionInvocationStore;
    this.factory = factory;
  }

  @Override
  public void perform(WorkflowChain workflowChain) throws IOException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    if (actionInvocation == null || actionInvocation.configuration == null) {
      workflowChain.continueWorkflow();
      return;
    }

    Action actionAnnotation = actionInvocation.configuration.annotation;
    if (!actionAnnotation.requiresAuthentication()) {
      workflowChain.continueWorkflow();
      return;
    }

    if (actionInvocation.method.annotations.containsKey(AnonymousAccess.class)) {
      workflowChain.continueWorkflow();
      return;
    }

    String[] constraints = getConstraints(actionInvocation);
    for (String scheme : actionInvocation.configuration.securitySchemes) {
      SecurityScheme securityScheme = factory.build(scheme);
      if (securityScheme == null) {
        throw new PrimeException("You have specified an invalid security scheme named [" + scheme + "]");
      }

      // Catch UnauthenticatedException and continue, allow UnauthorizedException to propagate.
      try {
        securityScheme.handle(constraints);
        workflowChain.continueWorkflow();
        return;
      } catch (UnauthenticatedException ignore) {
        // Continue, to the next security scheme.
      }
    }

    throw new UnauthenticatedException();
  }

  protected String[] getConstraints(ActionInvocation actionInvocation) {
    // Order of operation:
    // 1. Constraint override on an HTTP method handler.
    //    - This overrides all other options for a single method on the action such as
    //      get() or post().
    //    - These values have the same restrictions as the @Action annotation in that
    //      they must be constant strings. So this may cramp your style.
    //
    // 2. Constraint override method.
    //    - This overrides the constraints for one or more HTTP methods per the annotation.
    //    - This could be helpful if you want to dynamically build constraints, or
    //      if your constraints aren't strings. The method can return a Collection of
    //      anything, and we'll simply call toString() to get a string value for the
    //      constraint to pass into the constraint validator.
    //
    // 3. Constraints provided in the @Action annotation.
    //
    // Note there is no fall through, if you use either override in step 1 or 2, this
    // will "override" the constraints, and we won't look at any values defined in the
    // @Action annotation.

    // Validate constraints, if provided on the method, this will override the ones on the @Action
    ConstraintOverride constraint = (ConstraintOverride) actionInvocation.method.annotations.get(ConstraintOverride.class);
    if (constraint != null) {
      return constraint.value();
    }

    // If we have a constraint override method, this will override the ones on the @Action
    ConstraintOverrideMethodConfiguration configuration = actionInvocation.configuration.constraintValidationMethods.get(actionInvocation.method.httpMethod);
    if (configuration != null) {
      Object constraints = ReflectionUtils.invoke(configuration.method, actionInvocation.action);
      return constraints != null
          ? ((Collection<?>) constraints).stream().map(Object::toString).toList().toArray(String[]::new)
          : new String[]{};
    }

    // Else, fall back to the busted ol' plain constraints. I guess they could still be cool.
    return actionInvocation.configuration.annotation.constraints();
  }
}

/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.guice.SecuritySchemeFactory;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * Default security workflow that uses the {@link MVCConfiguration} and the {@link Action} annotation to manage the
 * security constraints for actions.
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
  public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
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

    String scheme = actionAnnotation.scheme();
    SecurityScheme securityScheme = factory.build(scheme);
    if (securityScheme == null) {
      throw new PrimeException("You have specified an invalid security scheme named [" + scheme + "]");
    }

    securityScheme.handle(actionAnnotation.constraints());
    workflowChain.continueWorkflow();
  }
}

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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.config.MVCConfiguration;
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

  private final HttpServletResponse httpServletResponse;

  private final MVCConfiguration mvcConfiguration;

  private SecurityContext securityContext;

  @Inject
  public DefaultSecurityWorkflow(ActionInvocationStore actionInvocationStore, HttpServletResponse httpServletResponse,
                                 MVCConfiguration mvcConfiguration) {
    this.actionInvocationStore = actionInvocationStore;
    this.httpServletResponse = httpServletResponse;
    this.mvcConfiguration = mvcConfiguration;
  }

  @Override
  public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
    if (securityContext == null) {
      workflowChain.continueWorkflow();
      return;
    }

    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    if (actionInvocation != null) {
      Action actionAnnotation = actionInvocation.configuration.annotation;
      if (actionAnnotation.requiresAuthentication()) {
        // Check if user is signed in
        if (!securityContext.isLoggedIn()) {
          String loginURI = mvcConfiguration.loginURI();
          httpServletResponse.sendRedirect(loginURI);
          return;
        }

        // Check roles
        String[] requiredRoles = actionAnnotation.roles();
        if (requiredRoles.length > 0) {
          Set<String> userRoles = securityContext.getCurrentUsersRoles();
          if (Arrays.stream(requiredRoles).noneMatch(userRoles::contains)) {
            throw new NotAuthorizedException();
          }
        }
      }
    }

    workflowChain.continueWorkflow();
  }

  @Inject(optional = true)
  public void setSecurityContext(SecurityContext securityContext) {
    this.securityContext = securityContext;
  }
}

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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
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

  private final HttpServletRequest httpServletRequest;

  private final HttpServletResponse httpServletResponse;

  private final MVCConfiguration mvcConfiguration;

  private SecurityContext securityContext;

  @Inject
  public DefaultSecurityWorkflow(ActionInvocationStore actionInvocationStore, HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse, MVCConfiguration mvcConfiguration) {
    this.actionInvocationStore = actionInvocationStore;
    this.httpServletRequest = httpServletRequest;
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
          saveRequest();

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

  private String makeQueryString(Map<String, String[]> parameters) {
    if (parameters.size() == 0) {
      return "";
    }

    StringBuilder build = new StringBuilder();
    for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
      for (String value : entry.getValue()) {
        if (build.length() > 0) {
          build.append("&");
        }

        try {
          build.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return "?" + build.toString();
  }

  private void saveRequest() {
    Map<String, String[]> requestParameters = null;
    String redirectURI;
    if (httpServletRequest.getMethod().equals("GET")) {
      Map<String, String[]> params = httpServletRequest.getParameterMap();
      redirectURI = httpServletRequest.getRequestURI() + makeQueryString(params);
    } else {
      requestParameters = httpServletRequest.getParameterMap();
      redirectURI = httpServletRequest.getRequestURI();
    }

    // Save the request
    SavedHttpRequest saved = new SavedHttpRequest(redirectURI, requestParameters);
    HttpSession session = httpServletRequest.getSession(true);
    session.setAttribute(SavedHttpRequest.INITIAL_SESSION_KEY, saved);
  }
}

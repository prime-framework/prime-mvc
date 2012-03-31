/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

import org.primeframework.mvc.parameter.DefaultParameterParser;
import org.primeframework.mvc.parameter.InternalParameters;
import org.primeframework.mvc.servlet.ServletTools;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * This class is the default implementation of the ActionMappingWorkflow. During the perform method, this class
 * determines the action that will be invoked based on the URI and put it into the ActionInvocationStore.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionMappingWorkflow implements ActionMappingWorkflow {
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final ActionMapper actionMapper;
  private final ActionInvocationStore actionInvocationStore;

  @Inject
  public DefaultActionMappingWorkflow(HttpServletRequest request, HttpServletResponse response,
                                      ActionInvocationStore actionInvocationStore, ActionMapper actionMapper) {
    this.request = request;
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
    this.actionMapper = actionMapper;
  }

  /**
   * Processes the request URI, loads the action configuration, creates the action and stores the invocation in the
   * request.
   *
   * @param chain The workflow chain.
   * @throws IOException      If the chain throws an exception.
   * @throws ServletException If the chain throws an exception.
   */
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    // First, see if they hit a different button
    String uri = determineURI();
    boolean executeResult = InternalParameters.is(request, InternalParameters.EXECUTE_RESULT);
    ActionInvocation invocation = actionMapper.map(uri, executeResult);

    // This case is a redirect because they URI maps to something new and there isn't an action associated with it. For
    // example, this is how the index handling works.
    if (!invocation.uri().equals(uri) && invocation.action() == null) {
      response.sendRedirect(invocation.uri());
      return;
    }

    // Keep the current action in the store if an exception is thrown so that it can be used from the error workflow
    actionInvocationStore.setCurrent(invocation);
    chain.continueWorkflow();
    actionInvocationStore.removeCurrent();
  }

  @SuppressWarnings("unchecked")
  private String determineURI() {
    String uri = null;
    Set<String> keys = request.getParameterMap().keySet();
    for (String key : keys) {
      if (key.startsWith(DefaultParameterParser.ACTION_PREFIX)) {
        String actionParameterName = key.substring(4);
        String actionParameterValue = request.getParameter(key);
        if (request.getParameter(actionParameterName) != null && actionParameterValue.trim().length() > 0) {
          uri = actionParameterValue;

          // Handle relative URIs
          if (!uri.startsWith("/")) {
            String requestURI = ServletTools.getRequestURI(request);
            int index = requestURI.lastIndexOf("/");
            if (index >= 0) {
              uri = requestURI.substring(0, index) + "/" + uri;
            }
          }
        }
      }
    }

    if (uri == null) {
      uri = ServletTools.getRequestURI(request);
      if (!uri.startsWith("/")) {
        uri = "/" + uri;
      }
    }
    return uri;
  }
}
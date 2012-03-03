/*
 * Copyright (c) 2001-2010, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.servlet.WorkflowChain;

import com.google.inject.Inject;

/**
 * This class implements the ResultInvocationWorkflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultResultInvocationWorkflow implements ResultInvocationWorkflow {
  private final HttpServletResponse response;
  private final ActionInvocationStore actionInvocationStore;
  private final ResultInvocationProvider resultInvocationProvider;
  private final ResultProvider resultProvider;

  @Inject
  public DefaultResultInvocationWorkflow(HttpServletResponse response, ActionInvocationStore actionInvocationStore,
                                         ResultInvocationProvider resultInvocationProvider, ResultProvider resultProvider) {
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
    this.resultInvocationProvider = resultInvocationProvider;
    this.resultProvider = resultProvider;
  }

  /**
   * Performs the action invocation using this process.
   * <p/>
   * <h3>Action-less request</h3>
   * <p/>
   * <ul>
   *   <li>Lookup an action-less result invocation</li>
   *   <li>If it doesn't exist, continue down the chain</li>
   *   <li>If it does exist, call the ResultRegistry to find the Result</li>
   *   <li>Invoke the Result</li>
   * </ul>
   * <p/>
   * <h3>Action request</h3>
   * <p/>
   * <ul>
   *   <li>Lookup an result invocation using the action invocation, action URI and result code from the action</li>
   *   <li>If it doesn't exist, error out</li>
   *   <li>If it does exist, call the ResultRegistry to find the Result</li>
   *   <li>Invoke the Result</li>
   * </ul>
   *
   * @param chain The chain.
   * @throws IOException      If the chain throws an IOException.
   * @throws ServletException If the chain throws a ServletException or if the result can't be found.
   */
  @SuppressWarnings("unchecked")
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    ActionInvocation invocation = actionInvocationStore.getCurrent();
    if (invocation.executeResult()) {
      ResultInvocation resultInvocation;
      if (invocation.action() == null) {
        // Try a default result mapping just for the URI
        resultInvocation = resultInvocationProvider.lookup(invocation);
        if (resultInvocation == null) {
          chain.continueWorkflow();
          return;
        }
      } else {
        String resultCode = invocation.resultCode();
        resultInvocation = resultInvocationProvider.lookup(invocation, resultCode);
        if (resultInvocation == null) {
          response.setStatus(404);
          if (invocation.configuration() != null) {
            throw new ServletException("Missing result for action class [" +
              invocation.configuration().actionClass() + "] URI [" + invocation.actionURI() +
              "] and result code [" + resultCode + "]");
          } else {
            throw new ServletException("Missing result for actionless URI [" + invocation.actionURI() +
              "] and result code [" + resultCode + "]");
          }
        }
      }

      Annotation annotation = resultInvocation.annotation();
      Result result = resultProvider.lookup(annotation.annotationType());
      if (result == null) {
        throw new ServletException("Unmapped result annotationType [" + annotation.getClass() +
          "]. You probably need to define a Result implementation that maps to this annotationType " +
          "and then add that Result implementation to your Guice Module.");
      }

      result.execute(annotation, invocation);
    }
  }
}

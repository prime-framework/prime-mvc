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
import java.io.IOException;
import java.lang.reflect.Method;

import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.primeframework.mvc.util.MethodTools;

import com.google.inject.Inject;

/**
 * This class is the default implementation of the action invocation workflow. It looks up the ActionInvocation using
 * the ActionMappingWorkflow and the invokes the action using reflection.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionInvocationWorkflow implements ActionInvocationWorkflow {
  private final ActionInvocationStore actionInvocationStore;
  private final ResultStore resultStore;
  private final HttpServletRequest request;

  @Inject
  public DefaultActionInvocationWorkflow(ActionInvocationStore actionInvocationStore, ResultStore resultStore,
                                         HttpServletRequest request) {
    this.resultStore = resultStore;
    this.request = request;
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * Performs the action invocation using this process.
   * <p/>
   * <h3>Action-less request</h3>
   * <p/>
   * <ul>
   * <li>Continue down the chain</li>
   * </ul>
   * <p/>
   * <h3>Action request</h3>
   * <p/>
   * <ul>
   * <li>Invoke the action</li>
   * </ul>
   *
   * @param chain The chain.
   * @throws IOException      If the chain throws an IOException.
   * @throws ServletException If the chain throws a ServletException or if the result can't be found.
   */
  @SuppressWarnings("unchecked")
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    ActionInvocation invocation = actionInvocationStore.getCurrent();
    if (invocation.action() != null) {
      if (invocation.executeAction()) {
        String resultCode = execute(invocation, request.getMethod());
        resultStore.set(resultCode);
      }
    }

    chain.continueWorkflow();
  }

  /**
   * Invokes the execute method on the action. This first checks if there is an extension and if there is it looks for
   * a
   * method with the same name. Next, it looks for a method that matches the current method (i.e. get or post) and
   * finally falls back to execute.
   *
   * @param actionInvocation The action invocation.
   * @param httpMethod       The HTTP method used (get or post).
   * @return The result code from the execute method and never null.
   * @throws ServletException If the execute method doesn't exist, has the wrong signature, couldn't be invoked, threw
   *                          an exception or returned null.
   */
  protected String execute(ActionInvocation actionInvocation, String httpMethod) throws ServletException {
    Object action = actionInvocation.action();
    String extension = actionInvocation.extension();
    Method method = null;
    if (extension != null) {
      try {
        method = action.getClass().getMethod(extension);
      } catch (NoSuchMethodException e) {
        // Ignore
      }
    }

    if (method == null) {
      try {
        method = action.getClass().getMethod(httpMethod.toLowerCase());
      } catch (NoSuchMethodException e) {
        // Ignore
      }
    }

    // Handle HEAD requests using a GET
    if (method == null && httpMethod.equals("HEAD")) {
      try {
        method = action.getClass().getMethod("GET");
      } catch (NoSuchMethodException e) {
        // Ignore
      }
    }

    if (method == null) {
      try {
        method = action.getClass().getMethod("execute");
      } catch (NoSuchMethodException e) {
        // Ignore
      }
    }

    if (method == null) {
      throw new ServletException("The action class [" + action.getClass() + "] is missing a " +
        "valid execute method. The class can define a method with the same names as the " +
        "HTTP method (which is currently [" + httpMethod.toLowerCase() + "]) or it can define " +
        "a default method named [execute].");
    }

    verify(method);

    String result = MethodTools.invoke(method, action);
    if (result == null) {
      throw new ServletException("The action class [" + action.getClass() + "] returned " +
        "null for the result code. Execute methods must never return null.");
    }

    return result;
  }

  /**
   * Ensures that the method is a correct execute method.
   *
   * @param method The method.
   * @throws ServletException If the method is invalid.
   */
  protected void verify(Method method) throws ServletException {
    if (method.getReturnType() != String.class || method.getParameterTypes().length != 0) {
      throw new ServletException("The action class [" + method.getDeclaringClass().getClass() +
        "] has defined an execute method named [" + method.getName() + "] that is invalid. " +
        "Execute methods must have zero paramters and return a String like this: " +
        "[public String execute()].");
    }
  }
}

/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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
import java.io.IOException;

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.util.ReflectionUtils;
import org.primeframework.mvc.workflow.WorkflowChain;

/**
 * This class is the default implementation of the action invocation workflow. It looks up the ActionInvocation using
 * the ActionMappingWorkflow and the invokes the action using reflection.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionInvocationWorkflow implements ActionInvocationWorkflow {
  private final ActionInvocationStore actionInvocationStore;

  private final ResultStore resultStore;

  @Inject
  public DefaultActionInvocationWorkflow(ActionInvocationStore actionInvocationStore, ResultStore resultStore) {
    this.resultStore = resultStore;
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * Performs the action invocation using this process.
   * <p>
   * <h3>Action-less request</h3>
   * <p>
   * <ul> <li>Continue down the chain</li> </ul>
   * <p>
   * <h3>Action request</h3>
   * <p>
   * <ul> <li>Invoke the action</li> </ul>
   *
   * @param chain The chain.
   * @throws IOException      If the chain throws an IOException.
   * @throws ServletException If the chain throws a ServletException or if the result can't be found.
   */
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    if (actionInvocation.action != null) {
      String resultCode = execute(actionInvocation);
      resultStore.set(resultCode);
    }

    chain.continueWorkflow();
  }

  /**
   * Invokes the execute method on the action. This first checks if there is an extension and if there is it looks for a
   * method with the same name. Next, it looks for a method that matches the current method (i.e. get or post) and
   * finally falls back to execute.
   *
   * @param actionInvocation The action invocation.
   * @return The result code from the execute method and never null.
   */
  protected String execute(ActionInvocation actionInvocation) {
    Object action = actionInvocation.action;
    String result = ReflectionUtils.invoke(actionInvocation.method.method, action);
    if (result == null) {
      throw new PrimeException("The action class [" + action.getClass() + "] returned " +
          "null for the result code. Execute methods must never return null.");
    }

    return result;
  }
}

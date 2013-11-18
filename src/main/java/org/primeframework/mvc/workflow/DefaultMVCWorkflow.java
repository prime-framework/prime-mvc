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
package org.primeframework.mvc.workflow;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.ActionInvocationWorkflow;
import org.primeframework.mvc.action.ActionMappingWorkflow;
import org.primeframework.mvc.action.result.ResultInvocationWorkflow;
import org.primeframework.mvc.message.MessageWorkflow;
import org.primeframework.mvc.parameter.ParameterWorkflow;
import org.primeframework.mvc.parameter.RequestBodyWorkflow;
import org.primeframework.mvc.parameter.URIParameterWorkflow;
import org.primeframework.mvc.scope.ScopeRetrievalWorkflow;
import org.primeframework.mvc.scope.ScopeStorageWorkflow;
import org.primeframework.mvc.validation.ValidationWorkflow;

import com.google.inject.Inject;
import static java.util.Arrays.*;

/**
 * This class is the main entry point for the Prime MVC. It uses the workflows passed into the constructor in the order
 * they are passed in. It also catches {@link ErrorException} and then processes errors using a error workflow set. The
 * error set consists of the {@link ScopeStorageWorkflow} followed by the {@link ResultInvocationWorkflow}.
 *
 * @author Brian Pontarelli
 */
public class DefaultMVCWorkflow implements MVCWorkflow {
  private final ExceptionHandler exceptionHandler;
  private final List<Workflow> workflows;
  private final ErrorWorkflow errorWorkflow;

  @Inject
  public DefaultMVCWorkflow(RequestBodyWorkflow requestBodyWorkflow,
                            StaticResourceWorkflow staticResourceWorkflow,
                            ActionMappingWorkflow actionMappingWorkflow,
                            ScopeRetrievalWorkflow scopeRetrievalWorkflow,
                            URIParameterWorkflow uriParameterWorkflow,
                            ParameterWorkflow parameterWorkflow,
                            ValidationWorkflow validationWorkflow,
                            ActionInvocationWorkflow actionInvocationWorkflow,
                            ScopeStorageWorkflow scopeStorageWorkflow,
                            MessageWorkflow messageWorkflow,
                            ResultInvocationWorkflow resultInvocationWorkflow,
                            ErrorWorkflow errorWorkflow,
                            ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    this.errorWorkflow = errorWorkflow;
    workflows = asList(requestBodyWorkflow, staticResourceWorkflow, actionMappingWorkflow, scopeRetrievalWorkflow,
      uriParameterWorkflow, parameterWorkflow, validationWorkflow, actionInvocationWorkflow,
      scopeStorageWorkflow, messageWorkflow, resultInvocationWorkflow);
  }

  /**
   * Creates a sub-chain of the MVC workflows and invokes it.
   *
   * @param chain The chain.
   * @throws IOException      If the sub-chain throws an IOException
   * @throws ServletException If the sub-chain throws an ServletException
   */
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    try {
      SubWorkflowChain subChain = new SubWorkflowChain(workflows, chain);
      subChain.continueWorkflow();
    } catch (RuntimeException e) {
      exceptionHandler.handle(e);

      SubWorkflowChain errorChain = new SubWorkflowChain(Arrays.<Workflow>asList(errorWorkflow), chain);
      errorChain.continueWorkflow();
    } catch (Error e) {
      exceptionHandler.handle(e);

      SubWorkflowChain errorChain = new SubWorkflowChain(Arrays.<Workflow>asList(errorWorkflow), chain);
      errorChain.continueWorkflow();
    }
  }
}

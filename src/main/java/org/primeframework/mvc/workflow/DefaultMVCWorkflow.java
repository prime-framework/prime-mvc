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
import java.util.List;

import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.ActionInvocationWorkflow;
import org.primeframework.mvc.action.ActionMappingWorkflow;
import org.primeframework.mvc.action.result.ResultInvocationWorkflow;
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.message.MessageWorkflow;
import org.primeframework.mvc.parameter.ParameterWorkflow;
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
  private final ExceptionTranslator exceptionTranslator;
  private final ResultStore resultStore;
  private final List<Workflow> workflows;
  private final List<Workflow> errorWorkflows;

  @Inject
  public DefaultMVCWorkflow(ResultStore resultStore, ExceptionTranslator exceptionTranslator,
                            RequestBodyWorkflow requestBodyWorkflow,
                            StaticResourceWorkflow staticResourceWorkflow,
                            ActionMappingWorkflow actionMappingWorkflow,
                            ScopeRetrievalWorkflow scopeRetrievalWorkflow,
                            MessageWorkflow messageWorkflow,
                            URIParameterWorkflow uriParameterWorkflow,
                            ParameterWorkflow parameterWorkflow,
                            ValidationWorkflow validationWorkflow,
                            ActionInvocationWorkflow actionInvocationWorkflow,
                            ScopeStorageWorkflow scopeStorageWorkflow,
                            ResultInvocationWorkflow resultInvocationWorflow) {
    this.resultStore = resultStore;
    this.exceptionTranslator = exceptionTranslator;
    workflows = asList(requestBodyWorkflow, staticResourceWorkflow, actionMappingWorkflow, scopeRetrievalWorkflow,
      messageWorkflow, uriParameterWorkflow, parameterWorkflow, validationWorkflow, actionInvocationWorkflow,
      scopeStorageWorkflow, resultInvocationWorflow);

    errorWorkflows = asList(scopeStorageWorkflow, resultInvocationWorflow);
  }

  /**
   * Creates a sub-chain of the MVC workflows and invokes it.
   *
   * @param chain The chain.
   * @throws IOException            If the sub-chain throws an IOException
   * @throws ServletException If the sub-chain throws an ServletException
   */
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    try {
      SubWorkflowChain subChain = new SubWorkflowChain(workflows, chain);
      subChain.continueWorkflow();
    } catch (RuntimeException e) {
      String result = exceptionTranslator.translate(e);
      if (result == null) {
        throw e;
      }

      resultStore.set(result);
      SubWorkflowChain errorChain = new SubWorkflowChain(errorWorkflows, chain);
      errorChain.continueWorkflow();
    }
  }
}

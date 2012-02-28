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
package org.primeframework.mvc.servlet;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import org.primeframework.mvc.action.ActionInvocationWorkflow;
import org.primeframework.mvc.action.ActionMappingWorkflow;
import org.primeframework.mvc.action.ActionPrepareWorkflow;
import org.primeframework.mvc.action.result.ResultInvocationWorkflow;
import org.primeframework.mvc.message.MessageWorkflow;
import org.primeframework.mvc.parameter.ParameterWorkflow;
import org.primeframework.mvc.parameter.URIParameterWorkflow;
import org.primeframework.mvc.scope.ScopeRetrievalWorkflow;
import org.primeframework.mvc.scope.ScopeStorageWorkflow;
import org.primeframework.mvc.validation.ValidationWorkflow;

import com.google.inject.Inject;
import static java.util.Arrays.*;

/**
 * <p> This class is the main entry point for the JCatapult MVC. It creates the default workflow that is used to process
 * requests. </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultMVCWorkflow implements MVCWorkflow {
  private List<Workflow> workflows;

  @Inject
  public DefaultMVCWorkflow(ActionMappingWorkflow actionMappingWorkflow,
                            ScopeRetrievalWorkflow scopeRetrievalWorkflow,
                            MessageWorkflow messageWorkflow,
                            URIParameterWorkflow uriParameterWorkflow,
                            ActionPrepareWorkflow actionPrepareWorkflow,
                            ParameterWorkflow parameterWorkflow,
                            ValidationWorkflow validationWorkflow,
                            ActionInvocationWorkflow actionInvocationWorkflow,
                            ScopeStorageWorkflow scopeStorageWorkflow,
                            ResultInvocationWorkflow resultInvocationWorflow) {
    workflows = asList(actionMappingWorkflow, scopeRetrievalWorkflow, messageWorkflow,
      uriParameterWorkflow, actionPrepareWorkflow, parameterWorkflow, validationWorkflow,
      actionInvocationWorkflow, scopeStorageWorkflow, resultInvocationWorflow);
  }

  /**
   * Creates a sub-chain of the MVC workflows and invokes it.
   *
   * @param chain The chain.
   * @throws java.io.IOException            If the sub-chain throws an IOException
   * @throws javax.servlet.ServletException If the sub-chain throws an ServletException
   */
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    SubWorkflowChain subChain = new SubWorkflowChain(workflows, chain);
    subChain.continueWorkflow();
  }
}

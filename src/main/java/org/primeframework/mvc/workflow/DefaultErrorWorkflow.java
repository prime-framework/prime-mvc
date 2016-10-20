/*
 * Copyright (c) 2012-2016, Inversoft Inc., All Rights Reserved
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

import org.primeframework.mvc.action.result.ResultInvocationWorkflow;
import org.primeframework.mvc.message.MessageWorkflow;
import org.primeframework.mvc.scope.ScopeStorageWorkflow;

import com.google.inject.Inject;
import static java.util.Arrays.asList;

/**
 * Default error workflow. This executes the workflows passed into the constructor in order.
 *
 * @author Brian Pontarelli
 */
public class DefaultErrorWorkflow implements ErrorWorkflow {
  private final List<Workflow> errorWorkflows;

  @Inject
  public DefaultErrorWorkflow(ScopeStorageWorkflow scopeStorageWorkflow, MessageWorkflow messageWorkflow,
                              ResultInvocationWorkflow resultInvocationWorkflow) {
    errorWorkflows = asList(scopeStorageWorkflow, messageWorkflow, resultInvocationWorkflow);
  }

  @Override
  public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
    WorkflowChain chain = new SubWorkflowChain(errorWorkflows, workflowChain);
    chain.continueWorkflow();
  }
}

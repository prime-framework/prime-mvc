/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.util.List;

import com.google.inject.Inject;
import io.fusionauth.http.HTTPMethod;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ActionInvocationWorkflow;
import org.primeframework.mvc.action.ActionMapper;
import org.primeframework.mvc.action.result.ResultInvocationWorkflow;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.scope.ScopeStorageWorkflow;

/**
 * Default implementation of the missing workflow. This invokes the configured action that handles missing requests.
 *
 * @author Brian Pontarelli
 */
public class DefaultMissingWorkflow implements MissingWorkflow {
  private final ActionInvocationStore actionInvocationStore;

  private final ActionMapper actionMapper;

  private final MVCConfiguration configuration;

  private final List<Workflow> workflows;

  @Inject
  public DefaultMissingWorkflow(ActionInvocationStore actionInvocationStore, ActionMapper actionMapper,
                                MVCConfiguration configuration, ActionInvocationWorkflow actionInvocationWorkflow,
                                ScopeStorageWorkflow scopeStorageWorkflow,
                                ResultInvocationWorkflow resultInvocationWorkflow) {
    this.actionInvocationStore = actionInvocationStore;
    this.actionMapper = actionMapper;
    this.configuration = configuration;
    this.workflows = List.of(actionInvocationWorkflow, scopeStorageWorkflow, resultInvocationWorkflow);
  }

  @Override
  public void perform(WorkflowChain workflowChain) throws IOException {
    ActionInvocation missingInvocation = actionMapper.map(HTTPMethod.GET, configuration.missingPath(), true);
    actionInvocationStore.setCurrent(missingInvocation);
    WorkflowChain chain = new SubWorkflowChain(workflows, workflowChain);
    chain.continueWorkflow();
  }
}

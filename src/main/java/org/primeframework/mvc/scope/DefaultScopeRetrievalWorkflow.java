/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.scope;

import javax.servlet.ServletException;
import java.io.IOException;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * This class is the retrieval workflow that loads the scope values and puts them in the action at the start of the
 * request.
 *
 * @author Brian Pontarelli
 */
public class DefaultScopeRetrievalWorkflow implements ScopeRetrievalWorkflow {
  private final ActionInvocationStore actionInvocationStore;

  private final FlashScope flashScope;

  private final ScopeRetriever scopeRetriever;

  @Inject
  public DefaultScopeRetrievalWorkflow(ActionInvocationStore actionInvocationStore, FlashScope flashScope, ScopeRetriever scopeRetriever) {
    this.actionInvocationStore = actionInvocationStore;
    this.flashScope = flashScope;
    this.scopeRetriever = scopeRetriever;
  }

  /**
   * Handles the incoming HTTP request for scope values.
   *
   * @param chain The workflow chain.
   */
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    flashScope.transferFlash();

    // Handle loading scoped members into the action
    loadScopedMembers(actionInvocationStore.getCurrent());

    chain.continueWorkflow();
  }

  /**
   * Loads all of the values into the action from the scopes.
   *
   * @param actionInvocation The action invocation
   */
  @SuppressWarnings("unchecked")
  protected void loadScopedMembers(ActionInvocation actionInvocation) {
    scopeRetriever.setScopedValues(actionInvocation);
  }
}

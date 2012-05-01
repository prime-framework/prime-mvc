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
package org.primeframework.mvc.scope;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.util.ReflectionUtils;
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
  private final ScopeProvider scopeProvider;

  @Inject
  public DefaultScopeRetrievalWorkflow(ActionInvocationStore actionInvocationStore, FlashScope flashScope, ScopeProvider scopeProvider) {
    this.actionInvocationStore = actionInvocationStore;
    this.flashScope = flashScope;
    this.scopeProvider = scopeProvider;
  }

  /**
   * Handles the incoming HTTP request for scope values.
   *
   * @param chain The workflow chain.
   */
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    flashScope.transferFlash();

    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    Object action = actionInvocation.action;

    // Handle loading scoped members into the action
    ActionConfiguration actionConfiguration = actionInvocation.configuration;
    List<ScopeField> scopeFields = (actionConfiguration != null) ? actionConfiguration.scopeFields : null;
    if (action != null && scopeFields != null && scopeFields.size() > 0) {
      loadScopedMembers(action, scopeFields);
    }

    chain.continueWorkflow();
  }

  /**
   * Loads all of the values into the action from the scopes.
   *
   * @param action The action to sets the values from scopes into.
   * @param scopeFields The scope fields.
   */
  @SuppressWarnings("unchecked")
  protected void loadScopedMembers(Object action, List<ScopeField> scopeFields) {
    for (ScopeField scopeField : scopeFields) {
      Scope scope = scopeProvider.lookup(scopeField.annotationType);
      Object value = scope.get(scopeField.field.getName(), scopeField.annotation);
      if (value != null) {
        ReflectionUtils.setField(scopeField.field, action, value);
      }
    }
  }
}

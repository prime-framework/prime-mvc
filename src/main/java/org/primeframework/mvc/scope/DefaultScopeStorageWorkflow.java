/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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
 * This class implements the scope storage workflow that handles scope storage after the request is finished.
 *
 * @author Brian Pontarelli
 */
public class DefaultScopeStorageWorkflow implements ScopeStorageWorkflow {
  private final ActionInvocationStore actionInvocationStore;

  private final ScopeProvider scopeProvider;

  @Inject
  public DefaultScopeStorageWorkflow(ActionInvocationStore actionInvocationStore, ScopeProvider scopeProvider) {
    this.actionInvocationStore = actionInvocationStore;
    this.scopeProvider = scopeProvider;
  }

  /**
   * Handles the incoming HTTP request for scope values.
   *
   * @param chain The workflow chain.
   */
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    Object action = actionInvocation.action;

    // Handle storing scoped members from the action
    ActionConfiguration actionConfiguration = actionInvocation.configuration;
    List<ScopeField> scopeFields = (actionConfiguration != null) ? actionConfiguration.scopeFields : null;
    if (action != null && scopeFields != null && scopeFields.size() > 0) {
      storeScopedMembers(action, scopeFields);
    }

    chain.continueWorkflow();
  }

  /**
   * Stores all of the values from the action from into the scopes.
   *
   * @param action      The action to get the values from.
   * @param scopeFields The scope fields.
   */
  @SuppressWarnings("unchecked")
  protected void storeScopedMembers(Object action, List<ScopeField> scopeFields) {
    for (ScopeField scopeField : scopeFields) {
      Scope scope = scopeProvider.lookup(scopeField.annotationType);
      Object value = ReflectionUtils.getField(scopeField.field, action);
      scope.set(scopeField.field.getName(), value, scopeField.annotation);
    }
  }
}

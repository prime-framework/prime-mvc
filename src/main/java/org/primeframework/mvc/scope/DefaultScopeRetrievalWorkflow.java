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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.scope.annotation.ScopeAnnotation;
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
  private final ExpressionEvaluator expressionEvaluator;
  private final FlashScope flashScope;
  private final ScopeProvider scopeProvider;

  @Inject
  public DefaultScopeRetrievalWorkflow(ActionInvocationStore actionInvocationStore, ExpressionEvaluator expressionEvaluator,
                                       FlashScope flashScope, ScopeProvider scopeProvider) {
    this.actionInvocationStore = actionInvocationStore;
    this.expressionEvaluator = expressionEvaluator;
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
    Object action = actionInvocation.action();

    // Handle loading scoped members into the action
    if (action != null) {
      loadScopedMembers(action);
    }

    chain.continueWorkflow();
  }

  /**
   * Loads all of the values into the action from the scopes.
   *
   * @param action The action to sets the values from scopes into.
   */
  @SuppressWarnings("unchecked")
  protected void loadScopedMembers(Object action) {
    Class<?> klass = action.getClass();
    while (klass != Object.class) {
      Field[] fields = klass.getDeclaredFields();
      for (Field field : fields) {
        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
          Class<? extends Annotation> type = annotation.annotationType();
          String fieldName = field.getName();

          if (type.isAnnotationPresent(ScopeAnnotation.class)) {
            Scope scope = scopeProvider.lookup(type);
            Object value = scope.get(fieldName, annotation);
            if (value != null) {
              expressionEvaluator.setValue(fieldName, action, value);
            }
          }
        }
      }

      klass = klass.getSuperclass();
    }
  }
}

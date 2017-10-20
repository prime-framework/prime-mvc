/*
 * Copyright (c) 2017, Inversoft Inc., All Rights Reserved
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

import java.util.List;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.util.ReflectionUtils;

import com.google.inject.Inject;

/**
 * @author Daniel DeGroff
 */
public class DefaultScopeRetriever implements ScopeRetriever {
  private final ScopeProvider scopeProvider;

  @Inject
  public DefaultScopeRetriever(ScopeProvider scopeProvider) {
    this.scopeProvider = scopeProvider;
  }

  @Override
  public void setScopedValues(ActionInvocation actionInvocation) {
    ActionConfiguration actionConfiguration = actionInvocation.configuration;
    List<ScopeField> scopeFields = (actionConfiguration != null) ? actionConfiguration.scopeFields : null;
    if (actionInvocation.action != null && scopeFields != null && scopeFields.size() > 0) {
      for (ScopeField scopeField : scopeFields) {
        Scope scope = scopeProvider.lookup(scopeField.annotationType);
        Object value = scope.get(scopeField.field.getName(), scopeField.annotation);
        if (value != null) {
          ReflectionUtils.setField(scopeField.field, actionInvocation.action, value);
        }
      }
    }
  }
}

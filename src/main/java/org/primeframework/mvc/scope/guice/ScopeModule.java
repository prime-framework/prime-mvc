/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.scope.guice;

import org.primeframework.mvc.scope.DefaultScopeProvider;
import org.primeframework.mvc.scope.DefaultScopeRetrievalWorkflow;
import org.primeframework.mvc.scope.DefaultScopeStorageWorkflow;
import org.primeframework.mvc.scope.ScopeProvider;
import org.primeframework.mvc.scope.ScopeRetrievalWorkflow;
import org.primeframework.mvc.scope.ScopeStorageWorkflow;

import com.google.inject.AbstractModule;

/**
 * Scope bindings.
 *
 * @author Brian Pontarelli
 */
public class ScopeModule extends AbstractModule {
  @Override
  protected void configure() {
    bindScopeProvider();
    bindScopeRetrievalWorkflow();
    bindScopeStorageWorkflow();
  }

  private void bindScopeProvider() {
    bind(ScopeProvider.class).to(DefaultScopeProvider.class);
  }

  protected void bindScopeRetrievalWorkflow() {
    bind(ScopeRetrievalWorkflow.class).to(DefaultScopeRetrievalWorkflow.class);
  }

  protected void bindScopeStorageWorkflow() {
    bind(ScopeStorageWorkflow.class).to(DefaultScopeStorageWorkflow.class);
  }
}

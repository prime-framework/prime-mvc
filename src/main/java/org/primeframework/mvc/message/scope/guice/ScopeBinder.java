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
package org.primeframework.mvc.message.scope.guice;

import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.message.scope.Scope;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;

/**
 * Binds scopes into Guice.
 *
 * @author Brian Pontarelli
 */
public class ScopeBinder {
  /**
   * Creates a new ScopeBinder.
   *
   * @param binder The Guice binder.
   * @return The ScopeBinder.
   */
  public static ScopeBinder newScopeBinder(Binder binder) {
    return new ScopeBinder(binder);
  }

  private final Binder binder;
  private ScopeBinder(Binder binder) {
    this.binder = binder;
  }

  /**
   * Binds the {@link Scope}. You must use the object returned to complete the binding.
   *
   * @param scopeType The scope type.
   * @return The ScopeTypeBinder used to specify the MessageScope implementation to bind to.
   */
  public ScopeTypeBinder bind(Class<? extends Scope> scopeType) {
    return new ScopeTypeBinder(binder, scopeType);
  }

  /**
   * Scope type binder.
   */
  public class ScopeTypeBinder {
    private final Binder binder;
    private final Class<? extends Scope> scopeType;

    private ScopeTypeBinder(Binder binder, Class<? extends Scope> scopeType) {
      this.binder = binder;
      this.scopeType = scopeType;
    }

    /**
     * Binds to a MessageScope instance.
     *
     * @param scope The MessageScope.
     */
    public void forScope(MessageScope scope) {
      MapBinder<MessageScope, Scope> mapBinder = MapBinder.newMapBinder(binder, MessageScope.class, Scope.class);
      mapBinder.addBinding(scope).to(scopeType);
    }
  }
}

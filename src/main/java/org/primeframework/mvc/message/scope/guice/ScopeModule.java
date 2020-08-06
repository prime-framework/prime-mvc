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

import org.primeframework.mvc.message.scope.ApplicationScope;
import org.primeframework.mvc.message.scope.FlashScope;
import org.primeframework.mvc.message.scope.RequestScope;
import org.primeframework.mvc.message.scope.SessionScope;

import com.google.inject.AbstractModule;

/**
 * Scope module.
 *
 * @author Brian Pontarelli
 */
public class ScopeModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ApplicationScope.class).asEagerSingleton();
    bind(SessionScope.class);
    bind(FlashScope.class);
    bind(RequestScope.class);

    // FlashScope needs to de-serialize messages when using cookies


  }
}

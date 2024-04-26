/*
 * Copyright (c) 2024-2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security;

import java.time.Clock;
import java.util.UUID;

import com.fasterxml.jackson.databind.Module;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.inversoft.json.JacksonModule;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.cors.CORSConfigurationProvider;
import org.primeframework.mvc.cors.NoCORSConfigurationProvider;

public class SessionTestModule extends AbstractModule {
  private final Provider<Clock> clockProvider;

  public SessionTestModule(Provider<Clock> clockProvider) {
    this.clockProvider = clockProvider;
  }

  @Override
  protected void configure() {
    bind(UserIdSessionContextProvider.class).to(MockUserIdSessionContextProvider.class);
    bind(MVCConfiguration.class).to(MockConfiguration.class).asEagerSingleton();
    bind(UserLoginSecurityContext.class).to(MockBaseUserIdCookieSecurityContext.class);
    bind(CORSConfigurationProvider.class).to(NoCORSConfigurationProvider.class).asEagerSingleton();
    bind(Clock.class).toProvider(clockProvider);
    var jacksonMultiBinder = Multibinder.newSetBinder(binder(), Module.class);
    jacksonMultiBinder.addBinding().toInstance(new JacksonModule());
  }
}
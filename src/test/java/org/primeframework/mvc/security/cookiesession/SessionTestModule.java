/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import java.time.Clock;

import com.fasterxml.jackson.databind.Module;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.inversoft.json.JacksonModule;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.cors.CORSConfigurationProvider;
import org.primeframework.mvc.cors.NoCORSConfigurationProvider;
import org.primeframework.mvc.security.UserLoginSecurityContext;

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

/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import java.io.ByteArrayOutputStream;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.primeframework.mvc.PrimeBaseTest.TestContentModule;
import org.primeframework.mvc.PrimeBaseTest.TestMVCConfigurationModule;
import org.primeframework.mvc.guice.MVCModule;
import org.primeframework.mvc.http.DefaultHTTPRequest;
import org.primeframework.mvc.http.DefaultHTTPResponse;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.jwt.MockVerifierProvider;
import org.primeframework.mvc.message.MessageObserver;
import org.primeframework.mvc.message.TestMessageObserver;
import org.primeframework.mvc.message.scope.ApplicationScope;
import org.primeframework.mvc.message.scope.CookieFlashScope;
import org.primeframework.mvc.message.scope.FlashScope;
import org.primeframework.mvc.message.scope.RequestScope;
import org.primeframework.mvc.security.MockOAuthUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.VerifierProvider;
import org.primeframework.mvc.test.RequestSimulator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JWTRefreshTokenLoginTest {
  protected final static TestMessageObserver messageObserver = new TestMessageObserver();

  protected final static MetricRegistry metricRegistry = new MetricRegistry();

  public static Injector injector;

  public DefaultHTTPRequest request;

  public DefaultHTTPResponse response;

  public RequestSimulator simulator;

  @AfterClass
  public void afterClass() {
    simulator.shutdown();
  }

  @AfterMethod
  public void afterMethod() {
    HTTPObjectsHolder.clearRequest();
    HTTPObjectsHolder.clearResponse();
  }

  @BeforeClass
  public void beforeClass() {
    Module mvcModule = new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestMVCConfigurationModule());
        bind(MessageObserver.class).toInstance(messageObserver);
        bind(MetricRegistry.class).toInstance(metricRegistry);
        bind(UserLoginSecurityContext.class).to(MockOAuthUserLoginSecurityContext.class);
      }
    };

    Module module = Modules.override(mvcModule).with(new TestContentModule(), new TestSecurityModule(), new TestScopeModule());
    TestPrimeMain main = new TestPrimeMain(8081, module);
    simulator = new RequestSimulator(main, messageObserver);
    injector = simulator.getInjector();
  }

  /**
   * Sets up the servlet objects and injects the test.
   */
  @BeforeMethod
  public void beforeMethod() {
    request = new DefaultHTTPRequest();
    response = new DefaultHTTPResponse(new ByteArrayOutputStream());
    HTTPObjectsHolder.setRequest(request);
    HTTPObjectsHolder.setResponse(response);

    // Clear the user agent (cookies)
    simulator.userAgent.reset();

    // Clear the message observer
    messageObserver.clear();

    // Clear the roles and logged in user
    MockOAuthUserLoginSecurityContext.Roles.clear();
    MockOAuthUserLoginSecurityContext.CurrentUser = null;

    // Reset the token endpoint
    MockOAuthUserLoginSecurityContext.TokenEndpoint = "http://localhost:8000/oauth/token";
  }

  @Test
  public void login() {
    simulator.test("/oauth/login")
             .post()
             .assertStatusCode(200);

    simulator.test("/oauth/protected-resource")
             .get()
             .assertStatusCode(200)
             .assertBodyContains("Logged in");
  }

  @Test
  public void notLoggedIn() {
    simulator.test("/oauth/protected-resource")
             .get()
             .assertStatusCode(302)
             .assertRedirect("/oauth/login");
  }

  @Test
  public void refreshTokenEndpointDown() {
    simulator.test("/oauth/login")
             .withParameter("expired", "true")
             .post()
             .assertStatusCode(200);

    simulator.test("/oauth/protected-resource")
             .get()
             .assertStatusCode(302)
             .assertRedirect("/oauth/login");
  }

  @Test
  public void refreshTokenEndpointUp() {
    MockOAuthUserLoginSecurityContext.TokenEndpoint = "http://localhost:8081/oauth/token";

    simulator.test("/oauth/login")
             .withParameter("expired", "true")
             .post()
             .assertStatusCode(200);

    simulator.test("/oauth/protected-resource")
             .get()
             .assertStatusCode(200)
             .assertBodyContains("Logged in");
  }

  public static class TestScopeModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(ApplicationScope.class).asEagerSingleton();
      bind(RequestScope.class);
      bind(FlashScope.class).toProvider(() -> injector.getInstance(CookieFlashScope.class));
    }
  }

  public static class TestSecurityModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(VerifierProvider.class).to(MockVerifierProvider.class);
    }
  }
}

/*
 * Copyright (c) 2021-2024, Inversoft Inc., All Rights Reserved
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

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.fusionauth.http.server.HTTPListenerConfiguration;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.http.server.HTTPServerConfiguration;
import org.primeframework.mvc.PrimeBaseTest.TestContentModule;
import org.primeframework.mvc.PrimeBaseTest.TestMVCConfigurationModule;
import org.primeframework.mvc.cors.CORSConfigurationProvider;
import org.primeframework.mvc.cors.NoCORSConfigurationProvider;
import org.primeframework.mvc.guice.MVCModule;
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
import org.primeframework.mvc.security.oauth.TokenAuthenticationMethod;
import org.primeframework.mvc.test.RequestSimulator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Brian Pontarelli
 */
public class JWTRefreshTokenLoginTest {
  protected final static TestMessageObserver messageObserver = new TestMessageObserver();

  protected final static MetricRegistry metricRegistry = new MetricRegistry();

  public static Injector injector;

  public HTTPRequest request;

  public HTTPResponse response;

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
        bind(CORSConfigurationProvider.class).to(NoCORSConfigurationProvider.class);
      }
    };

    Module module = Modules.override(mvcModule).with(new TestContentModule(), new TestSecurityModule(), new TestScopeModule());
    var config = new HTTPServerConfiguration().withListener(new HTTPListenerConfiguration(9081));
    TestPrimeMain main = new TestPrimeMain(new HTTPServerConfiguration[]{config}, module);
    simulator = new RequestSimulator(main, messageObserver);
    injector = simulator.getInjector();
  }

  /**
   * Sets up the servlet objects and injects the test.
   */
  @BeforeMethod
  public void beforeMethod() {
    request = new HTTPRequest();
    response = new HTTPResponse();
    HTTPObjectsHolder.setRequest(request);
    HTTPObjectsHolder.setResponse(response);

    // Clear the user agent (cookies)
    simulator.userAgent.reset();

    // Clear the message observer
    messageObserver.reset();

    // Clear the roles and logged-in user
    MockOAuthUserLoginSecurityContext.Roles.clear();
    MockOAuthUserLoginSecurityContext.CurrentUser = null;

    // Reset the token endpoint.
    MockOAuthUserLoginSecurityContext.reset();
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
  public void login_expired() {
    // The JWT will be validated for expiration when you login, expect an expired JWT to fail immediately.
    simulator.test("/oauth/login")
             .withParameter("expired", "true")
             .post()
             .assertStatusCode(401);
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
    // By default, the Token endpoint in the MockUserLoginSecurityContext is configured to a port that is not listening. So it is 'down'.
    MockOAuthUserLoginSecurityContext.ValidateJWTOnLogin = false;

    // Setting 'expired: true' on the request just tells the Login action to create an expired JWT and store it in the LoginContext.
    // - So we expect this to succeed, but the login context wil now contain an expired JWT. This means it will be refreshed on first use.
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
  public void refreshTokenEndpointUp_auth_client_secret_basic() {
    MockOAuthUserLoginSecurityContext.tokenAuthenticationMethod = TokenAuthenticationMethod.client_secret_basic;
    MockOAuthUserLoginSecurityContext.clientId = "the client ID";
    MockOAuthUserLoginSecurityContext.clientSecret = "the client secret";
    MockOAuthUserLoginSecurityContext.ValidateJWTOnLogin = false;
    MockOAuthUserLoginSecurityContext.TokenEndpoint = "http://localhost:" + simulator.getPort() + "/oauth/token";

    // Setting 'expired: true' on the request just tells the Login action to create an expired JWT and store it in the LoginContext.
    // - So we expect this to succeed, but the login context wil now contain an expired JWT. This means it will be refreshed on first use.
    simulator.test("/oauth/login")
             .withParameter("expired", "true")
             .post()
             .assertStatusCode(200);

    // The JWT in the security context was found to be expired on first use even though login succeeded.
    // - This will have caused us to refresh the token, so this action should succeed.
    simulator.test("/oauth/protected-resource")
             .get()
             .assertStatusCode(200)
             .assertBodyContains("Logged in");
  }

  @Test
  public void refreshTokenEndpointUp_auth_client_secret_basic_colon_in_client_id() {
    MockOAuthUserLoginSecurityContext.tokenAuthenticationMethod = TokenAuthenticationMethod.client_secret_basic;
    MockOAuthUserLoginSecurityContext.clientId = "the:client ID";
    MockOAuthUserLoginSecurityContext.clientSecret = "the client secret";
    MockOAuthUserLoginSecurityContext.ValidateJWTOnLogin = false;
    MockOAuthUserLoginSecurityContext.TokenEndpoint = "http://localhost:" + simulator.getPort() + "/oauth/token";

    // Setting 'expired: true' on the request just tells the Login action to create an expired JWT and store it in the LoginContext.
    // - So we expect this to succeed, but the login context wil now contain an expired JWT. This means it will be refreshed on first use.
    simulator.test("/oauth/login")
             .withParameter("expired", "true")
             .post()
             .assertStatusCode(200);

    // The JWT in the security context was found to be expired on first use even though login succeeded.
    // - This will have caused us to refresh the token, so this action should succeed.
    simulator.test("/oauth/protected-resource")
             .get()
             .assertStatusCode(302)
             .assertRedirect("/oauth/login");
  }

  @Test
  public void refreshTokenEndpointUp_auth_client_secret_post() {
    MockOAuthUserLoginSecurityContext.tokenAuthenticationMethod = TokenAuthenticationMethod.client_secret_post;
    MockOAuthUserLoginSecurityContext.clientId = "the client ID";
    MockOAuthUserLoginSecurityContext.clientSecret = "the client secret";
    MockOAuthUserLoginSecurityContext.ValidateJWTOnLogin = false;
    MockOAuthUserLoginSecurityContext.TokenEndpoint = "http://localhost:" + simulator.getPort() + "/oauth/token";

    // Setting 'expired: true' on the request just tells the Login action to create an expired JWT and store it in the LoginContext.
    // - So we expect this to succeed, but the login context wil now contain an expired JWT. This means it will be refreshed on first use.
    simulator.test("/oauth/login")
             .withParameter("expired", "true")
             .post()
             .assertStatusCode(200);

    // The JWT in the security context was found to be expired on first use even though login succeeded.
    // - This will have caused us to refresh the token, so this action should succeed.
    simulator.test("/oauth/protected-resource")
             .get()
             .assertStatusCode(200)
             .assertBodyContains("Logged in");
  }

  @Test
  public void refreshTokenEndpointUp_no_auth() {
    MockOAuthUserLoginSecurityContext.ValidateJWTOnLogin = false;
    MockOAuthUserLoginSecurityContext.TokenEndpoint = "http://localhost:" + simulator.getPort() + "/oauth/token";

    // Setting 'expired: true' on the request just tells the Login action to create an expired JWT and store it in the LoginContext.
    // - So we expect this to succeed, but the login context wil now contain an expired JWT. This means it will be refreshed on first use.
    simulator.test("/oauth/login")
             .withParameter("expired", "true")
             .post()
             .assertStatusCode(200);

    // The JWT in the security context was found to be expired on first use even though login succeeded.
    // - This will have caused us to refresh the token, so this action should succeed.
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

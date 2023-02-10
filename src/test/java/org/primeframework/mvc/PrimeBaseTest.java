/*
 * Copyright (c) 2012-2022, Inversoft Inc., All Rights Reserved
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.io.NonBlockingByteBufferOutputStream;
import io.fusionauth.http.server.HTTPContext;
import io.fusionauth.http.server.HTTPListenerConfiguration;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.http.server.HTTPServerConfiguration;
import org.example.action.user.EditAction;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.ValidationMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.DefaultActionConfigurationBuilder;
import org.primeframework.mvc.action.guice.ActionModule;
import org.primeframework.mvc.action.result.ResultInvocationFinalizer;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.content.guice.ContentHandlerFactory;
import org.primeframework.mvc.content.guice.ObjectMapperProvider;
import org.primeframework.mvc.content.json.JacksonContentHandler;
import org.primeframework.mvc.cors.CORSConfiguration;
import org.primeframework.mvc.cors.CORSConfigurationProvider;
import org.primeframework.mvc.guice.MVCModule;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.jwt.MockVerifierProvider;
import org.primeframework.mvc.message.MessageObserver;
import org.primeframework.mvc.message.TestMessageObserver;
import org.primeframework.mvc.message.scope.ApplicationScope;
import org.primeframework.mvc.message.scope.CookieFlashScope;
import org.primeframework.mvc.message.scope.FlashScope;
import org.primeframework.mvc.message.scope.RequestScope;
import org.primeframework.mvc.security.CipherProvider;
import org.primeframework.mvc.security.DefaultCipherProvider;
import org.primeframework.mvc.security.MockUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.VerifierProvider;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.test.RequestSimulator;
import org.primeframework.mvc.util.ThrowingRunnable;
import org.primeframework.mvc.validation.Validation;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

/**
 * This class is a base test for testing the Prime framework. It isn't recommended that you use it outside the Prime
 * project.
 *
 * @author Brian Pontarelli and James Humphrey
 */
public abstract class PrimeBaseTest {
  protected final static TestMessageObserver messageObserver = new TestMessageObserver();

  public static MockConfiguration configuration = new MockConfiguration();

  public static HTTPContext context;

  public static CORSConfiguration corsConfiguration;

  public static Injector injector;

  public static MetricRegistry metricRegistry = new MetricRegistry();

  public static RequestSimulator simulator;

  @Inject public CSRFProvider csrfProvider;

  @Inject public ObjectMapper objectMapper;

  public HTTPRequest request;

  public HTTPResponse response;

  @Inject public TestBuilder test;

  @AfterMethod
  public void afterMethod() {
    HTTPObjectsHolder.clearRequest();
    HTTPObjectsHolder.clearResponse();
  }

  /**
   * Sets up the servlet objects and injects the test.
   */
  @BeforeMethod
  public void beforeMethod() {
    request = new HTTPRequest();
    response = new HTTPResponse(new NonBlockingByteBufferOutputStream(null, 1024), request);
    HTTPObjectsHolder.setRequest(request);
    HTTPObjectsHolder.setResponse(response);

    injector.injectMembers(this);

    test.context = context;
    test.simulator = simulator;
    test.userAgent = simulator.userAgent;
    TestObjectMapperProvider.test = test;

    // Clear the user agent (cookies)
    simulator.userAgent.reset();

    // clear the metric registry before each test
    metricRegistry = injector.getInstance(MetricRegistry.class);
    metricRegistry.getNames().forEach(metricRegistry::remove);

    // Clear the message observer
    messageObserver.reset();

    // Clear the roles and logged in user
    MockUserLoginSecurityContext.roles.clear();
    MockUserLoginSecurityContext.currentUser = null;

    // Reset CSRF configuration
    configuration.csrfEnabled = false;

    // Reset the call count on the invocation finalizer
    MockResultInvocationFinalizer.Called.set(0);
  }

  @BeforeSuite
  public void init() {
    Module mvcModule = new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestMVCConfigurationModule());
        bind(CORSConfigurationProvider.class).to(TestCORSConfigurationProvider.class).in(Singleton.class);
        bind(MessageObserver.class).toInstance(messageObserver);
        bind(MetricRegistry.class).toInstance(metricRegistry);
        bind(UserLoginSecurityContext.class).to(MockUserLoginSecurityContext.class);

        // Test Content-Type
        ContentHandlerFactory.addContentHandler(binder(), "application/test+json", JacksonContentHandler.class);
      }
    };

    Module module = Modules.override(mvcModule).with(new TestActionModule(), new TestContentModule(), new TestSecurityModule(), new TestScopeModule());
    var mainConfig = new HTTPServerConfiguration().withListener(new HTTPListenerConfiguration(9080));
    TestPrimeMain main = new TestPrimeMain(new HTTPServerConfiguration[]{mainConfig}, module);
    simulator = new RequestSimulator(main, messageObserver);
    injector = simulator.getInjector();
    context = injector.getInstance(HTTPContext.class);
  }

  @DataProvider(name = "methodOverrides")
  public Object[][] methodOverrides() {
    return new Object[][]{
        {"X-HTTP-Method-Override"},
        {"x-http-method-override"},
        {"X-Method-Override"},
        {"x-method-override"}
    };
  }

  @AfterSuite
  public void shutdown() {
    simulator.shutdown();
  }

  @SuppressWarnings("Duplicates")
  protected void expectException(Class<? extends Throwable> throwable, ThrowingRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable e) {
      int count = 0;
      Throwable t = e;
      // Attempt to go up to 4 levels deep to find the cause of the exception
      while (count < 4 && t != null) {
        if (t.getClass().isAssignableFrom(throwable)) {
          return;
        }
        count++;
        t = t.getCause();
      }
      Assert.fail("Expected [" + throwable.getName() + "], but caught [" + e.getClass().getName() + "]");
      return;
    }
    Assert.fail("Expected [" + throwable.getName() + "], but no exception was thrown.");
  }

  /**
   * Makes an action invocation and configuration.
   *
   * @param action     The action object.
   * @param httpMethod The HTTP method.
   * @param extension  The extension.
   * @return The action invocation.
   */
  protected ActionInvocation makeActionInvocation(Object action, HTTPMethod httpMethod, String extension,
                                                  Map<String, List<String>> uriParameters) {
    DefaultActionConfigurationBuilder builder = injector.getInstance(DefaultActionConfigurationBuilder.class);
    ActionConfiguration actionConfiguration = builder.build(action.getClass());
    return new ActionInvocation(action, actionConfiguration.executeMethods.get(httpMethod), actionConfiguration.uri, extension, uriParameters, actionConfiguration, true);
  }

  /**
   * Makes an action invocation and configuration.
   *
   * @param httpMethod The HTTP method.
   * @param action     The action object.
   * @param methodName The method name to reflect and configure.
   * @param uri        The request URI.
   * @param extension  The extension.
   * @return The action invocation.
   * @throws Exception If the construction fails.
   */
  protected ActionInvocation makeActionInvocation(HTTPMethod httpMethod, Object action, String methodName, String uri,
                                                  String extension, String resultCode, Annotation annotation)
      throws Exception {
    Method method = action.getClass().getMethod(methodName);
    ExecuteMethodConfiguration executeMethod = new ExecuteMethodConfiguration(httpMethod, method, method.getAnnotation(Validation.class));
    Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods = new HashMap<>();
    executeMethods.put(httpMethod, executeMethod);

    Map<HTTPMethod, List<ValidationMethodConfiguration>> validationMethods = new HashMap<>();

    Map<String, Annotation> resultConfigurations = new HashMap<>();
    resultConfigurations.put(resultCode, annotation);

    return new ActionInvocation(action, executeMethod, uri, extension,
        new ActionConfiguration(EditAction.class, executeMethods, validationMethods, new ArrayList<>(), null, null, new ArrayList<>(), new HashMap<>(), new ArrayList<>(), resultConfigurations, new HashMap<>(), null, new HashMap<>(), new HashSet<>(), Collections.emptyList(), new ArrayList<>(), new HashMap<>(), uri, new ArrayList<>(), null, null
        ));
  }

  /**
   * Makes an action invocation and configuration.
   *
   * @param action     The action object.
   * @param httpMethod The HTTP method.
   * @param extension  The extension.
   * @return The action invocation.
   */
  protected ActionInvocation makeActionInvocation(Object action, HTTPMethod httpMethod, String extension) {
    DefaultActionConfigurationBuilder builder = injector.getInstance(DefaultActionConfigurationBuilder.class);
    ActionConfiguration actionConfiguration = builder.build(action.getClass());
    return new ActionInvocation(action, actionConfiguration.executeMethods.get(httpMethod), actionConfiguration.uri, extension, actionConfiguration);
  }

  @SuppressWarnings("unchecked")
  protected Map<String, List<String>> map(Object... params) {
    Map<String, List<String>> map = new HashMap<>();
    for (int i = 0; i < params.length; i += 2) {
      map.put(params[i].toString(), (List<String>) params[i + 1]);
    }
    return map;
  }

  public static class TestActionModule extends ActionModule {
    @Override
    protected void bindResultInvocationFinalizer() {
      bind(ResultInvocationFinalizer.class).to(MockResultInvocationFinalizer.class);
    }
  }

  public static class TestCORSConfigurationProvider implements CORSConfigurationProvider {
    @Override
    public CORSConfiguration get() {
      return corsConfiguration;
    }
  }

  public static class TestContentModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(ObjectMapper.class).toProvider(TestObjectMapperProvider.class);
    }
  }

  public static class TestMVCConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(MVCConfiguration.class).toInstance(configuration);
    }
  }

  public static class TestObjectMapperProvider extends ObjectMapperProvider {
    public static TestBuilder test;

    @Inject
    public TestObjectMapperProvider(Set<com.fasterxml.jackson.databind.Module> jacksonModules,
                                    MVCConfiguration configuration) {
      super(jacksonModules, configuration);
    }

    @Override
    public ObjectMapper get() {
      ObjectMapper objectMapper = super.get();

      if (test != null && test.objectMapperFunction != null) {
        return test.objectMapperFunction.apply(objectMapper);
      }

      return objectMapper;
    }
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
      // Don't bind as a singleton in tests so that I can change the key during a test
      bind(CipherProvider.class).to(DefaultCipherProvider.class);
      bind(VerifierProvider.class).to(MockVerifierProvider.class);
    }
  }
}

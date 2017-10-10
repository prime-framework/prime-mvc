/*
 * Copyright (c) 2012-2016, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.example.action.user.EditAction;
import org.primeframework.jwt.Verifier;
import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mock.servlet.MockHttpSession;
import org.primeframework.mock.servlet.MockServletContext;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.ValidationMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.DefaultActionConfigurationBuilder;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.guice.GuiceBootstrap;
import org.primeframework.mvc.guice.MVCModule;
import org.primeframework.mvc.jwt.MockVerifierProvider;
import org.primeframework.mvc.security.MockUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.primeframework.mvc.test.RequestSimulator;
import org.primeframework.mvc.validation.Validation;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;
import static java.util.Arrays.asList;

/**
 * This class is a base test for testing the Prime framework. It isn't recommended to use it outside of the Prime
 * project.
 *
 * @author Brian Pontarelli and James Humphrey
 */
public abstract class PrimeBaseTest {
  protected static MockServletContext context;

  protected static Injector injector;

  protected static MetricRegistry metricRegistry = new MetricRegistry();

  protected static MockHttpSession session;

  protected static RequestSimulator simulator;

  @Inject public MVCConfiguration configuration;

  @Inject public ObjectMapper objectMapper;

  @Inject public TestBuilder test;

  protected MockHttpServletRequest request;

  protected MockHttpServletResponse response;

  @BeforeSuite
  public static void init() throws ServletException {
    context = new MockServletContext(new File("src/test/web"));
    session = new MockHttpSession(context);
    ServletObjectsHolder.setServletContext(context);


    Module mvcModule = new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
        bind(MetricRegistry.class).toInstance(metricRegistry);
        bind(UserLoginSecurityContext.class).to(MockUserLoginSecurityContext.class);
      }
    };

    Module module = Modules.override(mvcModule).with(new SecurityModule());
    injector = GuiceBootstrap.initialize(module);
    simulator = new RequestSimulator(context, module);
  }

  /**
   * Sets up the servlet objects and injects the test.
   */
  @BeforeMethod
  public void setUp(Method method) {
    session.clear();
    request = new MockHttpServletRequest("/", Locale.getDefault(), false, "utf-8", session);
    response = new MockHttpServletResponse();

    ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper(request));
    ServletObjectsHolder.setServletResponse(response);

    injector.injectMembers(this);
    test.simulator = simulator;

    metricRegistry = injector.getInstance(MetricRegistry.class);
    // clear the metric registry before each test
    metricRegistry.getNames().forEach(metricRegistry::remove);
  }

  @AfterMethod
  public void tearDown() {
    ServletObjectsHolder.clearServletRequest();
    ServletObjectsHolder.clearServletResponse();
  }

  /**
   * Makes an action invocation and configuration.
   *
   * @param action     The action object.
   * @param httpMethod The HTTP method.
   * @param extension  The extension.
   * @return The action invocation.
   * @throws Exception If the construction fails.
   */
  protected ActionInvocation makeActionInvocation(Object action, HTTPMethod httpMethod, String extension, String... uriParameters) throws Exception {
    DefaultActionConfigurationBuilder builder = injector.getInstance(DefaultActionConfigurationBuilder.class);
    ActionConfiguration actionConfiguration = builder.build(action.getClass());
    return new ActionInvocation(action, actionConfiguration.executeMethods.get(httpMethod), actionConfiguration.uri,
        extension, asList(uriParameters), actionConfiguration, true);
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
                                                  String extension, String resultCode, Annotation annotation) throws Exception {
    Method method = action.getClass().getMethod(methodName);
    ExecuteMethodConfiguration executeMethod = new ExecuteMethodConfiguration(httpMethod, method, method.getAnnotation(Validation.class));
    Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods = new HashMap<>();
    executeMethods.put(httpMethod, executeMethod);

    Map<HTTPMethod, List<ValidationMethodConfiguration>> validationMethods = new HashMap<>();

    Map<String, Annotation> resultConfigurations = new HashMap<>();
    resultConfigurations.put(resultCode, annotation);

    return new ActionInvocation(action, executeMethod, uri, extension,
        new ActionConfiguration(EditAction.class, executeMethods, validationMethods, new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), resultConfigurations, new HashMap<>(), new HashMap<>(), new HashSet<>(), new ArrayList<>(), new HashMap<>(), uri, new ArrayList<>()
        ));
  }

  public static class SecurityModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(new TypeLiteral<Map<String, Verifier>>() {
      }).toProvider(MockVerifierProvider.class);
    }
  }

  public static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(MVCConfiguration.class).toInstance(new MockConfiguration());
    }
  }
}

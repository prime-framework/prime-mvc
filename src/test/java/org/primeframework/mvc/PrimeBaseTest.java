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
package org.primeframework.mvc;

import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.example.action.user.Edit;
import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mock.servlet.MockHttpSession;
import org.primeframework.mock.servlet.MockServletContext;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ExecuteMethod;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.result.Result;
import org.primeframework.mvc.action.result.ResultConfiguration;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.guice.GuiceBootstrap;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.primeframework.mvc.util.ReflectionUtils;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;
import org.primeframework.mvc.validation.jsr303.Validation;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import static java.util.Arrays.*;

/**
 * This class is a base test for testing the Prime framework. It isn't recommended to use it outside of the Prime
 * project.
 *
 * @author Brian Pontarelli and James Humphrey
 */
public abstract class PrimeBaseTest {
  protected static Injector injector;
  protected static MockServletContext context;
  protected static MockHttpSession session;
  protected MockHttpServletRequest request;
  protected MockHttpServletResponse response;

  @BeforeSuite
  public static void init() {
    context = new MockServletContext(new File("src/test/web"));
    session = new MockHttpSession(context);
    ServletObjectsHolder.setServletContext(context);

    injector = GuiceBootstrap.initialize(new TestModule());
  }
  
  /**
   * Sets up the servlet objects and injects the test.
   */
  @BeforeMethod
  public void setUp() {
    request = new MockHttpServletRequest("/", Locale.getDefault(), false, "utf-8", session);
    response = new MockHttpServletResponse();

    ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper(request));
    ServletObjectsHolder.setServletResponse(response);

    injector.injectMembers(this);
  }
  
  @AfterMethod
  public void tearDown() {
    ServletObjectsHolder.clearServletRequest();
    ServletObjectsHolder.clearServletResponse();
  }

  /**
   * Makes an action invocation.
   *
   * @param uri The request URI.
   * @param extension The extension.
   * @return The action invocation.
   * @throws Exception If the construction fails.
   */
  protected ActionInvocation makeActionInvocation(String uri, String extension) throws Exception {
    return new ActionInvocation(null, null, uri, extension, null);
  }

  /**
   * Makes an action invocation and configuration.
   *
   * @param httpMethod The HTTP method.
   * @param action The action object.
   * @param methodName The method name to reflect and configure.
   * @param uri The request URI.
   * @param extension The extension.
   * @return The action invocation.
   * @throws Exception If the construction fails.
   */
  protected ActionInvocation makeActionInvocation(HTTPMethod httpMethod, Object action, String methodName, String uri,
                                                  String extension, String... uriParamateres) throws Exception {
    Map<HTTPMethod, ExecuteMethod> executeMethods = new HashMap<HTTPMethod, ExecuteMethod>();
    ExecuteMethod executeMethod = null;
    if (methodName != null) {
      Method method = action.getClass().getMethod(methodName);
      executeMethod = new ExecuteMethod(method, method.getAnnotation(Validation.class));
      executeMethods.put(httpMethod, executeMethod);
    }

    List<Method> validationMethods = ReflectionUtils.findAllWithAnnotation(action.getClass(), ValidationMethod.class);
    List<Method> preValidationMethods = ReflectionUtils.findAllWithAnnotation(action.getClass(), PreValidationMethod.class);
    List<Method> postValidationMethods = ReflectionUtils.findAllWithAnnotation(action.getClass(), PostValidationMethod.class);
    List<Method> preParameterMethods = ReflectionUtils.findAllWithAnnotation(action.getClass(), PreParameterMethod.class);
    List<Method> postParameterMethods = ReflectionUtils.findAllWithAnnotation(action.getClass(), PostParameterMethod.class);

    Map<String, ResultConfiguration> resultConfigurations = new HashMap<String, ResultConfiguration>();

    return new ActionInvocation(action, executeMethod, uri, extension, asList(uriParamateres),
      new ActionConfiguration(action.getClass(), preValidationMethods, postValidationMethods, preParameterMethods, postParameterMethods, executeMethods, validationMethods, resultConfigurations, uri), true);
  }

  /**
   * Makes an action invocation and configuration.
   *
   * @param httpMethod The HTTP method.
   * @param action The action object.
   * @param methodName The method name to reflect and configure.
   * @param uri The request URI.
   * @param extension The extension.
   * @return The action invocation.
   * @throws Exception If the construction fails.
   */
  protected ActionInvocation makeActionInvocation(HTTPMethod httpMethod, Object action, String methodName, String uri,
                                                  String extension, String resultCode, Annotation annotation,
                                                  Class<? extends Result> resultType) throws Exception {
    Method method = action.getClass().getMethod(methodName);
    ExecuteMethod executeMethod = new ExecuteMethod(method, method.getAnnotation(Validation.class));
    Map<HTTPMethod, ExecuteMethod> executeMethods = new HashMap<HTTPMethod, ExecuteMethod>();
    executeMethods.put(httpMethod, executeMethod);

    List<Method> validationMethods = new ArrayList<Method>();

    Map<String, ResultConfiguration> resultConfigurations = new HashMap<String, ResultConfiguration>();
    resultConfigurations.put(resultCode, new ResultConfiguration(annotation, resultType));

    return new ActionInvocation(action, executeMethod, uri, extension,
      new ActionConfiguration(Edit.class, new ArrayList<Method>(), new ArrayList<Method>(), new ArrayList<Method>(), new ArrayList<Method>(), executeMethods, validationMethods, resultConfigurations, uri));
  }

  public static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(MVCConfiguration.class).toInstance(new MockConfiguration());
    }
  }
}

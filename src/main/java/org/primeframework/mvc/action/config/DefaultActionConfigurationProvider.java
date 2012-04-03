/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.config;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ExecuteMethod;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.ResultConfiguration;
import org.primeframework.mvc.action.result.annotation.ResultAnnotation;
import org.primeframework.mvc.action.result.annotation.ResultContainerAnnotation;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.util.ClassClasspathResolver;
import org.primeframework.mvc.util.URIBuilder;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.jsr303.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class loads the configuration by scanning the classpath for packages and action classes.
 *
 * @author Brian Pontarelli
 */
@Singleton
@SuppressWarnings("unchecked")
public class DefaultActionConfigurationProvider implements ActionConfigurationProvider {
  private static final Logger logger = LoggerFactory.getLogger(DefaultActionConfigurationProvider.class);
  public static final String ACTION_CONFIGURATION_KEY = "primeActionConfiguration";
  private final ServletContext context;

  @Inject
  public DefaultActionConfigurationProvider(ServletContext context, URIBuilder uriBuilder) {
    this.context = context;

    ClassClasspathResolver resolver = new ClassClasspathResolver();
    Set<Class<?>> actionClassses;
    try {
      actionClassses = resolver.findByLocators(new ClassClasspathResolver.AnnotatedWith(Action.class), true, null, "action");
    } catch (IOException e) {
      throw new PrimeException("Error discovering action classes", e);
    }

    Map<String, ActionConfiguration> actionConfigurations = new HashMap<String, ActionConfiguration>();
    for (Class<?> actionClass : actionClassses) {
      if ((actionClass.getModifiers() & Modifier.ABSTRACT) != 0) {
        throw new PrimeException("The action class [" + actionClass + "] is annotated with the @Action annotation but is " +
          "abstract. You can only annotate concrete action classes");
      }

      String uri = uriBuilder.build(actionClass);
      Map<HTTPMethod, ExecuteMethod> executeMethods = findExecuteMethods(actionClass);
      List<Method> validationMethods = findValidationMethods(actionClass);
      Map<String, ResultConfiguration> resultAnnotations = findResultConfigurations(actionClass);
      ActionConfiguration actionConfiguration = new ActionConfiguration(actionClass, executeMethods, validationMethods, resultAnnotations, uri);

      if (actionConfigurations.containsKey(uri)) {
        boolean previousOverrideable = actionConfigurations.get(uri).actionClass.getAnnotation(Action.class).overridable();
        boolean thisOverrideable = actionClass.getAnnotation(Action.class).overridable();
        if ((previousOverrideable && thisOverrideable) || (!previousOverrideable && !thisOverrideable)) {
          throw new PrimeException("Duplicate action found for URI [" + uri + "]. The first action class found was [" +
            actionConfigurations.get(uri).actionClass + "]. The second action class found was [" + actionClass + "]. Either " +
            "both classes are marked as overridable or neither is marked as overridable. You can fix this by only " +
            "marking one of the classes with the overridable flag on the Action annotation.");
        } else if (previousOverrideable) {
          actionConfigurations.put(uri, actionConfiguration);
        }
      } else {
        actionConfigurations.put(uri, actionConfiguration);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Added action configuration for [" + actionClass + "] and the uri [" + uri + "]");
      }
    }

    context.setAttribute(ACTION_CONFIGURATION_KEY, actionConfigurations);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ActionConfiguration lookup(String uri) {
    Map<String, ActionConfiguration> configuration = (Map<String, ActionConfiguration>) context.getAttribute(ACTION_CONFIGURATION_KEY);
    if (configuration == null) {
      return null;
    }

    return configuration.get(uri);
  }

  /**
   * Locates all the valid execute methods on the action.
   *
   * @param actionClass The action class.
   * @return The execute methods Map.
   */
  protected Map<HTTPMethod, ExecuteMethod> findExecuteMethods(Class<?> actionClass) {
    Method defaultMethod = null;
    try {
      defaultMethod = actionClass.getMethod("execute");
    } catch (NoSuchMethodException e) {
      // Ignore
    }

    Map<HTTPMethod, ExecuteMethod> executeMethods = new HashMap<HTTPMethod, ExecuteMethod>();
    for (HTTPMethod httpMethod : HTTPMethod.values()) {
      Method method = null;
      try {
        method = actionClass.getMethod(httpMethod.name().toLowerCase());
      } catch (NoSuchMethodException e) {
        // Ignore
      }

      // Handle HEAD requests using a GET
      if (method == null && httpMethod == HTTPMethod.HEAD) {
        try {
          method = actionClass.getMethod("get");
        } catch (NoSuchMethodException e) {
          // Ignore
        }
      }

      if (method == null) {
        method = defaultMethod;
      }

      if (method != null) {
        verify(method);
        executeMethods.put(httpMethod, new ExecuteMethod(method, method.getAnnotation(Validation.class)));
      }
    }

    if (executeMethods.isEmpty()) {
      throw new PrimeException("The action class [" + actionClass + "] is missing at least one valid execute method. " +
        "The class can define execute methods with the same names as the HTTP methods (lowercased) or a default execute " +
        "method named [execute]. For example:\n\n" +
        "public String execute() {\n" +
        "  return \"success\"\n" +
        "}\n\n" +
        "or\n\n" +
        "public String post() {\n" +
        "  return \"success\"\n" +
        "}");
    }

    return executeMethods;
  }

  /**
   * Ensures that the method is a correct execute method.
   *
   * @param method The method.
   * @throws PrimeException If the method is invalid.
   */
  protected void verify(Method method) {
    if (method.getReturnType() != String.class || method.getParameterTypes().length != 0) {
      throw new PrimeException("The action class [" + method.getDeclaringClass().getClass() + "] has defined an " +
        "execute method named [" + method.getName() + "] that is invalid. Execute methods must have zero parameters " +
        "and return a String like this:\n\n" +
        "public String execute() {\n" +
        "  return \"success\"\n" +
        "}");
    }
  }

  /**
   * Finds any methods on the action annotated with the {@link ValidationMethod} annotation.
   *
   * @param actionClass The action class.
   * @return The validation methods or an empty list.
   */
  protected List<Method> findValidationMethods(Class<?> actionClass) {
    List<Method> validationMethods = new ArrayList<Method>();
    for (Method method : actionClass.getMethods()) {
      if (method.isAnnotationPresent(ValidationMethod.class)) {
        validationMethods.add(method);
      }
    }

    return validationMethods;
  }

  /**
   * Finds all of the result configurations for the action class.
   *
   * @param actionClass The action class.
   * @return The map of all the result configurations.
   */
  protected Map<String, ResultConfiguration> findResultConfigurations(Class<?> actionClass) {
    Map<String, ResultConfiguration> resultConfigurations = new HashMap<String, ResultConfiguration>();
    Annotation[] annotations = actionClass.getAnnotations();
    for (Annotation annotation : annotations) {
      Class<? extends Annotation> annotationType = annotation.annotationType();
      ResultAnnotation resultAnnotation = annotationType.getAnnotation(ResultAnnotation.class);
      if (resultAnnotation != null) {
        addResultConfiguration(actionClass, resultConfigurations, annotation, annotationType, resultAnnotation);
      } else if (annotationType.isAnnotationPresent(ResultContainerAnnotation.class)) {
        // There are multiple annotations inside the value
        try {
          Annotation[] results = (Annotation[]) annotation.getClass().getMethod("value").invoke(annotation);
          for (Annotation result : results) {
            annotationType = result.annotationType();
            resultAnnotation = annotationType.getAnnotation(ResultAnnotation.class);
            addResultConfiguration(actionClass, resultConfigurations, annotation, annotationType, resultAnnotation);
          }
        } catch (Exception e) {
          throw new PrimeException("The custom result annotation container [" + annotationType + "] must " +
            "have a method named [value] that is an array of result annotations. For example:\n\n" +
            "public @interface MyContainer {\n" +
            "  MyResult[] value();\n" +
            "}");
        }
      }
    }

    return resultConfigurations;
  }

  /**
   * Adds the result annotation to the map and handles throwing an exception if there are duplicates.
   *
   * @param actionClass The action class.
   * @param resultConfigurations The result configurations Map.
   * @param annotation The annotation to check and add.
   */
  protected void addResultConfiguration(Class<?> actionClass, Map<String, ResultConfiguration> resultConfigurations,
                                        Annotation annotation, Class<? extends Annotation> annotationType,
                                        ResultAnnotation resultAnnotation) {
    try {
      String code = (String) annotation.getClass().getMethod("code").invoke(annotation);
      if (resultConfigurations.containsKey(code)) {
        throw new PrimeException("The action class [" + actionClass + "] contains two or more result annotations for " +
          "the code [" + code + "]");
      }

      resultConfigurations.put(code, new ResultConfiguration(annotation, resultAnnotation.value()));
    } catch (Exception e) {
      throw new PrimeException("The custom result annotation [" + annotationType + "] is missing a method named [code] " +
        "that returns a String. For example:\n\n" +
        "public @interface MyResult {\n" +
        "  String code() default \"success\";\n" +
        "}");
    }
  }
}
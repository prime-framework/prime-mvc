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
package org.primeframework.mvc.action.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primeframework.jwt.domain.JWT;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.JWTMethodConfiguration;
import org.primeframework.mvc.action.ValidationMethodConfiguration;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.ResultAnnotation;
import org.primeframework.mvc.action.result.annotation.ResultContainerAnnotation;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.scope.ScopeField;
import org.primeframework.mvc.scope.annotation.ScopeAnnotation;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.util.ReflectionUtils;
import org.primeframework.mvc.util.URIBuilder;
import org.primeframework.mvc.validation.Validation;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;

import com.google.inject.Inject;

/**
 * Default action configuration builder.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionConfigurationBuilder implements ActionConfigurationBuilder {
  private final Set<ActionConfigurator> configurators;

  private final URIBuilder uriBuilder;

  @Inject
  public DefaultActionConfigurationBuilder(URIBuilder uriBuilder, Set<ActionConfigurator> configurators) {
    this.uriBuilder = uriBuilder;
    this.configurators = configurators;
  }

  /**
   * Builds the action configuration using the class.
   *
   * @param actionClass The action class.
   * @return The action configuration.
   */
  @Override
  public ActionConfiguration build(Class<?> actionClass) {
    if ((actionClass.getModifiers() & Modifier.ABSTRACT) != 0) {
      throw new PrimeException("The action class [" + actionClass + "] is annotated with the @Action annotation but is " +
          "abstract. You can only annotate concrete action classes");
    }

    String uri = uriBuilder.build(actionClass);
    Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods = findExecuteMethods(actionClass);
    List<Method> formPrepareMethods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, FormPrepareMethod.class);
    List<Method> preParameterMethods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, PreParameterMethod.class);
    List<Method> postParameterMethods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, PostParameterMethod.class);
    List<Method> preValidationMethods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, PreValidationMethod.class);
    List<Method> postValidationMethods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, PostValidationMethod.class);
    Map<String, Annotation> resultAnnotations = findResultConfigurations(actionClass);
    Map<String, PreParameter> preParameterMembers = ReflectionUtils.findAllMembersWithAnnotation(actionClass, PreParameter.class);
    Map<String, FileUpload> fileUploadMembers = ReflectionUtils.findAllMembersWithAnnotation(actionClass, FileUpload.class);
    Set<String> memberNames = ReflectionUtils.findAllMembers(actionClass);

    Map<HTTPMethod, List<ValidationMethodConfiguration>> validationMethods = findValidationMethods(actionClass);
    Map<HTTPMethod, List<JWTMethodConfiguration>> jwtAuthorizationMethods = findJwtAuthorizationMethods(actionClass, executeMethods.keySet());

    List<ScopeField> scopeFields = findScopeFields(actionClass);

    Map<Class<?>, Object> additionalConfiguration = getAdditionalConfiguration(actionClass);

    return new ActionConfiguration(actionClass, executeMethods, validationMethods, formPrepareMethods, jwtAuthorizationMethods,
        postValidationMethods, preParameterMethods, postParameterMethods, resultAnnotations, preParameterMembers, fileUploadMembers,
        memberNames, scopeFields, additionalConfiguration, uri, preValidationMethods);
  }

  /**
   * Adds the result annotation to the map and handles throwing an exception if there are duplicates.
   *
   * @param actionClass          The action class.
   * @param resultConfigurations The result configurations Map.
   * @param annotation           The annotation to check and add.
   */
  protected void addResultConfiguration(Class<?> actionClass, Map<String, Annotation> resultConfigurations,
                                        Annotation annotation, Class<? extends Annotation> annotationType) {
    try {
      String code = (String) annotation.getClass().getMethod("code").invoke(annotation);
      if (resultConfigurations.containsKey(code)) {
        throw new PrimeException("The action class [" + actionClass + "] contains two or more result annotations for " +
            "the code [" + code + "]");
      }

      resultConfigurations.put(code, annotation);
    } catch (NoSuchMethodException e) {
      throw new PrimeException("The result annotation [" + annotationType + "] is missing a method named [code] that " +
          "returns a String. For example:\n\n" +
          "public @interface MyResult {\n" +
          "  String code() default \"success\";\n" +
          "}", e);
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new PrimeException("Unable to invoke the code() method on the result annotation container [" +
          annotationType + "]", e);
    }
  }

  /**
   * Adds all the result annotations for the given class.
   *
   * @param actionClass The action class.
   */
  protected Map<String, Annotation> addResultsForClass(Class<?> actionClass) {
    Map<String, Annotation> resultConfigurations = new HashMap<>();
    Annotation[] annotations = actionClass.getAnnotations();
    for (Annotation annotation : annotations) {
      Class<? extends Annotation> annotationType = annotation.annotationType();
      ResultAnnotation resultAnnotation = annotationType.getAnnotation(ResultAnnotation.class);
      if (resultAnnotation != null) {
        addResultConfiguration(actionClass, resultConfigurations, annotation, annotationType);
      } else if (annotationType.isAnnotationPresent(ResultContainerAnnotation.class)) {
        // There are multiple annotations inside the value
        try {
          Annotation[] results = (Annotation[]) annotation.getClass().getMethod("value").invoke(annotation);
          for (Annotation result : results) {
            annotationType = result.annotationType();
            addResultConfiguration(actionClass, resultConfigurations, result, annotationType);
          }
        } catch (NoSuchMethodException e) {
          throw new PrimeException("The result annotation container [" + annotationType + "] must have a method named " +
              "[value] that is an array of result annotations. For example:\n\n" +
              "public @interface MyContainer {\n" +
              "  MyResult[] value();\n" +
              "}", e);
        } catch (InvocationTargetException | IllegalAccessException e) {
          throw new PrimeException("Unable to invoke the value() method on the result annotation container [" +
              annotationType + "]", e);
        }
      }
    }

    return resultConfigurations;
  }

  /**
   * Locates all the valid execute methods on the action.
   *
   * @param actionClass The action class.
   * @return The execute methods Map.
   */
  protected Map<HTTPMethod, ExecuteMethodConfiguration> findExecuteMethods(Class<?> actionClass) {
    Method defaultMethod = null;
    try {
      defaultMethod = actionClass.getMethod("execute");
    } catch (NoSuchMethodException e) {
      // Ignore
    }

    Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods = new HashMap<>();
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
        executeMethods.put(httpMethod, new ExecuteMethodConfiguration(httpMethod, method, method.getAnnotation(Validation.class)));
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

  protected Map<HTTPMethod, List<JWTMethodConfiguration>> findJwtAuthorizationMethods(Class<?> actionClass, Set<HTTPMethod> executeMethods) {
    // When JWT is not enabled, we will not call any of the JWT Authorization Methods.
    if (!actionClass.getAnnotation(Action.class).jwtEnabled()) {
      return Collections.emptyMap();
    }

    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, JWTAuthorizeMethod.class);
    if (methods.isEmpty()) {
      throw new PrimeException("The action class [" + actionClass + "] is missing at a JWT Authorization method. " +
          "The class must define a one or more methods annotated " + JWTAuthorizeMethod.class.getSimpleName() + " when [jwtEnabled] is set to [true].");
    }

    // Return type must be Boolean or boolean
    if (methods.stream().anyMatch(m -> m.getReturnType() != Boolean.TYPE && m.getReturnType() != Boolean.class)) {
      throw new PrimeException("The action class [" + actionClass + "] has at least one JWT Authorization method that has declared a return "
          + "type of something other than boolean. Your method annotated with " + JWTAuthorizeMethod.class.getSimpleName() + " must declare a return type"
          + "of boolean or Boolean.");
    }

    // Must take a single parameter for a JWT
    if (methods.stream().anyMatch(m -> m.getParameterCount() != 1 || m.getParameterTypes()[0] != JWT.class)) {
      throw new PrimeException("The action class [" + actionClass + "] has at least one JWT Authorization method that has not declared the correct method "
          + "signature. Your method annotated with " + JWTAuthorizeMethod.class.getSimpleName() + " must declare a single method parameter of type JWT.");
    }

    // Map HTTP method to a list of JWT Authorize Methods.
    Map<HTTPMethod, List<JWTMethodConfiguration>> jwtMethods = new HashMap<>();
    methods.stream()
           .map(m -> new JWTMethodConfiguration(m, m.getAnnotation(JWTAuthorizeMethod.class)))
           .forEach(c -> Arrays.asList(c.annotation.httpMethods())
                               .forEach(m -> jwtMethods.computeIfAbsent(m, k -> new ArrayList<>()).add(c)));


    // Ensure we're calling the JWT Authorize GET method for a HEAD request
    if (jwtMethods.containsKey(HTTPMethod.GET) && !jwtMethods.containsKey(HTTPMethod.HEAD)) {
      jwtMethods.put(HTTPMethod.HEAD, jwtMethods.get(HTTPMethod.GET));
    }

    // Ensure all methods are accounted for JWT authorization
    if (executeMethods.equals(jwtMethods.keySet())) {
      throw new PrimeException("The action class [" + actionClass + "] is missing at a JWT Authorization method. " +
          "The class must define one or more methods annotated " + JWTAuthorizeMethod.class.getSimpleName() + " when [jwtEnabled] is set to [true]. "
          + "Ensure that for each execute method in your action such as post, put, get and delete that a method is configured to authorize the JWT.");
    }

    return jwtMethods;
  }

  /**
   * Finds all of the result configurations for the action class.
   *
   * @param actionClass The action class.
   * @return The map of all the result configurations.
   */
  protected Map<String, Annotation> findResultConfigurations(Class<?> actionClass) {
    Map<String, Annotation> resultConfigurations = new HashMap<>();
    while (actionClass != Object.class) {
      Map<String, Annotation> resultsForClass = addResultsForClass(actionClass);
      resultsForClass.forEach(resultConfigurations::putIfAbsent);
      actionClass = actionClass.getSuperclass();
    }

    return resultConfigurations;
  }

  /**
   * Locates all the fields in the action class that have a scope annotation on them.
   *
   * @param actionClass The action class.
   * @return The scope fields.
   */
  protected List<ScopeField> findScopeFields(Class<?> actionClass) {
    List<ScopeField> scopeFields = new ArrayList<>();
    while (actionClass != Object.class) {
      Field[] fields = actionClass.getDeclaredFields();
      for (Field field : fields) {
        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
          Class<? extends Annotation> type = annotation.annotationType();
          if (type.isAnnotationPresent(ScopeAnnotation.class)) {
            scopeFields.add(new ScopeField(field, annotation));
          }
        }
      }

      actionClass = actionClass.getSuperclass();
    }

    return scopeFields;
  }

  /**
   * Locates all of the validation methods and return a map keyed by HTTP Method.
   *
   * @param actionClass The action class.
   * @return The validation method configurations.
   */
  protected Map<HTTPMethod, List<ValidationMethodConfiguration>> findValidationMethods(Class<?> actionClass) {
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, ValidationMethod.class);

    // Map HTTP method to a list of Validation Methods.
    Map<HTTPMethod, List<ValidationMethodConfiguration>> validationMethods = new HashMap<>();
    methods.stream()
           .map(m -> new ValidationMethodConfiguration(m, m.getAnnotation(ValidationMethod.class)))
           .forEach(c -> Arrays.asList(c.annotation.httpMethods())
                               .forEach(m -> validationMethods.computeIfAbsent(m, k -> new ArrayList<>()).add(c)));


    // Ensure we're calling the GET Validation Method for a HEAD request
    if (validationMethods.containsKey(HTTPMethod.GET) && !validationMethods.containsKey(HTTPMethod.HEAD)) {
      validationMethods.put(HTTPMethod.HEAD, validationMethods.get(HTTPMethod.GET));
    }

    return validationMethods;
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

  private Map<Class<?>, Object> getAdditionalConfiguration(Class<?> actionClass) {
    Map<Class<?>, Object> additionalConfiguration = new HashMap<>();
    for (ActionConfigurator configurator : configurators) {
      Object config = configurator.configure(actionClass);
      if (config != null) {
        additionalConfiguration.put(config.getClass(), config);
      }
    }
    return additionalConfiguration;
  }
}

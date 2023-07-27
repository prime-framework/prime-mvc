/*
 * Copyright (c) 2012-2023, Inversoft Inc., All Rights Reserved
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.jwt.domain.JWT;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.AuthorizationMethodConfiguration;
import org.primeframework.mvc.action.ConstraintOverrideMethodConfiguration;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.JWTMethodConfiguration;
import org.primeframework.mvc.action.PreParameterMethodConfiguration;
import org.primeframework.mvc.action.ValidationMethodConfiguration;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.annotation.AllowUnknownParameters;
import org.primeframework.mvc.action.result.annotation.ResultAnnotation;
import org.primeframework.mvc.action.result.annotation.ResultContainerAnnotation;
import org.primeframework.mvc.content.ValidContentTypes;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.annotation.PreParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.parameter.annotation.UnknownParameters;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.scope.ScopeField;
import org.primeframework.mvc.scope.annotation.ScopeAnnotation;
import org.primeframework.mvc.security.AuthorizeSchemeData;
import org.primeframework.mvc.security.annotation.AnonymousAccess;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.security.annotation.ConstraintOverrideMethod;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;
import org.primeframework.mvc.util.ReflectionUtils;
import org.primeframework.mvc.util.URIBuilder;
import org.primeframework.mvc.validation.Validation;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;

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

    Action action = actionClass.getAnnotation(Action.class);
    String uri = !action.baseURI().equals("") ? action.baseURI() : uriBuilder.build(actionClass);
    boolean allowKnownParameters = actionClass.getAnnotation(AllowUnknownParameters.class) != null;
    Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods = findExecuteMethods(actionClass);
    List<Method> formPrepareMethods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, FormPrepareMethod.class);
    List<Method> postParameterMethods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, PostParameterMethod.class);
    Map<Class<?>, List<Method>> preRenderMethodsMap = findAllPreRenderMethods(actionClass);
    List<Method> preValidationMethods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, PreValidationMethod.class);
    List<Method> postValidationMethods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, PostValidationMethod.class);
    Map<String, Annotation> resultAnnotations = findResultConfigurations(actionClass);
    Map<String, PreParameter> preParameterMembers = ReflectionUtils.findAllMembersWithAnnotation(actionClass, PreParameter.class);
    Map<String, FileUpload> fileUploadMembers = ReflectionUtils.findAllMembersWithAnnotation(actionClass, FileUpload.class);
    Set<String> memberNames = ReflectionUtils.findAllMembers(actionClass);

    Map<HTTPMethod, List<PreParameterMethodConfiguration>> preParameterMethods = findAnnotatedMethods(
        actionClass,
        PreParameterMethod.class,
        (annotation, method) -> new PreParameterMethodConfiguration(method, annotation),
        config -> Arrays.stream(config.annotation.httpMethods()).map(HTTPMethod::of).collect(Collectors.toList()));
    Map<HTTPMethod, List<ValidationMethodConfiguration>> validationMethods = findAnnotatedMethods(
        actionClass,
        ValidationMethod.class,
        (annotation, method) -> new ValidationMethodConfiguration(method, annotation),
        config -> Arrays.stream(config.annotation.httpMethods()).map(HTTPMethod::of).collect(Collectors.toList()));

    List<String> securitySchemes = findSecuritySchemes(actionClass);
    Map<HTTPMethod, List<AuthorizationMethodConfiguration>> authorizationMethods = findAuthorizationMethods(actionClass, securitySchemes,
                                                                                                            executeMethods);
    Map<HTTPMethod, List<JWTMethodConfiguration>> jwtAuthorizationMethods = findJwtAuthorizationMethods(actionClass, securitySchemes, executeMethods);

    List<ScopeField> scopeFields = findScopeFields(actionClass);

    Map<Class<?>, Object> additionalConfiguration = getAdditionalConfiguration(actionClass);
    Map<HTTPMethod, ConstraintOverrideMethodConfiguration> constraintValidationMethods = findConstraintValidationMethod(actionClass);

    // Unknown parameters field
    Field unknownParametersField = findUnknownParametersField(actionClass);
    Set<String> validContentTypes = findAllowedContentTypes(actionClass);

    return new ActionConfiguration(actionClass, allowKnownParameters, constraintValidationMethods, executeMethods, validationMethods, formPrepareMethods, authorizationMethods, jwtAuthorizationMethods, postValidationMethods, preParameterMethods, postParameterMethods, resultAnnotations, preParameterMembers, preRenderMethodsMap, fileUploadMembers, memberNames, securitySchemes, scopeFields, additionalConfiguration, uri, preValidationMethods, unknownParametersField, validContentTypes);
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

  protected Set<String> findAllowedContentTypes(Class<?> actionClass) {
    Class<?> currentClass = actionClass;
    while (!currentClass.equals(Object.class)) {
      ValidContentTypes annotation = currentClass.getAnnotation(ValidContentTypes.class);
      if (annotation != null) {
        return Set.of(annotation.value());
      }

      currentClass = currentClass.getSuperclass();
    }

    return Set.of();
  }

  /**
   * Locates all the validation methods and return a map keyed by HTTP Method.
   *
   * @param actionClass The action class.
   * @return The validation method configurations.
   */
  protected <T extends Annotation, U> Map<HTTPMethod, List<U>> findAnnotatedMethods(Class<?> actionClass,
                                                                                    Class<T> annotationType,
                                                                                    BiFunction<T, Method, U> constructor,
                                                                                    Function<U, List<HTTPMethod>> methodFunction) {
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, annotationType);

    // Map HTTP method to a list of Validation Methods.
    Map<HTTPMethod, List<U>> map = new HashMap<>();
    methods.stream()
           .map(m -> constructor.apply(m.getAnnotation(annotationType), m))
           .forEach(c -> methodFunction.apply(c)
                                       .forEach(m -> map.computeIfAbsent(m, k -> new ArrayList<>()).add(c)));


    // Ensure we're calling the GET Validation Method for a HEAD request
    if (map.containsKey(HTTPMethod.GET) && !map.containsKey(HTTPMethod.HEAD)) {
      map.put(HTTPMethod.HEAD, map.get(HTTPMethod.GET));
    }

    return map;
  }

  protected Map<HTTPMethod, List<AuthorizationMethodConfiguration>> findAuthorizationMethods(Class<?> actionClass,
                                                                                             List<String> securitySchemes,
                                                                                             Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods) {
    // When Authorize Method scheme is not enabled, we will not call any of the Authorization Methods.
    if (!securitySchemes.contains("authorize-method")) {
      return Collections.emptyMap();
    }

    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, AuthorizeMethod.class);
    if (methods.isEmpty()) {
      throw new PrimeException("The action class [" + actionClass + "] is missing at a Authorization method. " +
                               "The class must define a one or more methods annotated " + AuthorizeMethod.class.getSimpleName() + " when the [authorize-method] is specified as a security scheme.");
    }

    // Return type must be Boolean or boolean
    if (methods.stream().anyMatch(m -> m.getReturnType() != Boolean.TYPE && m.getReturnType() != Boolean.class)) {
      throw new PrimeException("The action class [" + actionClass + "] has at least one Authorization method that has declared a return "
                               + "type of something other than boolean. Your method annotated with " + AuthorizeMethod.class.getSimpleName() + " must declare a return type"
                               + "of boolean or Boolean.");
    }

    // Optionally take a single parameter
    if (methods.stream().anyMatch(m -> m.getParameterCount() > 1)) {
      throw new PrimeException(
          "The action class [" + actionClass + "] has at least one Authorization method that has not declared the correct method "
          + "signature. Your method annotated with " + AuthorizeMethod.class.getSimpleName() + " may be declared without parameters, or a single parameter of type " + AuthorizeSchemeData.class.getSimpleName() + ".");
    }

    // If a parameter is in the signature, it must be of type AuthorizeSchemeData
    if (methods.stream().filter(m -> m.getParameterCount() == 1).anyMatch(m -> m.getParameterTypes()[0] != AuthorizeSchemeData.class)) {
      throw new PrimeException(
          "The action class [" + actionClass + "] has at least one Authorization method that has not declared the correct method "
          + "signature. Your method annotated with " + AuthorizeMethod.class.getSimpleName() + " must declare a single method parameter of type " + AuthorizeSchemeData.class.getSimpleName() + ".");
    }

    // Map HTTP method to a list of Authorize Methods.
    Map<HTTPMethod, List<AuthorizationMethodConfiguration>> authorizationMethods = new HashMap<>();
    methods.stream()
           .map(m -> new AuthorizationMethodConfiguration(m, m.getAnnotation(AuthorizeMethod.class)))
           .forEach(c -> Arrays.stream(c.annotation.httpMethods()).map(HTTPMethod::of).collect(Collectors.toList())
                               .forEach(m -> authorizationMethods.computeIfAbsent(m, k -> new ArrayList<>()).add(c)));

    // Ensure we're calling the Authorize GET method for a HEAD request
    if (authorizationMethods.containsKey(HTTPMethod.GET) && !authorizationMethods.containsKey(HTTPMethod.HEAD)) {
      authorizationMethods.put(HTTPMethod.HEAD, authorizationMethods.get(HTTPMethod.GET));
    }

    // All Execute Methods that require authentication need to be accounted for in Authorization Methods. It is ok if the Authorization Methods define a superset of the execute methods.
    Set<HTTPMethod> authenticatedMethods = executeMethods.keySet().stream().filter(k -> {
      ExecuteMethodConfiguration methodConfiguration = executeMethods.get(k);
      return methodConfiguration.annotations.containsKey(AnonymousAccess.class);
    }).collect(Collectors.toSet());

    if (!authorizationMethods.keySet().containsAll(authenticatedMethods)) {
      throw new PrimeException("The action class [" + actionClass + "] is missing at an Authorization method. " +
                               "The class must define one or more methods annotated " + AuthorizeMethod.class.getSimpleName() + " when the [authorize-method] is specified as a security scheme. "
                               + "Ensure that for each execute method in your action such as post, put, get and delete that a method is configured to authorize the request.");
    }

    return authorizationMethods;
  }

  protected Map<HTTPMethod, ConstraintOverrideMethodConfiguration> findConstraintValidationMethod(Class<?> actionClass) {
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, ConstraintOverrideMethod.class);
    if (methods.isEmpty()) {
      return Collections.emptyMap();
    }

    // Return type must be Collection<T>
    if (methods.stream().anyMatch(m -> !Collection.class.isAssignableFrom(m.getReturnType()))) {
      throw new PrimeException(
          "The action class [" + actionClass + "] has at least one method annotated with " + ConstraintOverrideMethod.class.getSimpleName() +
          " that has a declared a return type of something other than Collection<?>. Your method annotated with " + ConstraintOverrideMethod.class.getSimpleName()
          + " must declare a return type of Collection<T>.");
    }

    // Map HTTP method to a list of Constraint Override Methods.
    Map<HTTPMethod, ConstraintOverrideMethodConfiguration> constraintOverrideMethods = new HashMap<>();
    for (Method method : methods) {
      ConstraintOverrideMethod annotation = method.getAnnotation(ConstraintOverrideMethod.class);
      for (String stringMethod : annotation.httpMethods()) {
        HTTPMethod httpMethod = HTTPMethod.of(stringMethod);
        if (constraintOverrideMethods.containsKey(httpMethod)) {
          throw new PrimeException(
              "The action class [" + actionClass + "] has more than one method annotated with " + ConstraintOverrideMethod.class.getSimpleName() +
              " for the same HTTP method. You may only have one method annotated " + ConstraintOverrideMethod.class.getSimpleName()
              + " for any one HTTP method.");
        }

        constraintOverrideMethods.put(httpMethod, new ConstraintOverrideMethodConfiguration(method, annotation));
      }
    }

    return constraintOverrideMethods;
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
    for (HTTPMethod httpMethod : HTTPMethod.StandardMethods.values()) {
      Method method = null;
      try {
        method = actionClass.getMethod(httpMethod.name().toLowerCase());
      } catch (NoSuchMethodException e) {
        // Ignore
      }

      // Handle HEAD requests using a GET
      if (method == null && HTTPMethod.HEAD.is(httpMethod)) {
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

  protected Map<HTTPMethod, List<JWTMethodConfiguration>> findJwtAuthorizationMethods(Class<?> actionClass,
                                                                                      List<String> securitySchemes,
                                                                                      Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods) {
    // When JWT scheme is not enabled, we will not call any of the JWT Authorization Methods.
    if (!securitySchemes.contains("jwt")) {
      return Collections.emptyMap();
    }

    // JWT Authorize methods are not required.
    List<Method> methods = ReflectionUtils.findAllMethodsWithAnnotation(actionClass, JWTAuthorizeMethod.class);

    // Return type must be Boolean or boolean
    if (methods.stream().anyMatch(m -> m.getReturnType() != Boolean.TYPE && m.getReturnType() != Boolean.class)) {
      throw new PrimeException("The action class [" + actionClass + "] has at least one JWT Authorization method that has declared a return "
                               + "type of something other than boolean. Your method annotated with " + JWTAuthorizeMethod.class.getSimpleName() + " must declare a return type"
                               + "of boolean or Boolean.");
    }

    // Must take a single parameter for a JWT
    if (methods.stream().anyMatch(m -> m.getParameterCount() != 1 || m.getParameterTypes()[0] != JWT.class)) {
      throw new PrimeException(
          "The action class [" + actionClass + "] has at least one JWT Authorization method that has not declared the correct method "
          + "signature. Your method annotated with " + JWTAuthorizeMethod.class.getSimpleName() + " must declare a single method parameter of type JWT.");
    }

    // Map HTTP method to a list of JWT Authorize Methods.
    Map<HTTPMethod, List<JWTMethodConfiguration>> jwtMethods = new HashMap<>();
    methods.stream()
           .map(m -> new JWTMethodConfiguration(m, m.getAnnotation(JWTAuthorizeMethod.class)))
           .forEach(c -> Arrays.stream(c.annotation.httpMethods()).map(HTTPMethod::of).collect(Collectors.toList())
                               .forEach(m -> jwtMethods.computeIfAbsent(m, k -> new ArrayList<>()).add(c)));

    // Ensure we're calling the JWT Authorize GET method for a HEAD request
    if (jwtMethods.containsKey(HTTPMethod.GET) && !jwtMethods.containsKey(HTTPMethod.HEAD)) {
      jwtMethods.put(HTTPMethod.HEAD, jwtMethods.get(HTTPMethod.GET));
    }

    // All Execute Methods that require authentication need to be accounted for in JWT Methods. It is ok if the JWT Methods define a superset of the execute methods.
    Set<HTTPMethod> authenticatedMethods = executeMethods.keySet().stream().filter(k -> {
      ExecuteMethodConfiguration methodConfiguration = executeMethods.get(k);
      return methodConfiguration.annotations.containsKey(AnonymousAccess.class);
    }).collect(Collectors.toSet());

    if (!jwtMethods.keySet().containsAll(authenticatedMethods)) {
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

  protected Field findUnknownParametersField(Class<?> actionClass) {
    List<Field> unknownParameters = ReflectionUtils.findAllFieldsWithAnnotation(actionClass, UnknownParameters.class);
    if (unknownParameters.size() > 1) {
      throw new PrimeException(
          "The action class [" + actionClass + "] has more than one field annotated with " + UnknownParameters.class.getSimpleName() + ". This annotation may only be used once in the action class.");
    }

    if (unknownParameters.size() == 1) {
      return unknownParameters.get(0);
    }

    return null;
  }

  /**
   * Ensures that the method is a correct execute method.
   *
   * @param method The method.
   * @throws PrimeException If the method is invalid.
   */
  protected void verify(Method method) {
    if (method.getReturnType() != String.class || method.getParameterTypes().length != 0) {
      throw new PrimeException("The action class [" + method.getDeclaringClass() + "] has defined an " +
                               "execute method named [" + method.getName() + "] that is invalid. Execute methods must have zero parameters " +
                               "and return a String like this:\n\n" +
                               "public String execute() {\n" +
                               "  return \"success\"\n" +
                               "}");
    }
  }

  private Map<Class<?>, List<Method>> findAllPreRenderMethods(Class<?> actionClass) {
    Map<Class<?>, List<Method>> result = new HashMap<>();
    for (Method method : ReflectionUtils.findAllMethodsWithAnnotation(actionClass, PreRenderMethod.class)) {
      for (Class<?> clazz : method.getAnnotation(PreRenderMethod.class).value()) {
        // Ensure the requested result type is actually a result.
        if (clazz.getAnnotation(ResultAnnotation.class) == null) {
          throw new PrimeException(
              "The request annotation [" + clazz.getSimpleName() + "] must also have " +
              "the @ResultAnnotation annotation to be properly considered as a result class type.");
        }

        result.computeIfAbsent(clazz, key -> new ArrayList<>()).add(method);
      }
    }

    return result;
  }

  private List<String> findSecuritySchemes(Class<?> actionClass) {
    List<String> securitySchemes = new ArrayList<>(Arrays.asList(actionClass.getAnnotation(Action.class).scheme()));

    // jwtEnabled is deprecated, but if in use, add 'jwt' to the schemes list, adding it last.
    if (!securitySchemes.contains("jwt") && actionClass.getAnnotation(Action.class).jwtEnabled()) {
      securitySchemes.add(0, "jwt");
    }

    return securitySchemes;
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

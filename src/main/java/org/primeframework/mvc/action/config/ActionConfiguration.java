/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.primeframework.mvc.action.AuthorizationMethodConfiguration;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.JWTMethodConfiguration;
import org.primeframework.mvc.action.PreParameterMethodConfiguration;
import org.primeframework.mvc.action.ValidationMethodConfiguration;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.scope.ScopeField;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.validation.Validatable;

/**
 * This interface defines the public API that describes an action configuration.
 *
 * @author Brian Pontarelli
 */
public class ActionConfiguration {
  public final Class<?> actionClass;

  public final Map<Class<?>, Object> additionalConfiguration;

  public final Action annotation;

  public final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

  public final Map<HTTPMethod, List<AuthorizationMethodConfiguration>> authorizationMethods;

  public final Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods;

  public final Map<String, FileUpload> fileUploadMembers;

  public final List<Method> formPrepareMethods;

  public final Map<HTTPMethod, List<JWTMethodConfiguration>> jwtAuthorizationMethods;

  public final Set<String> memberNames;

  public final String pattern;

  public final String[] patternParts;

  public final List<Method> postParameterMethods;

  public final List<Method> postValidationMethods;

  public final Map<String, PreParameter> preParameterMembers;

  public final Map<HTTPMethod, List<PreParameterMethodConfiguration>> preParameterMethods;

  public final List<Method> preValidationMethods;

  public final Map<String, Annotation> resultConfigurations;

  public final List<ScopeField> scopeFields;

  public final List<String> securitySchemes;

  public final Field unknownParametersField;

  public final String uri;

  public final boolean validatable;

  public final Map<HTTPMethod, List<ValidationMethodConfiguration>> validationMethods;

  public ActionConfiguration(Class<?> actionClass, Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods,
                             Map<HTTPMethod, List<ValidationMethodConfiguration>> validationMethods,
                             List<Method> formPrepareMethods,
                             Map<HTTPMethod, List<AuthorizationMethodConfiguration>> authorizationMethods,
                             Map<HTTPMethod, List<JWTMethodConfiguration>> jwtAuthorizationMethods,
                             List<Method> postValidationMethods,
                             Map<HTTPMethod, List<PreParameterMethodConfiguration>> preParameterMethods,
                             List<Method> postParameterMethods, Map<String, Annotation> resultConfigurations,
                             Map<String, PreParameter> preParameterMembers, Map<String,
      FileUpload> fileUploadMembers, Set<String> memberNames, List<String> securitySchemes,
                             List<ScopeField> scopeFields, Map<Class<?>, Object> additionalConfiguration, String uri,
                             List<Method> preValidationMethods, Field unknownParametersField) {

    Objects.requireNonNull(actionClass);

    this.actionClass = actionClass;
    this.formPrepareMethods = formPrepareMethods;
    this.authorizationMethods = authorizationMethods;
    this.jwtAuthorizationMethods = jwtAuthorizationMethods;
    this.preValidationMethods = preValidationMethods;
    this.postValidationMethods = postValidationMethods;
    this.preParameterMethods = preParameterMethods;
    this.postParameterMethods = postParameterMethods;
    this.preParameterMembers = preParameterMembers;
    this.fileUploadMembers = fileUploadMembers;
    this.validationMethods = validationMethods;
    this.executeMethods = executeMethods;
    this.resultConfigurations = resultConfigurations;
    this.memberNames = memberNames;
    this.scopeFields = scopeFields;
    this.securitySchemes = securitySchemes;
    this.additionalConfiguration = additionalConfiguration;
    this.validatable = Validatable.class.isAssignableFrom(actionClass);
    this.uri = uri;
    this.annotation = actionClass.getAnnotation(Action.class);
    this.unknownParametersField = unknownParametersField;

    // Load the annotations on the class
    Annotation[] annotations = actionClass.getAnnotations();
    for (Annotation annotation : annotations) {
      this.annotations.put(annotation.annotationType(), annotation);
    }

    this.pattern = annotation.value();
    if (!pattern.equals("")) {
      this.patternParts = pattern.split("/");
    } else {
      this.patternParts = new String[0];
    }
  }
}
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

import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.ValidationMethodConfiguration;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.scope.ScopeField;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.validation.Validatable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface defines the public API that describes an action configuration.
 *
 * @author Brian Pontarelli
 */
public class ActionConfiguration {
  public final Class<?> actionClass;
  public final Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods;
  public final List<ValidationMethodConfiguration> validationMethods;
  public final List<Method> formPrepareMethods;
  public final List<Method> preValidationMethods;
  public final List<Method> postValidationMethods;
  public final List<Method> preParameterMethods;
  public final List<Method> postParameterMethods;
  public final Map<String, PreParameter> preParameterMembers;
  public final Map<String, FileUpload> fileUploadMembers;
  public final Set<String> memberNames;
  public final List<ScopeField> scopeFields;
  public final Map<Class<?>, Object> additionalConfiguration;
  public final Map<String, Annotation> resultConfigurations;
  public final boolean validatable;
  public final String uri;
  public final Action annotation;
  public final String pattern;
  public final String[] patternParts;
  public final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

  public ActionConfiguration(Class<?> actionClass, Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods,
                             List<ValidationMethodConfiguration> validationMethods, List<Method> formPrepareMethods,
                             List<Method> preValidationMethods, List<Method> postValidationMethods,
                             List<Method> preParameterMethods, List<Method> postParameterMethods,
                             Map<String, Annotation> resultConfigurations, Map<String, PreParameter> preParameterMembers,
                             Map<String, FileUpload> fileUploadMembers, Set<String> memberNames,
                             List<ScopeField> scopeFields, Map<Class<?>, Object> additionalConfiguration, String uri) {
    this.actionClass = actionClass;
    this.formPrepareMethods = formPrepareMethods;
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
    this.additionalConfiguration = additionalConfiguration;
    this.validatable = Validatable.class.isAssignableFrom(actionClass);
    this.uri = uri;
    this.annotation = actionClass.getAnnotation(Action.class);

    // Load the annotations on the class
    if (actionClass != null) {
      Annotation[] annotations = actionClass.getAnnotations();
      for (Annotation annotation : annotations) {
        this.annotations.put(annotation.annotationType(), annotation);
      }
    }

    this.pattern = annotation.value();
    if (!pattern.equals("")) {
      this.patternParts = pattern.split("/");
    } else {
      this.patternParts = new String[0];
    }
  }
}
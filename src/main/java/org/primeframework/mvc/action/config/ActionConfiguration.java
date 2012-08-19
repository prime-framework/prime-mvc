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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.action.ExecuteMethod;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.scope.ScopeField;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.validation.jsr303.Validatable;

/**
 * This interface defines the public API that describes an action configuration.
 *
 * @author Brian Pontarelli
 */
public class ActionConfiguration {
  public final Class<?> actionClass;
  public final List<Method> validationMethods;
  public final List<Method> preValidationMethods;
  public final List<Method> postValidationMethods;
  public final List<Method> preParameterMethods;
  public final List<Method> postParameterMethods;
  public final Map<String, PreParameter> preParameterMembers;
  public final Map<String, FileUpload> fileUploadMembers;
  public final Set<String> memberNames;
  public final Map<HTTPMethod, ExecuteMethod> executeMethods;
  public final List<ScopeField> scopeFields;
  public final Map<String, Annotation> resultConfigurations;
  public final boolean validatable;
  public final String uri;
  public final Action annotation;
  public final String pattern;
  public final String[] patternParts;

  public ActionConfiguration(Class<?> actionClass, Map<HTTPMethod, ExecuteMethod> executeMethods, List<Method> validationMethods,
                             Map<String, Annotation> resultConfigurations, List<Method> preValidationMethods,
                             List<Method> postValidationMethods, List<Method> preParameterMethods, List<Method> postParameterMethods,
                             Map<String, PreParameter> preParameterMembers, Map<String, FileUpload> fileUploadMembers,
                             Set<String> memberNames, List<ScopeField> scopeFields, String uri) {
    this.actionClass = actionClass;
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
    this.validatable = Validatable.class.isAssignableFrom(actionClass);
    this.uri = uri;
    this.annotation = actionClass.getAnnotation(Action.class);

    this.pattern = annotation.value();
    if (!pattern.equals("")) {
      this.patternParts = pattern.split("/");
    } else {
      this.patternParts = new String[0];
    }
  }
}
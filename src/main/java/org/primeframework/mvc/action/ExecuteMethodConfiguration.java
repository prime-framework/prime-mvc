/*
 * Copyright (c) 2012-2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.validation.Validation;

/**
 * Stores the Method and annotations for all execute methods on an action.
 *
 * @author Brian Pontarelli
 */
public class ExecuteMethodConfiguration {
  public final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

  public final HTTPMethod httpMethod;

  public final Method method;

  public final Validation validation;

  public ExecuteMethodConfiguration(HTTPMethod httpMethod, Method method, Validation validation) {
    this.httpMethod = httpMethod;
    this.method = method;
    this.validation = validation;

    // Load the annotations on the method
    if (method != null) {
      Annotation[] annotations = method.getAnnotations();
      for (Annotation annotation : annotations) {
        this.annotations.put(annotation.annotationType(), annotation);
      }
    }
  }
}

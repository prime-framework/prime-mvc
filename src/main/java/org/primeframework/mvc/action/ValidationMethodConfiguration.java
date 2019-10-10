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
package org.primeframework.mvc.action;

import java.lang.reflect.Method;

import org.primeframework.mvc.validation.ValidationMethod;

/**
 * Stores the Method and annotations for all execute methods on an action.
 *
 * @author Brian Pontarelli
 */
public class ValidationMethodConfiguration {
  public final ValidationMethod annotation;

  public final Method method;

  public ValidationMethodConfiguration(Method method, ValidationMethod annotation) {
    this.method = method;
    this.annotation = annotation;
  }
}

/*
 * Copyright (c) 2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.fusionauth.http.HTTPValues.Methods;
import org.primeframework.mvc.action.annotation.Action;

/**
 * Allows for a different set of constraint at the method level of an action. If constraints have been defined by the {@link Action} annotation, using
 * this on a method will take precedence.
 *
 * @author Daniel DeGroff
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConstraintOverrideMethod {

  /**
   * @return The HTTP methods to execute the constraint override method for. Defaults to POST, PUT, PATCH, DELETE and GET.
   */
  String[] httpMethods() default {Methods.POST, Methods.PUT, Methods.PATCH, Methods.DELETE, Methods.GET};
}

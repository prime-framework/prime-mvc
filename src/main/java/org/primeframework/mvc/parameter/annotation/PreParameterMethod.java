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
package org.primeframework.mvc.parameter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.primeframework.mvc.servlet.HTTPMethod;

/**
 * This annotation defines a method that is invoked prior to the parameters being set into the action. This is useful
 * if you need to setup objects so that the parameters can be set into them.
 *
 * @author Brian Pontarelli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreParameterMethod {
  /**
   * The HTTP methods that this is called for. THe default is all of the methods.
   *
   * @return All methods are the default.
   */
  HTTPMethod[] httpMethods() default {HTTPMethod.CONNECT, HTTPMethod.DELETE, HTTPMethod.GET, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PATCH, HTTPMethod.POST, HTTPMethod.PUT, HTTPMethod.TRACE};
}

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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.servlet.HTTPMethod;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * HTTP Methods <code>POST</code>, <code>PUT</code>, <code>DELETE</code> and <code>GET</code> all need
 * to be authorized. This means the action needs to ensure coverage for all HTTP Methods. A single method may cover all four methods, or
 * they can be divided to simplify logic.
 * <p/>
 * Required when {@link Action#scheme()} or {@link Action#schemes()} contains <code>authorize-method</code>scheme.
 *
 * @author Daniel DeGroff
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface AuthorizeMethod {

  /**
   * @return The HTTP methods to execute the authorization method for. Defaults to POST, PUT, DELETE and GET.
   */
  HTTPMethod[] httpMethods() default {HTTPMethod.POST, HTTPMethod.PUT, HTTPMethod.DELETE, HTTPMethod.GET};
}
/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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

import io.fusionauth.http.HTTPValues.Methods;
import org.primeframework.mvc.action.annotation.Action;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * When {@link Action#jwtEnabled()} is set to true, <code>POST</code>, <code>PUT</code>, <code>DELETE</code> and
 * <code>GET</code> all need to be authorized. This means the action needs to ensure coverage for all HTTP Methods. A
 * single method may cover all four methods, or they can be divided to simplify logic.
 * <p/>
 * Required when {@link Action#jwtEnabled()} is set to true. This method will be called and expect a true or false
 * return value to indicate if the request is authorized.
 *
 * @author Daniel DeGroff
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface JWTAuthorizeMethod {

  /**
   * @return The HTTP methods to execute the authorization method for. Defaults to POST, PUT, DELETE and GET.
   */
  String[] httpMethods() default {Methods.POST, Methods.PUT, Methods.DELETE, Methods.GET};
}

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
package org.primeframework.mvc.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a class as being a Prime MVC action.
 *
 * @author Brian Pontarelli
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Action {
  /**
   * @return the required roles for this action to be executed.
   */
  String[] constraints() default {};

  /**
   * Set this to true to enable JWT authorization. If JWT is enabled and a JWT Authorization header is sent in on the request the JWT
   * Security Scheme will be utilized instead of the specified security scheme.
   *
   * @return true if this action allows JWT authorization.
   * @deprecated Use "jwt" as a security scheme. For example: <code>scheme = {"jwt", "api"}</code>.
   */
  @Deprecated
  boolean jwtEnabled() default false;

  /**
   * An optional namespace for this action. Similar to the prefixParameter, but this must be at the front of the URI.
   *
   * @return the namespace parameters.
   */
  String namespace() default "";

  /**
   * Determines if the action can be overridden by another action that maps to the same URI. If a class that is marked
   * as overridable and another another class is found for the same URI but is not marked as overridable, that one is
   * used.
   *
   * @return True of false.
   */
  boolean overridable() default false;

  /**
   * Parameters in the URI that are before the URI segment for the action itself.
   *
   * @return The parameters.
   */
  String prefixParameters() default "";

  /**
   * @return If this action requires authentication.
   */
  boolean requiresAuthentication() default false;

  /**
   * @return The security scheme(s) that handle authentication and authorization for this action. These are applied in order and the
   * constraints are offered to each scheme.
   */
  String[] scheme() default {"user"};

  /**
   * @return The value of the action annotation is used to determines the URI suffix patterns that the action class can
   * handle. This is also known as RESTful URI handling. The pattern is derived from the current WADL specification
   * from Sun. The base URI for the action is fixed based on the package and class name. However, everything after the
   * base can be set into properties or fields of the action class using the WADL pattern here. The pattern is like
   * this:
   * <p>
   * {@code {id}}
   * <p>
   * If the classes base URI is /admin/user/edit, the full specification for the URI that action can handle would be:
   * <p>
   * {@code {/admin/user/edit/{id}}
   * <p>
   * If the URI is <strong>/admin/user/edit/42</strong>, the value of 42 would be added to the HTTP request parameters
   * under the key <strong>id</strong>. In most cases this means that the value will also be set into the action, but
   * it could also be used as a {@link org.primeframework.mvc.parameter.annotation.PreParameter}.
   */
  String value() default "";
}
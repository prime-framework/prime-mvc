/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.scope.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.primeframework.mvc.http.Cookie.SameSite;
import org.primeframework.mvc.scope.ManagedSessionCookieScope;

/**
 * Just your typical basic cookie. This annotation should be used to mark member fields of actions that should be
 * fetched and stored in a session Cookie.
 * <p>
 * This cookie will be written as an HTTP-only, Session cookie that is Secure based on the scheme of the request.
 *
 * @author Daniel DeGroff
 */
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation(ManagedSessionCookieScope.class)
@Target(ElementType.FIELD)
public @interface ManagedSessionCookie {
  /**
   * When `true` the cookie value will be compressed. The default is false.
   *
   * @return True if the cookie value should be compressed.
   */
  boolean compress() default false;

  /**
   * When `true` the cookie value will be encrypted, this is the default.
   *
   * @return true if the cookie value should be encrypted.
   */
  boolean encrypt() default true;

  /**
   * @return This attribute determines the name under which that the value is stored in the action session. The default
   *     name is the name of the field that the annotation is put on.
   */
  String name() default "##field-name##";

  /**
   * By default, the annotated field will always be set, and the value of the cookie will be <code>null</code> if the
   * cookie was not found in the request.
   * <p>
   * Optionally set this to false if you want the field in the action to remain <code>null</code> when the cookie is not
   * found in the action.
   *
   * @return true if the cookie object should never be null
   */
  boolean neverNull() default true;

  /**
   * @return The SameSite setting for the cookie. Defaults to Lax.
   */
  SameSite sameSite() default SameSite.Lax;
}
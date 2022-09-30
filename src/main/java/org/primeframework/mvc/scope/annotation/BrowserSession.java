/*
 * Copyright (c) 2021-2022, Inversoft Inc., All Rights Reserved
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

import io.fusionauth.http.Cookie.SameSite;
import org.primeframework.mvc.scope.BrowserSessionScope;

/**
 * Just your typical basic cookie. This annotation should be used to mark member fields of actions that should be
 * fetched and stored in a secure Cookie by marshalling and unmarshalling the field to and from JSON.
 * <p>
 * This cookie will be written as an HTTP only Persistent, Secure cookie. If the default maxAge is used, then the value
 * will be stored in a session cookie, which is deleted when the browser is closed.
 *
 * @author Brian Pontarelli
 */
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation(BrowserSessionScope.class)
@Target(ElementType.FIELD)
public @interface BrowserSession {
  /**
   * When `true` the cookie value will be compressed, this is the default.
   *
   * @return true if the cookie value should be compressed.
   */
  boolean compress() default true;

  /**
   * When `true` the cookie value will be encrypted, this is the default.
   *
   * @return true if the cookie value should be encrypted.
   */
  boolean encrypt() default true;

  /**
   * @return This attribute determines the name of the cookie. The default name is the name of the field that the
   *     annotation is put on.
   */
  String name() default "##field-name##";

  /**
   * @return The SameSite setting for the cookie. Defaults to Lax.
   */
  SameSite sameSite() default SameSite.Lax;
}
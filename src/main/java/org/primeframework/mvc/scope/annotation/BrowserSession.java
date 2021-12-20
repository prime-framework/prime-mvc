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

import org.primeframework.mvc.scope.BrowserSessionScope;

/**
 * Just your typical basic cookie. This annotation should be used to mark member fields of actions that should be
 * fetched and stored in a secure Cookie by marshalling and unmarshalling the field to and from JSON.
 * <p>
 * This cookie will be written as an HTTP only Persistent, Secure cookie.
 *
 * @author Brian Pontarelli
 */
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation(BrowserSessionScope.class)
@Target(ElementType.FIELD)
public @interface BrowserSession {
  /**
   * When `true` the cookie value will be encrypted, this is the default.
   *
   * @return true if the cookie value should be encrypted.
   */
  boolean encrypt() default true;

  /**
   * Optionally specify a value to set on the cookie for Max-Age
   *
   * @return the max age value
   */
  long maxAge() default Long.MAX_VALUE;

  /**
   * @return This attribute determines the name of the cookie. The default name is the name of the field that the
   *     annotation is put on.
   */
  String value() default "##field-name##";
}
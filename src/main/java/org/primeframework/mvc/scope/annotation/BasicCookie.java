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

import org.primeframework.mvc.scope.BasicCookieScope;

/**
 * Just your typical basic cookie. This annotation should be used to mark member fields of actions that should be
 * fetched and stored in a secure Cookie.
 * <p>
 * This cookie will be written as an HTTP only Persistent, Secure cookie.
 *
 * @author Daniel DeGroff
 */
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation(BasicCookieScope.class)
@Target(ElementType.FIELD)
public @interface BasicCookie {
  /**
   * When true, even if the value is a string, it will be base64 encoded. When false, if the value is a string it will
   * not be encoded. This is ignored when encrypting because we will always encode then. Also if you annotate a field
   * that is not a string, it will always be encoded after the object is serialized.
   *
   * @return true if plain strings should be encoded.
   */
  boolean encodeStrings() default false;

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
  String value() default "##field-name##";
}
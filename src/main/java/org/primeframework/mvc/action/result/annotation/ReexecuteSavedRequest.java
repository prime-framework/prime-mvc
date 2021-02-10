/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks a result from an action as a Saved Request redirect.
 *
 * @author Brian Pontarelli
 */
@ResultAnnotation
@Retention(RUNTIME)
@Target(TYPE)
public @interface ReexecuteSavedRequest {
  /**
   * @return the value to use for the <code>Cache-Control</code> header.
   */
  String cacheControl() default "no-cache";

  /**
   * @return The result code from the action's execute method that this Result is associated with.
   */
  String code() default "success";

  /**
   * @return set to true to disable cache control and manage the headers on your own.
   */
  boolean disableCacheControl() default false;

  /**
   * @return Whether or not variable replacements inside the URI string should be encoded or not. In some cases, you
   *     want to encode variables when they contain UTF-8 characters and are part of the URL query parameters. For
   *     example, "/foo?user=${bar}" and the bar variable contains unicode characters. In other cases, you don't want to
   *     encode the variables. For example, if they variable contains the entire URI such as "${uri}". This defaults to
   *     false to maintain backwards compatibility.
   */
  boolean encodeVariables() default false;

  /**
   * @return Whether or not this is a permanent redirect (301) or a temporary redirect (302).
   */
  boolean perm() default false;

  /**
   * @return The redirect URI to use if there is no Saved Request.
   */
  String uri();

  /**
   * A list of ReexecuteSavedRequest annotations.
   */
  @ResultContainerAnnotation
  @Retention(RUNTIME)
  @Target(TYPE)
  @interface List {
    ReexecuteSavedRequest[] value();
  }
}
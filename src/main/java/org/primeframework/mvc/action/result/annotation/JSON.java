/*
 * Copyright (c) 2001-2022, Inversoft Inc., All Rights Reserved
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

import org.primeframework.mvc.content.json.annotation.JSONResponse;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks a result from an action as JSON. This uses Jackson to convert the Java Object to JSON. This
 * uses the {@link JSONResponse} annotation to locate the member of the action that contains the response.
 *
 * @author Brian Pontarelli
 */
@ResultAnnotation
@Retention(RUNTIME)
@Target(TYPE)
public @interface JSON {
  /**
   * @return the value to use for the <code>Cache-Control</code> header.
   */
  String cacheControl() default "no-cache";

  /**
   * @return The result code from the action's execute method that this Result is associated with.
   */
  String code() default "success";

  /**
   * Optionally override the Content-Type that is either already set or defaulted based upon the Result handler. Leave
   * the default value if you do not wish to override the Content-Type header.
   *
   * @return the value to be written as the Content-Type for the HTTP response.
   */
  String contentType() default "";

  /**
   * @return set to true to disable cache control and manage the headers on your own.
   */
  boolean disableCacheControl() default false;

  /**
   * @return The HTTP status code.
   */
  int status() default 200;

  /**
   * A list of JSON annotations.
   */
  @ResultContainerAnnotation
  @Retention(RUNTIME)
  @Target(TYPE)
  @interface List {
    JSON[] value();
  }
}

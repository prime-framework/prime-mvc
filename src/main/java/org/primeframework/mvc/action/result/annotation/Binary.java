/*
 * Copyright (c) 2016-2019, Inversoft Inc., All Rights Reserved
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

import org.primeframework.mvc.content.binary.annotation.BinaryResponse;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks a result from an action as a binary file. This uses the {@link BinaryResponse} annotation to
 * locate the member of the action that contains the response object to be written to the output stream.
 *
 * @author Daniel DeGroff
 */
@ResultAnnotation
@Retention(RUNTIME)
@Target(TYPE)
public @interface Binary {
  /**
   * @return the value to use for the <code>Cache-Control</code> header.
   */
  String cacheControl() default "no-store";

  /**
   * @return The result code from the action's execute method that this Result is associated with.
   */
  String code() default "success";

  /**
   * @return set to true to disable cache control and manage the headers on your own.
   */
  boolean disableCacheControl() default false;

  /**
   * @return The HTTP status code.
   */
  int status() default 200;

  /**
   * @return The content type of the InputStream. This is used to set the HTTP header and disposition so that the
   *     browser can correctly handle the response.
   */
  String type() default "application/octet-stream";
}

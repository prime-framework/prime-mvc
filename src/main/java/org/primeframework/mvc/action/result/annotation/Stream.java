/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
 * This annotation marks a result from an action as a Streaming result using an InputStream from the action.
 *
 * @author Brian Pontarelli
 */
@ResultAnnotation
@Retention(RUNTIME)
@Target(TYPE)
public @interface Stream {
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
   * @return The name of the property that contains a ZonedDateTime for the last modified header.
   */
  String lastModifiedProperty() default "lastModified";

  /**
   * @return The content length sent back to the client. This is used to set the content length header so that the
   *     browser displays a progress bar when downloading the file. This defaults to <code>${length}</code>, which means
   *     that its value is set dynamically using the value from the <code>length</code> property of the action.
   */
  String length() default "${length}";

  /**
   * @return The file name sent back to the client. This is used to set the content disposition header so that the
   *     browser displays the correct name when saving the response to a file. This defaults to
   *     <code>${name}</code>, which means that its value is set dynamically using the value from the
   *     <code>name</code> property of the action.
   */
  String name() default "${name}";

  /**
   * @return The name of the property of the action that returns an InputStream. The bytes from this InputStream are
   *     sent back to the browser via the ServletOutputStream. This defaults to <code>stream</code>, which means the
   *     getStream method of the action should return the InputStream.
   */
  String property() default "stream";

  /**
   * @return The HTTP status code of the response. This defaults to <code>200</code>.
   */
  int status() default 200;

  /**
   * @return The content type of the InputStream. This is used to set the HTTP header and disposition so that the
   *     browser can correctly handle the response. This defaults to <code>${type}</code>, which means that its value is
   *     set dynamically using the value from the <code>type</code> property of the action.
   */
  String type() default "${type}";

  /**
   * A list of Stream annotations.
   */
  @ResultContainerAnnotation
  @Retention(RUNTIME)
  @Target(TYPE)
  @interface List {
    Stream[] value();
  }
}
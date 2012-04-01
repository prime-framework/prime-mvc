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

import org.primeframework.mvc.action.result.StreamResult;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * This annotation marks a result from an action as a Streaming result using an InputStream from the action.
 *
 * @author Brian Pontarelli
 */
@ResultAnnotation(StreamResult.class)
@Retention(RUNTIME)
@Target(TYPE)
public @interface Stream {
  /**
   * @return The result code from the action's execute method that this Result is associated with.
   */
  String code() default "success";

  /**
   * @return The content type of the InputStream. This is used to set the HTTP header and disposition so that the
   *         browser can correctly handle the response. This defaults to <code>${type}</code>, which means that its
   *         value is set dynamically using the value from the <code>type</code> property of the action.
   */
  String type() default "${type}";

  /**
   * @return The file name sent back to the client. This is used to set the content disposition header so that the
   *         browser displays the correct name when saving the response to a file. This defaults to
   *         <code>${name}</code>, which means that its value is set dynamically using the value from the
   *         <code>name</code> property of the action.
   */
  String name() default "${name}";

  /**
   * @return The content length sent back to the client. This is used to set the content length header so that the
   *         browser displays a progress bar when downloading the file. This defaults to <code>${length}</code>, which
   *         means that its value is set dynamically using the value from the <code>length</code> property of the
   *         action.
   */
  String length() default "${length}";

  /**
   * @return The name of the property of the action that returns an InputStream. The bytes from this InputStream are
   *         sent back to the browser via the ServletOutputStream. This defaults to <code>stream</code>, which means the
   *         getStream method of the action should return the InputStream.
   */
  String property() default "stream";

  /**
   * A list of Stream annotations.
   */
  @ResultContainerAnnotation
  @Retention(RUNTIME)
  @Target(TYPE)
  public static @interface List {
    Stream[] value();
  }
}
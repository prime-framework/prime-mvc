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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * This annotation marks a result from an action as a Status result. Status results can have a status code, status
 * string, and a set of additional headers to return. This result never returns a body.
 *
 * @author Brian Pontarelli
 */
@ResultAnnotation
@Retention(RUNTIME)
@Target(TYPE)
public @interface Status {
  /**
   * @return The result code from the action's execute method that this Result is associated with.
   */
  String code() default "success";

  /**
   * @return The status code.
   */
  int status() default 200;

  /**
   * @return Overrides the status parameter. If this is set, Prime use the value of this parameter and first expand it.
   *         It uses the <code>${variable}</code> notation that is common for variable expanders. After it has been
   *         expanded, the result is converted into an int. Therefore, you can specify either a number as a String, or a
   *         variable expansion. Here are some examples: <code>"${myStatus}"</code>, <code>"200"</code>,
   *         <code>"40${someField}"</code>
   */
  String statusStr() default "";

  /**
   * @return A list of headers to return as part of the HTTP response. See the W3 HTTP 1.1 specification for a list of
   *         headers.
   */
  Header[] headers() default {};

  /**
   * A list of Status annotations.
   */
  @ResultContainerAnnotation
  @Retention(RUNTIME)
  @Target(TYPE)
  public static @interface List {
    Status[] value();
  }

  /**
   * A header that has a name and value.
   */
  public static @interface Header {
    /**
     * @return The name of the header.
     */
    String name();

    /**
     * @return The value of the header.
     */
    String value();
  }
}

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

import org.primeframework.mvc.content.json.annotation.JSONResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
   * @return The result code from the action's execute method that this Result is associated with.
   */
  String code() default "success";

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
  public static @interface List {
    JSON[] value();
  }
}

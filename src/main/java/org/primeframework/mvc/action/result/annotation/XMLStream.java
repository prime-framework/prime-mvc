/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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
 * This annotation marks a result from an action as a Streaming XML result using a xml string from the action.
 *
 * @author jhumphrey
 */
@ResultAnnotation
@Retention(RUNTIME)
@Target(TYPE)
public @interface XMLStream {
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
   * @return The name of the property of the action that represents an XML string.
   */
  String property() default "xml";

  /**
   * @return The HTTP status code.
   */
  int status() default 200;

  /**
   * A list of XMLStream annotations.
   */
  @ResultContainerAnnotation
  @Retention(RUNTIME)
  @Target(TYPE)
  @interface List {
    XMLStream[] value();
  }
}

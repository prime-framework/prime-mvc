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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.primeframework.mvc.action.result.RedirectResult;

/**
 * This annotation marks a result from an action as a redirect.
 *
 * @author Brian Pontarelli
 */
@ResultAnnotation(RedirectResult.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Redirect {
  /**
   * @return The result code from the action's execute method that this Result is associated with.
   */
  String code() default "success";

  /**
   * @return The redirect URI.
   */
  String uri();

  /**
   * @return Whether or not this is a permanent redirect (301) or a temporary redirect (302).
   */
  boolean perm() default false;

  /**
   * @return Whether or not variable replacements inside the URI string should be encoded or not. In some cases, you
   *         want to encode variables when they contain UTF-8 characters and are part of the URL query parameters. For
   *         example, "/foo?user=${bar}" and the bar variable contains unicode characters. In other cases, you don't
   *         want to encode the variables. For example, if they variable contains the entire URI such as "${uri}". This
   *         defaults to false to maintain backwards compatibility.
   */
  boolean encodeVariables() default false;
}
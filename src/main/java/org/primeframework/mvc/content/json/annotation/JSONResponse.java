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
package org.primeframework.mvc.content.json.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a member as the storage of a JSON data object that will be bound by Jackson for the response.
 *
 * @author Brian Pontarelli
 */
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface JSONResponse {
  /**
   * View or views that annotated element is part of. Views are identified
   * by classes, and use expected class inheritance relationship: child
   * views contain all elements parent views have, for example.
   */
  Class<?> value() default Void.class;
}
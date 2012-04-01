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
package org.primeframework.mvc.control.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * This class defines a single attribute that a control can have passed to it. Since FreeMarker doesn't provide a
 * mechanism for ensuring directives have the correct attributes or types, this annotation is used instead.
 *
 * @author Brian Pontarelli
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ControlAttribute {
  /**
   * @return The name of the attribute.
   */
  String name();

  /**
   * @return The types that can be passed for this attribute.
   */
  Class<?>[] types() default {String.class};
}
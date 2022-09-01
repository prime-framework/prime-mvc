/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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

import com.fasterxml.jackson.databind.ser.PropertyFilter;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Use this to annotate a method that provides a {@link PropertyFilter} for use when serializing the field annotated
 * with {@link JSONResponse}
 *
 * @author Daniel DeGroff
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface JSONPropertyFilter {
  /**
   * @return the name of the filter to bind to a filter provider. <br>For example, when setting the value to
   *     <code>"namedFilter"</code>, the domain object to filter needs to be annotated with
   *     <code>@JsonFilter("namedFilter")</code>.
   */
  String value();
}

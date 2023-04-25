/*
 * Copyright (c) 2023, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.primeframework.mvc.util.ReflectionUtils;

/**
 * This annotation allows you to change the name of the field and set a logical property name it relates to how prime will evaluate fields and methods
 * in an action using {@link ReflectionUtils}.
 * <p>
 * For example, if you need to accept a request parameter that does not follow bean spec naming such as 'x-field', you can rename this field so that
 * prime will set this field value based upon a request parameter  named 'x-field'.
 * <p>
 * When used on a field, this will effectively change the field name when accessed using {@link ReflectionUtils}.
 * <p>
 * When used on a method, the property name will be assumed to be a setter. For example, the logical name of 'foo' will be interpreted as a method
 * named 'setFoo'.
 *
 * @author Lyle Schemmerling
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface FieldName {
  /**
   * @return the name of the logical property.
   */
  String value();
}

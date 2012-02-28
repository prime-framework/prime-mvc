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
package org.primeframework.mvc.validation;

import java.lang.annotation.Annotation;

/**
 * <p> This interface defines a validator. </p>
 *
 * @author Brian Pontarelli
 */
public interface Validator<T extends Annotation> {
  /**
   * Validates the given value against the given annotation.
   *
   * @param annotation The annotation that is associated with this validator and that was placed on a bean property or
   *                   field.
   * @param container  This is the Object that contains the bean property or field being validated. This is useful when
   *                   a specific validation is dependent on other properties of an Object. For example, an address
   *                   might have special validator on the state property that ensures the state is only specified for
   *                   countries that have states.
   * @param value      The bean property or field value.
   * @return True if the validation passes, false otherwise.
   */
  boolean validate(T annotation, Object container, Object value);
}
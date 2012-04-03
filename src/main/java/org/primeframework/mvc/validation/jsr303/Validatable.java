/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.validation.jsr303;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Marks an action as handling its own validation.
 *
 * @author Brian Pontarelli
 */
public interface Validatable<T> {
  /**
   * Called by Prime instead of using the default handling and group determination. This allows an action to handle
   * validation however it wants and determine the groups however it chooses.
   *
   * @param validator The JSR validator.
   * @return Any constraint violations.
   */
  Set<ConstraintViolation<T>> validate(Validator validator);
}

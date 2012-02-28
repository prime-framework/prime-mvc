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
package org.primeframework.mvc.scope;

import java.lang.annotation.Annotation;

/**
 * <p> This interface defines the handler for a specific scope. </p>
 *
 * @author Brian Pontarelli
 */
public interface Scope<T extends Annotation> {
  /**
   * Retrieve the value from the scope based on the name given.
   *
   * @param fieldName The name of the field.
   * @param scope     The scope annotation from the field.
   * @return The value or null if it doesn't exist in the scope.
   */
  Object get(String fieldName, T scope);

  /**
   * Sets the value into the scope.
   *
   * @param fieldName The name to store the value under in the scope.
   * @param scope     The scope annotation from the field.
   * @param value     The value.
   */
  void set(String fieldName, Object value, T scope);
}
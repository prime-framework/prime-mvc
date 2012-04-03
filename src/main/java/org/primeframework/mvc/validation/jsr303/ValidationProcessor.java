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
package org.primeframework.mvc.validation.jsr303;

import javax.validation.ConstraintViolation;
import java.util.Set;

import org.primeframework.mvc.validation.ValidationException;

import com.google.inject.ImplementedBy;

/**
 * This interface defines how Prime handles validation.
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(JSRValidationProcessor.class)
public interface ValidationProcessor {
  /**
   * Validates the request and if validation fails throws a ValidationException.
   *
   * @throws ValidationException If the validation failed.
   */
  void validate() throws ValidationException;

  /**
   * Handles violations that were produced elsewhere.
   *
   * @param violations The violations.
   */
  void handle(Set<ConstraintViolation<Object>> violations);
}
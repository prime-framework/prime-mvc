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
package org.primeframework.mvc.validation;

import javax.validation.ConstraintViolation;
import java.util.Set;

import org.primeframework.mvc.ErrorException;

/**
 * Thrown when validation fails.
 *
 * @author Brian Pontarelli
 */
public class ValidationException extends ErrorException {
  public Set<ConstraintViolation<Object>> violations;

  public ValidationException() {
    this((Set<ConstraintViolation<Object>>) null);
  }

  public ValidationException(String message) {
    this(null, message);
  }

  public ValidationException(String message, Throwable cause) {
    this(null, message, cause);
  }

  public ValidationException(Throwable cause) {
    this((Set<ConstraintViolation<Object>>) null, cause);
  }

  public ValidationException(Set<ConstraintViolation<Object>> violations) {
    super("input");
    this.violations = violations;
  }

  public ValidationException(Set<ConstraintViolation<Object>> violations, String message) {
    super("input", message);
    this.violations = violations;
  }

  public ValidationException(Set<ConstraintViolation<Object>> violations, String message, Throwable cause) {
    super("input", message, cause);
    this.violations = violations;
  }

  public ValidationException(Set<ConstraintViolation<Object>> violations, Throwable cause) {
    super("input", cause);
    this.violations = violations;
  }
}

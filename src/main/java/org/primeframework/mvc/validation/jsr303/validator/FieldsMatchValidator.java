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
package org.primeframework.mvc.validation.jsr303.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.validation.jsr303.constraint.FieldsMatch;

/**
 * Validates that if wo fields are equal.
 *
 * @author James Humphrey
 */
public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {
  private FieldsMatch match;

  @Override
  public void initialize(FieldsMatch constraintAnnotation) {
    if (constraintAnnotation.fields().length <= 1) {
      throw new PrimeException("The FieldsMatch annotation must contain at least two fields");
    }

    this.match = constraintAnnotation;
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext context) {
    try {
      boolean valid = true;
      Object value = null;
      for (String fieldName : match.fields()) {
        Field field = object.getClass().getDeclaredField(fieldName);

        Object fieldValue = field.get(object);
        if (value == null) {
          // Don't handle null cases for the first value
          if (fieldValue == null) {
            return true;
          }

          value = fieldValue;
        } else if (fieldValue == null || !value.equals(fieldValue)) {
          context.disableDefaultConstraintViolation();
          context.buildConstraintViolationWithTemplate("").addNode(fieldName).addConstraintViolation();
          valid = false;
        }
      }

      return valid;
    } catch (NoSuchFieldException e) {
      throw new PrimeException("You must declare the fields specified in the annotation.");
    } catch (IllegalAccessException e) {
      throw new PrimeException("Unable to access the fields specified in the annotation.");
    }
  }
}

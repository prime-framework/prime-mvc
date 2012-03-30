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

import org.apache.commons.lang3.StringUtils;
import org.primeframework.mvc.validation.jsr303.constraint.Unicode;

/**
 * Validates that the action-actionKey combination is unique
 *
 * @author James Humphrey
 */
public class UnicodeValidator implements ConstraintValidator<Unicode, String> {

  @Override
  public void initialize(Unicode constraintAnnotation) {
  }

  @Override
  public boolean isValid(String string, ConstraintValidatorContext context) {
    return StringUtils.isBlank(string) || !containsSupplementaryPlaneCharacters(string);
  }

  private boolean containsSupplementaryPlaneCharacters(String str) {
    for (int i = 0; i < str.length(); i++) {
      if (Character.isSupplementaryCodePoint(str.codePointAt(i))) {
        return true;
      }
    }

    return false;
  }
}

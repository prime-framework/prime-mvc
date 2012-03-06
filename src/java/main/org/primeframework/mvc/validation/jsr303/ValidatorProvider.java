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

import javax.validation.ConstraintValidatorFactory;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provides a JSR 303 Validator
 *
 * @author James Humphrey
 */
public class ValidatorProvider implements Provider<Validator> {
  private final ConstraintValidatorFactory constraintValidatorFactory;

  @Inject
  public ValidatorProvider(ConstraintValidatorFactory constraintValidatorFactory) {
    this.constraintValidatorFactory = constraintValidatorFactory;
  }

  @Override
  public Validator get() {
    ValidatorFactory factory = Validation.
            byProvider(HibernateValidator.class).
            configure().
            constraintValidatorFactory(constraintValidatorFactory).
            buildValidatorFactory();
    return factory.getValidator();
  }
}

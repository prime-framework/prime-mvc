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
package org.primeframework.mvc.validation.guice;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.Validator;

import org.primeframework.mvc.validation.ValidationWorkflow;
import org.primeframework.mvc.validation.jsr303.DefaultConstraintValidatorFactory;
import org.primeframework.mvc.validation.jsr303.DefaultGroupLocator;
import org.primeframework.mvc.validation.jsr303.GroupLocator;
import org.primeframework.mvc.validation.jsr303.JSRValidationProcessor;
import org.primeframework.mvc.validation.jsr303.JSRValidationWorkflow;
import org.primeframework.mvc.validation.jsr303.ValidationProcessor;
import org.primeframework.mvc.validation.jsr303.ValidatorProvider;

import com.google.inject.AbstractModule;

/**
 * Validation module for Guice.
 *
 * @author Brian Pontarelli
 */
public class ValidationModule extends AbstractModule {
  @Override
  protected void configure() {
    bindValidator();
    bindConstraintValidatorFactory();
    bindValidationWorkflow();
    bindGroupLocator();
    bindValidationProcessor();
  }

  protected void bindValidator() {
    bind(Validator.class).toProvider(ValidatorProvider.class).asEagerSingleton();
  }

  protected void bindConstraintValidatorFactory() {
    bind(ConstraintValidatorFactory.class).to(DefaultConstraintValidatorFactory.class);
  }

  protected void bindValidationWorkflow() {
    bind(ValidationWorkflow.class).to(JSRValidationWorkflow.class);
  }

  protected void bindGroupLocator() {
    bind(GroupLocator.class).to(DefaultGroupLocator.class);
  }

  protected void bindValidationProcessor() {
    bind(ValidationProcessor.class).to(JSRValidationProcessor.class);
  }
}

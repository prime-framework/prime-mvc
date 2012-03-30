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

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;

import static org.easymock.EasyMock.*;

/**
 * @author James Humphrey
 */
public class BaseValidatorUnitTest {

  /**
   * Builds a constraint validator context.  This is used in validators that are validating class level constraints
   *
   * @param propertyPath the property path
   * @return the constraint validator context
   */
  protected ConstraintValidatorContext makeContext(String propertyPath) {
    ConstraintValidatorContext context = createStrictMock(ConstraintValidatorContext.class);

    ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext definedContext = createStrictMock(NodeBuilderDefinedContext.class);
    expect(definedContext.addConstraintViolation()).andReturn(context);
    replay(definedContext);

    ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder = createStrictMock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    expect(violationBuilder.addNode(propertyPath)).andReturn(definedContext);
    replay(violationBuilder);

    context.disableDefaultConstraintViolation();
    expect(context.buildConstraintViolationWithTemplate("")).andReturn(violationBuilder);
    replay(context);

    return context;
  }
}

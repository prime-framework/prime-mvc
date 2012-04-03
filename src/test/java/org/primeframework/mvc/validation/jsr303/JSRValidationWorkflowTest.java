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
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.HashSet;
import java.util.Set;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.validation.ValidationException;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * Test the validation workflow.
 *
 * @author Brian Pontarelli
 */
public class JSRValidationWorkflowTest extends PrimeBaseTest {
  @Test
  public void success() throws Exception {
    ValidationProcessor validationProcessor = createStrictMock(ValidationProcessor.class);
    validationProcessor.validate();
    replay(validationProcessor);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    JSRValidationWorkflow workflow = new JSRValidationWorkflow(validationProcessor);
    workflow.perform(chain);
    
    verify(validationProcessor, chain);
  }

  @Test
  public void failureWithoutConstraintViolations() throws Exception {
    ValidationProcessor validationProcessor = createStrictMock(ValidationProcessor.class);
    validationProcessor.validate();
    expectLastCall().andThrow(new ValidationException());
    validationProcessor.handle(null);
    replay(validationProcessor);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    JSRValidationWorkflow workflow = new JSRValidationWorkflow(validationProcessor);
    try {
      workflow.perform(chain);
      fail("Should have thrown an exception");
    } catch (ValidationException e) {
      // Expected
    }

    verify(validationProcessor, chain);
  }

  @Test
  public void failureWithConstraintViolations() throws Exception {
    Set<ConstraintViolation<Object>> violations = new HashSet<ConstraintViolation<Object>>();
    violations.add(new TestConstraintViolation());

    ValidationProcessor validationProcessor = createStrictMock(ValidationProcessor.class);
    validationProcessor.validate();
    expectLastCall().andThrow(new ValidationException(violations));
    validationProcessor.handle(violations);
    replay(validationProcessor);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    JSRValidationWorkflow workflow = new JSRValidationWorkflow(validationProcessor);
    try {
      workflow.perform(chain);
      fail("Should have thrown an exception");
    } catch (ValidationException e) {
      // Expected
    }

    verify(validationProcessor, chain);
  }

  private class TestConstraintViolation implements ConstraintViolation<Object> {
    @Override
    public String getMessage() {
      return null;
    }

    @Override
    public String getMessageTemplate() {
      return null;
    }

    @Override
    public Object getRootBean() {
      return null;
    }

    @Override
    public Class<Object> getRootBeanClass() {
      return null;
    }

    @Override
    public Object getLeafBean() {
      return null;
    }

    @Override
    public Path getPropertyPath() {
      return null;
    }

    @Override
    public Object getInvalidValue() {
      return null;
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
      return null;
    }
  }
}
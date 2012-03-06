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
package org.primeframework.mvc.validation;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * Test the validation workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultValidationWorkflowTest extends PrimeBaseTest {
  @Test
  public void success() throws Exception {
    ValidationProcessor validationProcessor = createStrictMock(ValidationProcessor.class);
    validationProcessor.validate();
    replay(validationProcessor);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    DefaultValidationWorkflow workflow = new DefaultValidationWorkflow(validationProcessor);
    workflow.perform(chain);
    
    verify(validationProcessor, chain);
  }

  @Test
  public void failure() throws Exception {
    ValidationProcessor validationProcessor = createStrictMock(ValidationProcessor.class);
    validationProcessor.validate();
    expectLastCall().andThrow(new ValidationException());
    replay(validationProcessor);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    DefaultValidationWorkflow workflow = new DefaultValidationWorkflow(validationProcessor);
    workflow.perform(chain);

    verify(validationProcessor, chain);
  }
}
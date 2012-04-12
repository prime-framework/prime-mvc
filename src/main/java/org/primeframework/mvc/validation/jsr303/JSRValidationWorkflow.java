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

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;

import org.primeframework.mvc.validation.ValidationException;
import org.primeframework.mvc.validation.ValidationWorkflow;
import org.primeframework.mvc.workflow.ErrorWorkflow;
import org.primeframework.mvc.workflow.SubWorkflowChain;
import org.primeframework.mvc.workflow.Workflow;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * This workflow performs all the validation on the current action.
 *
 * @author Brian Pontarelli
 */
public class JSRValidationWorkflow implements ValidationWorkflow {
  private final ValidationProcessor validationProcessor;
  private final ErrorWorkflow errorWorkflow;

  @Inject
  public JSRValidationWorkflow(ValidationProcessor validationProcessor, ErrorWorkflow errorWorkflow) {
    this.validationProcessor = validationProcessor;
    this.errorWorkflow = errorWorkflow;
  }

  /**
   * Invokes the validationProcessor and if there is a validation exception, it passes the constraint violations to the
   * validationProcessor and then invokes the error workflow.
   *
   * @param chain The chain.
   * @throws IOException      If the chain throws.
   * @throws ServletException If the chain throws.
   */
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    try {
      validationProcessor.validate();
      chain.continueWorkflow();
    } catch (ValidationException e) {
      validationProcessor.handle(e.violations);

      SubWorkflowChain errorChain = new SubWorkflowChain(Arrays.<Workflow>asList(errorWorkflow), chain);
      errorChain.continueWorkflow();
    }
  }
}
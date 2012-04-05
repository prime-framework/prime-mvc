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
package org.primeframework.mvc.parameter;

import javax.servlet.ServletException;
import java.io.IOException;

import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.parameter.ParameterParser.Parameters;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * This class uses the {@link ExpressionEvaluator} to process the incoming request parameters. It also handles check
 * boxes, submit buttons and radio buttons.
 *
 * @author Brian Pontarelli
 */
public class DefaultParameterWorkflow implements ParameterWorkflow {
  private final ActionInvocationStore actionInvocationStore;
  private final ParameterParser parser;
  private final ParameterHandler handler;

  @Inject
  public DefaultParameterWorkflow(ActionInvocationStore actionInvocationStore, ParameterParser parser, ParameterHandler handler) {
    this.actionInvocationStore = actionInvocationStore;
    this.parser = parser;
    this.handler = handler;
  }

  /**
   * Handles the incoming HTTP request parameters.
   *
   * @param chain The workflow chain.
   */
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    if (actionInvocationStore.getCurrent().action != null) {
      Parameters params = parser.parse();
      handler.handle(params);
    }

    chain.continueWorkflow();
  }
}

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
package org.primeframework.mvc.workflow;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * This interface defines a unit of work that should be performed within Prime.
 *
 * @author Brian Pontarelli
 */
public interface Workflow {
  /**
   * Performs a task.
   *
   * @param workflowChain This chain should be called if the Workflow wants to continue processing the request by the
   *                      next Workflow in the chain or by the next J2EE filter in the chain after the PrimeFilter.
   * @throws IOException      If the workflow had any IO problems.
   * @throws ServletException If the workflow had any servlet problems.
   */
  void perform(WorkflowChain workflowChain) throws IOException, ServletException;
}
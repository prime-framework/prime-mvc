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
package org.primeframework.mvc.servlet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import com.google.inject.ImplementedBy;

/**
 * This interface defines how {@link Workflow} instances pass control to the next Workflow or back to the
 * JCatapultFilter.
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultWorkflowChain.class)
public interface WorkflowChain {
  void start(FilterChain filterChain) throws IOException, ServletException;

  /**
   * Invokes the next {@link Workflow} in the chain, or if all the workflows have been invoked this should pass control
   * back to the JCatapultFilter to continue processing the HTTP request down the filter chain.
   *
   * @throws IOException      If the workflows or filters throw IOException.
   * @throws ServletException If the workflows or filters throw ServletException.
   */
  void continueWorkflow() throws IOException, ServletException;

  /**
   * This method resets the workflow to its initial state. This is useful when the workflow needs to be reprocessed
   * completely for any reason (such as security exceptions).
   */
  void reset();

  Iterable<Workflow> workflows();
}

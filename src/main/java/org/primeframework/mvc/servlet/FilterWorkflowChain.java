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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.primeframework.mvc.workflow.DefaultWorkflowChain;
import org.primeframework.mvc.workflow.Workflow;

/**
 * A WorkflowChain that ends with the FilterChain being invoked.
 *
 * @author Brian Pontarelli
 */
public class FilterWorkflowChain extends DefaultWorkflowChain {
  private final FilterChain filterChain;
  private final HttpServletRequest request;
  private final HttpServletResponse response;

  public FilterWorkflowChain(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response, Workflow... workflows) {
    super(workflows);
    this.filterChain = filterChain;
    this.request = request;
    this.response = response;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void continueWorkflow() throws IOException, ServletException {
    if (iterator.hasNext()) {
      super.continueWorkflow();
    } else {
      filterChain.doFilter(request, response);
    }
  }
}

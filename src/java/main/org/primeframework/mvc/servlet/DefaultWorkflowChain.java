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
import java.util.Iterator;

import com.google.inject.Inject;
import static java.util.Arrays.*;

/**
 * This class is the default chain that first iterates over all the Workflow instances that were resolved in the {@link
 * JCatapultFilter} using the {@link WorkflowChain} implementation. After all the Workflows have been invoked, this
 * continues to invoke the rest of the FilterChain that was passed into the JCatapultFilter.
 *
 * @author Brian Pontarelli
 */
public class DefaultWorkflowChain implements WorkflowChain {
  private final Iterable<Workflow> workflows;
  private FilterChain filterChain;
  private Iterator<Workflow> iterator;

  @Inject
  public DefaultWorkflowChain(CoreWorkflow coreWorkflow, MVCWorkflow mvcWorkflow) {
    workflows = asList(coreWorkflow, mvcWorkflow);
    iterator = workflows.iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start(FilterChain filterChain) throws IOException, ServletException {
    this.filterChain = filterChain;
    continueWorkflow();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void continueWorkflow() throws IOException, ServletException {
    if (iterator.hasNext()) {
      Workflow workflow = iterator.next();
      workflow.perform(this);
    } else {
      filterChain.doFilter(ServletObjectsHolder.getServletRequest(), ServletObjectsHolder.getServletResponse());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset() {
    iterator = workflows.iterator();
  }

  @Override
  public Iterable<Workflow> workflows() {
    return workflows;
  }
}

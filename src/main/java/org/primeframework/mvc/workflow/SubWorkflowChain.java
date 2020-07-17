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
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a sub-workflow chain that can be used to chain multiple workflows under a single workflow.
 *
 * @author Brian Pontarelli
 */
public class SubWorkflowChain implements WorkflowChain {
  private static final Logger logger = LoggerFactory.getLogger(SubWorkflowChain.class);

  private final WorkflowChain outer;

  private final Iterator<Workflow> iterator;

  public SubWorkflowChain(Iterable<Workflow> workflows, WorkflowChain outer) {
    this.outer = outer;
    this.iterator = workflows.iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void continueWorkflow() throws IOException, ServletException {
    if (iterator.hasNext()) {
      long start = System.currentTimeMillis();

      Workflow workflow = iterator.next();
      workflow.perform(this);

      if (logger.isDebugEnabled()) {
        logger.debug("Workflow [{}]] took [{}]", workflow.getClass(), (System.currentTimeMillis() - start));
      }
    } else {
      outer.continueWorkflow();
    }
  }
}

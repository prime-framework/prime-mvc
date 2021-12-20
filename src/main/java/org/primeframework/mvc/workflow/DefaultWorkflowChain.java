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

import java.io.IOException;
import java.util.Iterator;

import static java.util.Arrays.*;

/**
 * This class is the default chain that iterates over all the Workflow instances passed in.
 *
 * @author Brian Pontarelli
 */
public class DefaultWorkflowChain implements WorkflowChain {
  protected final Iterator<Workflow> iterator;

  public DefaultWorkflowChain(Workflow... workflows) {
    Iterable<Workflow> workflowList = asList(workflows);
    iterator = workflowList.iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void continueWorkflow() throws IOException {
    if (iterator.hasNext()) {
      Workflow workflow = iterator.next();
      workflow.perform(this);
    }
  }
}

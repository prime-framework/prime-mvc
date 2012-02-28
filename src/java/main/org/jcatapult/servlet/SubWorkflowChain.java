/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.servlet;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;

/**
 * <p>
 * This class is a sub-workflow chain that can be used to chain multiple workflows
 * under a single workflow.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class SubWorkflowChain implements WorkflowChain {
    private final Iterable<Workflow> workflows;
    private final WorkflowChain workflowChain;
    private Iterator<Workflow> iterator;

    public SubWorkflowChain(Iterable<Workflow> workflows, WorkflowChain workflowChain) {
        this.workflows = workflows;
        this.workflowChain = workflowChain;
        this.iterator = workflows.iterator();
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
            workflowChain.continueWorkflow();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        iterator = workflows.iterator();
        workflowChain.reset();
    }
}

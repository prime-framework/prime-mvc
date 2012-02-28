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
 *
 */
package org.jcatapult.servlet;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import com.google.inject.Inject;
import static java.util.Arrays.*;

/**
 * <p>
 * This is the core workflow that includes the static resource workflows.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class CoreWorkflow implements Workflow {
    private final List<Workflow> workflows;

    @Inject
    public CoreWorkflow(RequestBodyWorkflow requestBodyWorkflow, StaticResourceWorkflow staticResourceWorkflow) {
        this.workflows = asList(requestBodyWorkflow, staticResourceWorkflow);
    }

    /**
     * Creates a sub-chain and invokes it.
     *
     * @param   chain The end of the sub chain.
     * @throws  IOException If the sub chain throws.
     * @throws  ServletException If the sub chain throws.
     */
    public void perform(WorkflowChain chain) throws IOException, ServletException {
        SubWorkflowChain sub = new SubWorkflowChain(workflows, chain);
        sub.continueWorkflow();
    }
}

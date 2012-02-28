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
package org.primeframework.mvc.action;

import java.io.IOException;
import javax.servlet.ServletException;

import org.primeframework.mvc.action.annotation.ActionPrepareMethod;
import org.primeframework.mvc.util.MethodTools;
import org.primeframework.servlet.WorkflowChain;

import com.google.inject.Inject;

/**
 * <p>
 * This implements the action prepare workflow and invokes any method that
 * is annotated with the @PrepareMethod annotation.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultActionPrepareWorkflow implements ActionPrepareWorkflow {
    private final ActionInvocationStore actionInvocationStore;

    @Inject
    public DefaultActionPrepareWorkflow(ActionInvocationStore actionInvocationStore) {
        this.actionInvocationStore = actionInvocationStore;
    }

    /**
     * Calls any method annotated with the {@link org.primeframework.mvc.action.annotation.ActionPrepareMethod} annotation.
     *
     * @param   workflowChain The workflow chain that is called after preparation.
     * @throws  IOException If the chain throws.
     * @throws  ServletException If the chain throws.
     */
    public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
        Object action = actionInvocationStore.getCurrent().action();
        if (action != null) {
            MethodTools.invokeAllWithAnnotation(action, ActionPrepareMethod.class);
        }

        workflowChain.continueWorkflow();
    }
}
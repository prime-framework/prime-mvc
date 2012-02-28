/*
 * Copyright (c) 2001-2010, JCatapult.org, All Rights Reserved
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
package org.jcatapult.mvc.servlet;

import javax.servlet.ServletException;
import java.io.IOException;

import org.jcatapult.servlet.WorkflowChain;

/**
 * <p>
 * This is a mock workflow chain for testing.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class MockWorkflowChain implements WorkflowChain {
    private final Runnable runnable;

    public MockWorkflowChain(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void continueWorkflow() throws IOException, ServletException {
        runnable.run();
    }

    @Override
    public void reset() {
    }
}

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
package org.jcatapult.mvc.action;

import java.io.IOException;
import javax.servlet.ServletException;

import org.easymock.EasyMock;
import org.example.action.user.Edit;
import org.jcatapult.servlet.WorkflowChain;
import org.jcatapult.test.JCatapultBaseTest;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * <p>
 * This class tests the default action prepare workflow.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultActionPrepareWorkflowTest extends JCatapultBaseTest {
    @Test
    public void testPrepare() throws IOException, ServletException {
        Edit edit = new Edit();
        ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
        EasyMock.expect(store.getCurrent()).andReturn(new DefaultActionInvocation(edit, "/foo", null, null));
        EasyMock.replay(store);

        WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
        chain.continueWorkflow();
        EasyMock.replay(chain);

        DefaultActionPrepareWorkflow workflow = new DefaultActionPrepareWorkflow(store);
        workflow.perform(chain);

        assertTrue(edit.actionPrepared);
    }
}
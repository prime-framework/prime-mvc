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
package org.primeframework.mvc.scope;

import org.example.action.KitchenSinkAction;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertSame;

/**
 * Tests the scope retrieval workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultScopeRetrievalWorkflowTest extends PrimeBaseTest {
  @Test
  public void perform() throws Exception {
    Object obj = new Object();
    KitchenSinkAction action = new KitchenSinkAction(null);
    session.setAttribute("sessionObject", obj);

    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    FlashScope fs = createStrictMock(FlashScope.class);
    fs.transferFlash();
    replay(fs);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    DefaultScopeRetrievalWorkflow workflow = new DefaultScopeRetrievalWorkflow(ais, fs, new DefaultScopeProvider(injector));
    workflow.perform(chain);

    assertSame(action.sessionObject, obj);

    verify(ais, fs, chain);
  }
}

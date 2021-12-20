/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter;

import java.util.List;

import org.example.action.ComplexRestAction;
import org.example.action.user.EditAction;
import org.example.action.user.RESTEdit;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * This class tests the default URI parameter workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultURIParameterWorkflowTest extends PrimeBaseTest {
  @Test
  public void complexParameters() throws Exception {
    ComplexRestAction action = new ComplexRestAction();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "", map("firstName", List.of("brian"), "lastName", List.of("pontarelli"), "theRest", List.of("then", "a", "bunch", "of", "stuff")));
    expect(store.getCurrent()).andReturn(ai);
    replay(store);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(request, store);
    workflow.perform(chain);

    assertEquals(request.getParameterValue("firstName"), "brian");
    assertEquals(request.getParameterValue("lastName"), "pontarelli");
    assertEquals(request.getParameterValues("theRest").get(0), "then");
    assertEquals(request.getParameterValues("theRest").get(1), "a");
    assertEquals(request.getParameterValues("theRest").get(2), "bunch");
    assertEquals(request.getParameterValues("theRest").get(3), "of");
    assertEquals(request.getParameterValues("theRest").get(4), "stuff");

    verify(store, chain);
  }

  @Test
  public void noParameters() throws Exception {
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ActionInvocation ai = makeActionInvocation(new EditAction(), HTTPMethod.POST, "");
    expect(store.getCurrent()).andReturn(ai);
    replay(store);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(request, store);
    workflow.perform(chain);

    verify(store, chain);
  }

  @Test
  public void singleIDParameters() throws Exception {
    RESTEdit action = new RESTEdit();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "", map("id", List.of("12")));
    expect(store.getCurrent()).andReturn(ai);
    replay(store);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(request, store);
    workflow.perform(chain);

    assertEquals(request.getParameterValue("id"), "12");

    verify(store, chain);
  }
}
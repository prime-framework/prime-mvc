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

import javax.servlet.http.HttpServletRequestWrapper;

import java.net.URLEncoder;

import org.example.action.ComplexRest;
import org.example.action.EncodedRestValue;
import org.example.action.user.Edit;
import org.example.action.user.RESTEdit;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This class tests the default URI parameter workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultURIParameterWorkflowTest extends PrimeBaseTest {

  @Test
  public void noParameters() throws Exception {
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ActionInvocation ai = makeActionInvocation(new Edit(), HTTPMethod.POST, "");
    expect(store.getCurrent()).andReturn(ai);
    replay(store);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(new HttpServletRequestWrapper(request), store);
    workflow.perform(chain);

    verify(store, chain);
  }

  @Test
  public void singleIDParameters() throws Exception {
    RESTEdit action = new RESTEdit();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "", "12");
    expect(store.getCurrent()).andReturn(ai);
    replay(store);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(wrapper, store);
    workflow.perform(chain);

    assertEquals(wrapper.getParameter("id"), "12");

    verify(store, chain);
  }

  @Test
  public void encodedParameters() throws Exception {
    EncodedRestValue action = new EncodedRestValue();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "", URLEncoder.encode("user@foo.com", "UTF-8"), URLEncoder.encode("bar@foo.com", "UTF-8"), URLEncoder.encode("baz@foo.com", "UTF-8"));
    expect(store.getCurrent()).andReturn(ai);
    replay(store);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(wrapper, store);
    workflow.perform(chain);

    assertEquals(wrapper.getParameter("email"), "user@foo.com");
    assertEquals(wrapper.getParameterValues("theRest")[0], "bar@foo.com");
    assertEquals(wrapper.getParameterValues("theRest")[1], "baz@foo.com");

    verify(store, chain);
  }

  @Test
  public void complexParameters() throws Exception {
    ComplexRest action = new ComplexRest();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    ActionInvocation ai = makeActionInvocation(action, HTTPMethod.POST, "", "brian", "static", "pontarelli", "then", "a", "bunch", "of", "stuff");
    expect(store.getCurrent()).andReturn(ai);
    replay(store);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(wrapper, store);
    workflow.perform(chain);

    assertEquals(wrapper.getParameter("firstName"), "brian");
    assertEquals(wrapper.getParameter("lastName"), "pontarelli");
    assertEquals(wrapper.getParameterValues("theRest")[0], "then");
    assertEquals(wrapper.getParameterValues("theRest")[1], "a");
    assertEquals(wrapper.getParameterValues("theRest")[2], "bunch");
    assertEquals(wrapper.getParameterValues("theRest")[3], "of");
    assertEquals(wrapper.getParameterValues("theRest")[4], "stuff");

    verify(store, chain);
  }
}
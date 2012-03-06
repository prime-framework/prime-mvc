/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

import org.example.action.ComplexRest;
import org.example.action.user.Edit;
import org.example.action.user.RESTEdit;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.config.DefaultActionConfiguration;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static java.util.Arrays.*;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This class tests the default URI parmaeter workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultURIParameterWorkflowTest extends PrimeBaseTest {
  @Inject public ExpressionEvaluator expressionEvaluator;

  @Test
  public void noParameters() throws IOException, ServletException {
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(
      new DefaultActionInvocation(new Edit(), "/admin/user/edit", null, new DefaultActionConfiguration(Edit.class, "/admin/user/edit")));
    replay(store);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(new HttpServletRequestWrapper(request), store);
    workflow.perform(chain);

    verify(store, chain);
  }

  @Test
  public void singleIDParameters() throws IOException, ServletException {
    RESTEdit action = new RESTEdit();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(
      new DefaultActionInvocation(action, "/admin/user/rest-edit/12", null, asList("12"), new DefaultActionConfiguration(RESTEdit.class, "/admin/user/rest-edit"), true));
    replay(store);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(wrapper, store);
    workflow.perform(chain);

    assertEquals("12", wrapper.getParameter("id"));

    verify(store, chain);
  }

  @Test
  public void complexParameters() throws IOException, ServletException {
    ComplexRest action = new ComplexRest();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(
      new DefaultActionInvocation(action, "/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff", null,
        asList("brian", "static", "pontarelli", "then", "a", "bunch", "of", "stuff"),
        new DefaultActionConfiguration(ComplexRest.class, "/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff"), true));
    replay(store);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(wrapper, store);
    workflow.perform(chain);

    assertEquals("brian", wrapper.getParameter("firstName"));
    assertEquals("pontarelli", wrapper.getParameter("lastName"));
    assertEquals("then", wrapper.getParameterValues("theRest")[0]);
    assertEquals("a", wrapper.getParameterValues("theRest")[1]);
    assertEquals("bunch", wrapper.getParameterValues("theRest")[2]);
    assertEquals("of", wrapper.getParameterValues("theRest")[3]);
    assertEquals("stuff", wrapper.getParameterValues("theRest")[4]);

    verify(store, chain);
  }
}
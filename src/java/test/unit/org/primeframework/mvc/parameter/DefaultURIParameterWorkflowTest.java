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

import org.easymock.EasyMock;
import org.example.action.ComplexRest;
import org.example.action.user.Edit;
import org.example.action.user.RESTEdit;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.config.DefaultActionConfiguration;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.primeframework.mvc.test.JCatapultBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static java.util.Arrays.*;
import static org.testng.Assert.*;

/**
 * <p> This class tests the default URI parmaeter workflow. </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultURIParameterWorkflowTest extends JCatapultBaseTest {
  @Inject public ExpressionEvaluator expressionEvaluator;

  /**
   * Tests the no parameters case.
   *
   * @throws IOException      Never
   * @throws ServletException Never
   */
  @Test
  public void testNoParameters() throws IOException, ServletException {
    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(store.getCurrent()).andReturn(
      new DefaultActionInvocation(new Edit(), "/admin/user/edit", null, new DefaultActionConfiguration(Edit.class, "/admin/user/edit")));
    EasyMock.replay(store);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(new HttpServletRequestWrapper(request), store);
    workflow.perform(chain);

    EasyMock.verify(store, chain);
  }

  /**
   * Tests the single parameter case.
   *
   * @throws IOException      Never
   * @throws ServletException Never
   */
  @Test
  public void testSingleIDParameters() throws IOException, ServletException {
    RESTEdit action = new RESTEdit();
    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(store.getCurrent()).andReturn(
      new DefaultActionInvocation(action, "/admin/user/rest-edit/12", null, asList("12"), new DefaultActionConfiguration(RESTEdit.class, "/admin/user/rest-edit"), true));
    EasyMock.replay(store);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    DefaultURIParameterWorkflow workflow = new DefaultURIParameterWorkflow(wrapper, store);
    workflow.perform(chain);

    assertEquals("12", wrapper.getParameter("id"));

    EasyMock.verify(store, chain);
  }

  /**
   * Tests the complex parameters case.
   *
   * @throws IOException      Never
   * @throws ServletException Never
   */
  @Test
  public void testComplexParameters() throws IOException, ServletException {
    ComplexRest action = new ComplexRest();
    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(store.getCurrent()).andReturn(
      new DefaultActionInvocation(action, "/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff", null,
        asList("brian", "static", "pontarelli", "then", "a", "bunch", "of", "stuff"),
        new DefaultActionConfiguration(ComplexRest.class, "/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff"), true));
    EasyMock.replay(store);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

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

    EasyMock.verify(store, chain);
  }
}
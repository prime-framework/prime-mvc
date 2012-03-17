/*
 * Copyright (c) 2001-2010, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;

import org.example.action.Simple;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.config.DefaultActionConfiguration;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This class tests the default action invocation workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultResultInvocationWorkflowTest {
  @Test
  public void actionLessWithDefault() throws IOException, ServletException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    replay(response);

    ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    Annotation annotation = new ForwardResult.ForwardImpl("/foo/bar", null);
    ResultInvocation ri = new DefaultResultInvocation(annotation, "/foo/bar", null);
    ResultInvocationProvider rip = createStrictMock(ResultInvocationProvider.class);
    expect(rip.lookup()).andReturn(ri);
    replay(rip);

    Result result = createStrictMock(Result.class);
    result.execute(annotation);
    replay(result);

    ResultProvider resultProvider = createStrictMock(ResultProvider.class);
    expect(resultProvider.lookup(Forward.class)).andReturn(result);
    replay(resultProvider);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);
    
    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.clear();
    replay(resultStore);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider, resultStore);
    workflow.perform(chain);

    verify(response, ais, rip, result, resultProvider, chain);
  }

  @Test
  public void actionLessWithoutDefault() throws IOException, ServletException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    replay(response);

    ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    ResultInvocationProvider rip = createStrictMock(ResultInvocationProvider.class);
    expect(rip.lookup()).andReturn(null);
    replay(rip);

    Result result = createStrictMock(Result.class);
    replay(result);

    ResultProvider resultProvider = createStrictMock(ResultProvider.class);
    replay(resultProvider);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.clear();
    replay(resultStore);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider, resultStore);
    workflow.perform(chain);

    verify(response, ais, rip, result, resultProvider, chain);
  }

  @Test
  public void actionWithResult() throws IOException, ServletException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    replay(response);

    Simple simple = new Simple();
    ActionInvocation invocation = new DefaultActionInvocation(simple, "/foo/bar", null, null, new DefaultActionConfiguration(Simple.class, "/foo/bar"), true);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    Annotation annotation = new ForwardResult.ForwardImpl("/foo/bar", "success");
    ResultInvocation ri = new DefaultResultInvocation(annotation, "/foo/bar", "success");
    ResultInvocationProvider rip = createStrictMock(ResultInvocationProvider.class);
    expect(rip.lookup("success")).andReturn(ri);
    replay(rip);

    Result result = createStrictMock(Result.class);
    result.execute(annotation);
    replay(result);

    ResultProvider resultProvider = createStrictMock(ResultProvider.class);
    expect(resultProvider.lookup(annotation.annotationType())).andReturn(result);
    replay(resultProvider);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);
    
    ResultStore resultStore = createStrictMock(ResultStore.class);
    expect(resultStore.get()).andReturn("success");
    resultStore.clear();
    replay(resultStore);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider, resultStore);
    workflow.perform(chain);

    verify(response, ais, rip, result, resultProvider, chain);
  }

  @Test
  public void actionSuppressResult() throws IOException, ServletException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    replay(response);

    Simple simple = new Simple();
    ActionInvocation invocation = new DefaultActionInvocation(simple, "/foo/bar", null, null, new DefaultActionConfiguration(Simple.class, "/foo/bar"), false);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    ResultInvocationProvider rip = createStrictMock(ResultInvocationProvider.class);
    replay(rip);

    Result result = createStrictMock(Result.class);
    replay(result);

    ResultProvider resultProvider = createStrictMock(ResultProvider.class);
    replay(resultProvider);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.clear();
    replay(resultStore);
    
    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider, resultStore);
    workflow.perform(chain);

    verify(response, ais, rip, result, resultProvider, chain);
  }

  @Test
  public void actionMissingResult() throws IOException, ServletException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(404);
    replay(response);

    Simple simple = new Simple();
    ActionInvocation invocation = new DefaultActionInvocation(simple, "/foo/bar", null, null, new DefaultActionConfiguration(Simple.class, "/foo/bar"), true);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    ResultInvocationProvider rip = createStrictMock(ResultInvocationProvider.class);
    expect(rip.lookup("success")).andReturn(null);
    replay(rip);

    ResultProvider resultProvider = createStrictMock(ResultProvider.class);
    replay(resultProvider);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    expect(resultStore.get()).andReturn("success");
    resultStore.clear();
    replay(resultStore);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider, resultStore);
    try {
      workflow.perform(chain);
      fail("Should have failed with 404");
    } catch (ServletException e) {
      System.out.println(e);
      // Expected
    }

    verify(response, ais, rip, resultProvider, chain);
  }

  @Test
  public void actionMissingResultType() throws IOException {
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    replay(response);

    Simple simple = new Simple();
    ActionInvocation invocation = new DefaultActionInvocation(simple, "/foo/bar", null, null, new DefaultActionConfiguration(Simple.class, "/foo/bar"), true);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    Annotation annotation = new ForwardResult.ForwardImpl("/foo/bar", "success");
    ResultInvocation ri = new DefaultResultInvocation(annotation, "/foo/bar", "success");
    ResultInvocationProvider rip = createStrictMock(ResultInvocationProvider.class);
    expect(rip.lookup("success")).andReturn(ri);
    replay(rip);

    ResultProvider resultProvider = createStrictMock(ResultProvider.class);
    expect(resultProvider.lookup(annotation.annotationType())).andReturn(null);
    replay(resultProvider);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    expect(resultStore.get()).andReturn("success");
    resultStore.clear();
    replay(resultStore);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(response, ais, rip, resultProvider, resultStore);
    try {
      workflow.perform(chain);
      fail("Should have failed");
    } catch (ServletException e) {
      System.out.println(e);
      // Expected
    }

    verify(response, ais, rip, resultProvider, chain);
  }
}

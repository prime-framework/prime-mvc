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
package org.primeframework.mvc.action;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.example.action.ExecuteMethodThrowsCheckedException;
import org.example.action.ExecuteMethodThrowsException;
import org.example.action.ExtensionInheritance;
import org.example.action.Head;
import org.example.action.InvalidExecuteMethod;
import org.example.action.MissingExecuteMethod;
import org.example.action.Post;
import org.example.action.Simple;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.config.DefaultActionConfiguration;
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This class tests the default action invocation workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionInvocationWorkflowTest {
  @Test
  public void actionLess() throws Exception {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    replay(request);

    ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    verify(request, ais, chain);
  }

  @Test
  public void action() throws Exception {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getMethod()).andReturn("POST");
    replay(request);

    Simple simple = new Simple();

    ActionInvocation invocation = new DefaultActionInvocation(simple, "/foo/bar", null, null);

    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("success");
    replay(resultStore);
    
    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    assertTrue(simple.invoked);

    verify(request, ais, chain);
  }

  @Test
  public void actionWithoutExecuteMethod() throws Exception {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getMethod()).andReturn("POST");
    replay(request);

    MissingExecuteMethod action = new MissingExecuteMethod();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    try {
      workflow.perform(chain);
      fail("Should have failed");
    } catch (PrimeException e) {
      System.out.println(e);
      // Expected
    }

    verify(request, ais, chain);
  }

  @Test
  public void actionWithWrongReturnType() throws Exception {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getMethod()).andReturn("POST");
    replay(request);

    InvalidExecuteMethod action = new InvalidExecuteMethod();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, new DefaultActionConfiguration(InvalidExecuteMethod.class, "/foo/bar"));
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    try {
      workflow.perform(chain);
      fail("Should have failed");
    } catch (PrimeException e) {
      System.out.println(e);
      // Expected
    }

    assertFalse(action.invoked);

    verify(request, ais, chain);
  }

  @Test
  public void actionThatThrowsRuntimeException() throws Exception {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    ExecuteMethodThrowsException action = new ExecuteMethodThrowsException();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    try {
      workflow.perform(chain);
      fail("Should have failed");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    assertTrue(action.invoked);

    verify(request, ais, chain);
  }

  @Test
  public void actionThatThrowsCheckedException() throws Exception {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    ExecuteMethodThrowsCheckedException action = new ExecuteMethodThrowsCheckedException();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    try {
      workflow.perform(chain);
      fail("Should have failed");
    } catch (RuntimeException e) {
      // Expected
      assertTrue(e.getCause() instanceof InvocationTargetException);
      assertTrue(((InvocationTargetException) e.getCause()).getTargetException() instanceof IOException);
    }

    assertTrue(action.invoked);

    verify(request, ais, chain);
  }

  @Test
  public void actionExtension() throws Exception {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    ExtensionInheritance action = new ExtensionInheritance();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", "ajax", null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("ajax");
    replay(resultStore);
    
    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    assertFalse(action.baseInvoked);
    assertTrue(action.invoked);

    verify(request, ais, chain);
  }

  @Test
  public void actionExtensionInheritance() throws Exception {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getMethod()).andReturn("GET");
    replay(request);

    ExtensionInheritance action = new ExtensionInheritance();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", "json", null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("json");
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    assertTrue(action.baseInvoked);
    assertFalse(action.invoked);

    verify(request, ais, chain);
  }

  @Test
  public void httpMethod() throws Exception {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getMethod()).andReturn("POST");
    replay(request);

    Post action = new Post();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("success");
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    assertTrue(action.invoked);

    verify(request, ais, chain);
  }

  @Test
  public void httpHeadMethod() throws Exception {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getMethod()).andReturn("HEAD");
    replay(request);

    Head action = new Head();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("success");
    replay(resultStore);
    
    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    assertTrue(action.invoked);

    verify(request, ais, chain);
  }
}

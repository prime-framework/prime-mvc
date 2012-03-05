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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.example.action.ExecuteMethodThrowsCheckedException;
import org.example.action.ExecuteMethodThrowsException;
import org.example.action.ExtensionInheritance;
import org.example.action.Head;
import org.example.action.InvalidExecuteMethod;
import org.example.action.MissingExecuteMethod;
import org.example.action.Post;
import org.example.action.Simple;
import org.primeframework.mvc.action.config.DefaultActionConfiguration;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.capture;
import static org.testng.Assert.*;

/**
 * <p> This class tests the default action invocation workflow. </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultActionInvocationWorkflowTest {
  @Test
  public void actionLess() throws Exception {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.replay(request);

    ActionInvocation ai = new DefaultActionInvocation(null, "/foo/bar", null, null);
    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(ai);
    EasyMock.replay(ais);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    EasyMock.verify(request, ais, chain);
  }

  @Test
  public void action() throws Exception {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getMethod()).andReturn("POST");
    EasyMock.replay(request);

    Simple simple = new Simple();

    ActionInvocation invocation = new DefaultActionInvocation(simple, "/foo/bar", null, null);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(invocation);
    ais.removeCurrent();

    Capture<ActionInvocation> capture = new Capture<ActionInvocation>();
    ais.setCurrent(capture(capture));
    EasyMock.replay(ais);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    assertTrue(simple.invoked);
    assertEquals("success", capture.getValue().resultCode());

    EasyMock.verify(request, ais, chain);
  }

  @Test
  public void actionWithoutExecuteMethod() throws IOException {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getMethod()).andReturn("POST");
    EasyMock.replay(request);

    MissingExecuteMethod action = new MissingExecuteMethod();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, null);
    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(invocation);
    EasyMock.replay(ais);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    EasyMock.replay(chain);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    try {
      workflow.perform(chain);
      fail("Should have failed");
    } catch (ServletException e) {
      System.out.println(e);
      // Expected
    }

    EasyMock.verify(request, ais, chain);
  }

  @Test
  public void actionWithWrongReturnType() throws Exception {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getMethod()).andReturn("POST");
    EasyMock.replay(request);

    InvalidExecuteMethod action = new InvalidExecuteMethod();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, new DefaultActionConfiguration(InvalidExecuteMethod.class, "/foo/bar"));
    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(invocation);
    EasyMock.replay(ais);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    EasyMock.replay(chain);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    try {
      workflow.perform(chain);
      fail("Should have failed");
    } catch (ServletException e) {
      System.out.println(e);
      // Expected
    }

    assertFalse(action.invoked);

    EasyMock.verify(request, ais, chain);
  }

  @Test
  public void actionThatThrowsRuntimeException() throws IOException, ServletException {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getMethod()).andReturn("GET");
    EasyMock.replay(request);

    ExecuteMethodThrowsException action = new ExecuteMethodThrowsException();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, null);
    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(invocation);
    EasyMock.replay(ais);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    EasyMock.replay(chain);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    try {
      workflow.perform(chain);
      fail("Should have failed");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    assertTrue(action.invoked);

    EasyMock.verify(request, ais, chain);
  }

  @Test
  public void actionThatThrowsCheckedException() throws IOException, ServletException {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getMethod()).andReturn("GET");
    EasyMock.replay(request);

    ExecuteMethodThrowsCheckedException action = new ExecuteMethodThrowsCheckedException();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, null);
    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(invocation);
    EasyMock.replay(ais);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    EasyMock.replay(chain);

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

    EasyMock.verify(request, ais, chain);
  }

  @Test
  public void actionExtension() throws Exception {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getMethod()).andReturn("GET");
    EasyMock.replay(request);

    ExtensionInheritance action = new ExtensionInheritance();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", "ajax", null);
    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(invocation);
    ais.removeCurrent();
    Capture<ActionInvocation> capture = new Capture<ActionInvocation>();
    ais.setCurrent(capture(capture));
    EasyMock.replay(ais);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    assertFalse(action.baseInvoked);
    assertTrue(action.invoked);
    assertEquals("ajax", capture.getValue().resultCode());
    assertEquals("/foo/bar", capture.getValue().actionURI());
    assertEquals("/foo/bar.ajax", capture.getValue().uri());

    EasyMock.verify(request, ais, chain);
  }

  @Test
  public void actionExtensionInheritance() throws Exception {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getMethod()).andReturn("GET");
    EasyMock.replay(request);

    ExtensionInheritance action = new ExtensionInheritance();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", "json", null);
    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(invocation);
    ais.removeCurrent();
    Capture<ActionInvocation> capture = new Capture<ActionInvocation>();
    ais.setCurrent(capture(capture));
    EasyMock.replay(ais);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    assertTrue(action.baseInvoked);
    assertFalse(action.invoked);
    assertEquals("json", capture.getValue().resultCode());
    assertEquals("/foo/bar", capture.getValue().actionURI());
    assertEquals("/foo/bar.json", capture.getValue().uri());

    EasyMock.verify(request, ais, chain);
  }

  @Test
  public void httpMethod() throws Exception {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getMethod()).andReturn("POST");
    EasyMock.replay(request);

    Post action = new Post();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, null);
    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(invocation);
    ais.removeCurrent();
    Capture<ActionInvocation> capture = new Capture<ActionInvocation>();
    ais.setCurrent(capture(capture));
    EasyMock.replay(ais);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    assertTrue(action.invoked);
    assertEquals("success", capture.getValue().resultCode());

    EasyMock.verify(request, ais, chain);
  }

  @Test
  public void httpHeadMethod() throws Exception {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getMethod()).andReturn("HEAD");
    EasyMock.replay(request);

    Head action = new Head();
    ActionInvocation invocation = new DefaultActionInvocation(action, "/foo/bar", null, null);
    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(invocation);
    ais.removeCurrent();
    Capture<ActionInvocation> capture = new Capture<ActionInvocation>();
    ais.setCurrent(capture(capture));
    EasyMock.replay(ais);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore, request);
    workflow.perform(chain);

    assertTrue(action.invoked);
    assertEquals("success", capture.getValue().resultCode());

    EasyMock.verify(request, ais, chain);
  }
}

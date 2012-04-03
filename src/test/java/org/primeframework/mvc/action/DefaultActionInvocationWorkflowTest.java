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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.example.action.ExecuteMethodThrowsCheckedException;
import org.example.action.ExecuteMethodThrowsException;
import org.example.action.ExtensionInheritance;
import org.example.action.Head;
import org.example.action.Post;
import org.example.action.Simple;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This class tests the default action invocation workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionInvocationWorkflowTest extends PrimeBaseTest {
  @Test
  public void actionLess() throws Exception {
    ActionInvocation ai = new ActionInvocation(null, null, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore);
    workflow.perform(chain);

    verify(ais, chain);
  }

  @Test
  public void action() throws Exception {
    Simple simple = new Simple();
    ActionInvocation invocation = makeActionInvocation(HTTPMethod.POST, simple, "execute", "/foo/bar", null);

    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("success");
    replay(resultStore);
    
    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore);
    workflow.perform(chain);

    assertTrue(simple.invoked);

    verify(ais, chain);
  }

  @Test
  public void actionThatThrowsRuntimeException() throws Exception {
    ExecuteMethodThrowsException action = new ExecuteMethodThrowsException();
    ActionInvocation invocation = makeActionInvocation(HTTPMethod.POST, action, "execute", "/foo/bar", null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore);
    try {
      workflow.perform(chain);
      fail("Should have failed");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    assertTrue(action.invoked);

    verify(ais, chain);
  }

  @Test
  public void actionThatThrowsCheckedException() throws Exception {
    ExecuteMethodThrowsCheckedException action = new ExecuteMethodThrowsCheckedException();
    ActionInvocation invocation = makeActionInvocation(HTTPMethod.POST, action, "execute", "/foo/bar", null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore);
    try {
      workflow.perform(chain);
      fail("Should have failed");
    } catch (RuntimeException e) {
      // Expected
      assertTrue(e.getCause() instanceof InvocationTargetException);
      assertTrue(((InvocationTargetException) e.getCause()).getTargetException() instanceof IOException);
    }

    assertTrue(action.invoked);

    verify(ais, chain);
  }

  @Test
  public void actionExtension() throws Exception {
    ExtensionInheritance action = new ExtensionInheritance();
    ActionInvocation invocation = makeActionInvocation(HTTPMethod.POST, action, "post", "/foo/bar", "post");
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("child");
    replay(resultStore);
    
    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore);
    workflow.perform(chain);

    assertFalse(action.baseInvoked);
    assertTrue(action.invoked);

    verify(ais, chain);
  }

  @Test
  public void actionExtensionInheritance() throws Exception {
    ExtensionInheritance action = new ExtensionInheritance();
    ActionInvocation invocation = makeActionInvocation(HTTPMethod.POST, action, "get", "/foo/bar", "get");
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("parent");
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore);
    workflow.perform(chain);

    assertTrue(action.baseInvoked);
    assertFalse(action.invoked);

    verify(ais, chain);
  }

  @Test
  public void httpMethod() throws Exception {
    Post action = new Post();
    ActionInvocation invocation = makeActionInvocation(HTTPMethod.POST, action, "post", "/foo/bar", null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("success");
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore);
    workflow.perform(chain);

    assertTrue(action.invoked);

    verify(ais, chain);
  }

  @Test
  public void httpHeadMethod() throws Exception {
    Head action = new Head();
    ActionInvocation invocation = makeActionInvocation(HTTPMethod.POST, action, "head", "/foo/bar", null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(invocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("success");
    replay(resultStore);
    
    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore);
    workflow.perform(chain);

    assertTrue(action.invoked);

    verify(ais, chain);
  }
}

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
package org.primeframework.mvc.action;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.example.action.ExecuteMethodThrowsCheckedException;
import org.example.action.ExecuteMethodThrowsExceptionAction;
import org.example.action.ExtensionInheritanceAction;
import org.example.action.HeadAction;
import org.example.action.PostAction;
import org.example.action.SimpleAction;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.DefaultActionConfigurationBuilder;
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * This class tests the default action invocation workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionInvocationWorkflowTest extends PrimeBaseTest {
  @Test
  public void action() throws Exception {
    SimpleAction simple = new SimpleAction();
    SimpleAction.invoked = false;
    ActionInvocation actionInvocation = makeActionInvocation(simple, HTTPMethod.POST, null);

    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(actionInvocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("success");
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore);
    workflow.perform(chain);

    assertTrue(SimpleAction.invoked);

    verify(ais, chain);
  }

  @Test
  public void actionExtension() throws Exception {
    ExtensionInheritanceAction action = new ExtensionInheritanceAction();
    ActionInvocation actionInvocation = makeActionInvocation(action, HTTPMethod.POST, "post");
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(actionInvocation);
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
    ExtensionInheritanceAction action = new ExtensionInheritanceAction();
    ActionInvocation actionInvocation = makeActionInvocation(action, HTTPMethod.GET, "get");
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(actionInvocation);
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
  public void actionThatThrowsCheckedException() throws Exception {
    ExecuteMethodThrowsCheckedException action = new ExecuteMethodThrowsCheckedException();
    ActionInvocation actionInvocation = makeActionInvocation(action, HTTPMethod.POST, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(actionInvocation);
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
  public void actionThatThrowsRuntimeException() throws Exception {
    ExecuteMethodThrowsExceptionAction action = new ExecuteMethodThrowsExceptionAction();
    ActionInvocation actionInvocation = makeActionInvocation(action, HTTPMethod.POST, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(actionInvocation);
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
  public void httpHeadMethod() throws Exception {
    HeadAction action = new HeadAction();
    ActionInvocation actionInvocation = makeActionInvocation(action, HTTPMethod.HEAD, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(actionInvocation);
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
  public void httpMethod() throws Exception {
    PostAction action = new PostAction();
    PostAction.invoked = false;
    ActionInvocation actionInvocation = makeActionInvocation(action, HTTPMethod.POST, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(actionInvocation);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("success");
    replay(resultStore);

    DefaultActionInvocationWorkflow workflow = new DefaultActionInvocationWorkflow(ais, resultStore);
    workflow.perform(chain);

    assertTrue(PostAction.invoked);

    verify(ais, chain);
  }

  /**
   * Makes an action invocation and configuration.
   *
   * @param action     The action object.
   * @param httpMethod The HTTP method.
   * @param extension  The extension.
   * @return The action invocation.
   */
  protected ActionInvocation makeActionInvocation(Object action, HTTPMethod httpMethod, String extension) {
    DefaultActionConfigurationBuilder builder = injector.getInstance(DefaultActionConfigurationBuilder.class);
    ActionConfiguration actionConfiguration = builder.build(action.getClass());
    return new ActionInvocation(action, actionConfiguration.executeMethods.get(httpMethod), actionConfiguration.uri, extension, actionConfiguration);
  }
}

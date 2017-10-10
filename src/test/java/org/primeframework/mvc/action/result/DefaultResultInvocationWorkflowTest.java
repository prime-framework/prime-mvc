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
package org.primeframework.mvc.action.result;

import org.example.action.user.EditAction;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.ForwardResult.ForwardImpl;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import com.google.inject.Binder;
import com.google.inject.Injector;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.fail;

/**
 * This class tests the default action invocation workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultResultInvocationWorkflowTest extends PrimeBaseTest {
  @Test
  public void actionLessWithDefaultForward() throws Exception {
    ActionInvocation ai = new ActionInvocation(null, null, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.clear();
    replay(resultStore);

    ResourceLocator resourceLocator = createStrictMock(ResourceLocator.class);
    replay(resourceLocator);

    ForwardResult result = createStrictMock(ForwardResult.class);
    expect(result.execute(isA(Forward.class))).andReturn(true);
    replay(result);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(ForwardResult.class)).andReturn(result);
    replay(injector);

    Binder binder = createStrictMock(Binder.class);
    expect(binder.bind(ForwardResult.class)).andReturn(null);
    replay(binder);

    ResultFactory.addResult(binder, Forward.class, ForwardResult.class);
    ResultFactory factory = new ResultFactory(injector);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, configuration, resultStore, resourceLocator, factory);
    workflow.perform(chain);

    verify(ais, resultStore, resourceLocator, injector, chain, binder);
  }

  @Test
  public void actionLessWithDefaultRedirect() throws Exception {
    ActionInvocation ai = new ActionInvocation(null, null, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.clear();
    replay(resultStore);

    ResourceLocator resourceLocator = createStrictMock(ResourceLocator.class);
    expect(resourceLocator.locateIndex(configuration.resourceDirectory() + "/templates")).andReturn("/foo/bar/");
    replay(resourceLocator);

    ForwardResult forwardResult = createStrictMock(ForwardResult.class);
    expect(forwardResult.execute(isA(Forward.class))).andReturn(false);
    replay(forwardResult);

    RedirectResult result = createStrictMock(RedirectResult.class);
    expect(result.execute(isA(Redirect.class))).andReturn(true);
    replay(result);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(ForwardResult.class)).andReturn(forwardResult);
    expect(injector.getInstance(RedirectResult.class)).andReturn(result);

    replay(injector);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    Binder binder = createStrictMock(Binder.class);
    expect(binder.bind(RedirectResult.class)).andReturn(null);
    replay(binder);

    ResultFactory.addResult(binder, Redirect.class, RedirectResult.class);
    ResultFactory factory = new ResultFactory(injector);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, configuration, resultStore, resourceLocator, factory);
    workflow.perform(chain);

    verify(ais, resultStore, resourceLocator, injector, chain, binder);
  }

  @Test
  public void actionLessWithoutDefault() throws Exception {
    ActionInvocation ai = new ActionInvocation(null, null, "/foo/bar", null, null);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.clear();
    replay(resultStore);

    ResourceLocator resourceLocator = createStrictMock(ResourceLocator.class);
    expect(resourceLocator.locateIndex(configuration.resourceDirectory() + "/templates")).andReturn(null);
    replay(resourceLocator);

    ForwardResult result = createStrictMock(ForwardResult.class);
    expect(result.execute(isA(Forward.class))).andReturn(false);
    replay(result);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(ForwardResult.class)).andReturn(result);
    replay(injector);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    Binder binder = createStrictMock(Binder.class);
    expect(binder.bind(ForwardResult.class)).andReturn(null);
    replay(binder);

    ResultFactory.addResult(binder, Forward.class, ForwardResult.class);
    ResultFactory factory = new ResultFactory(injector);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, configuration, resultStore, resourceLocator, factory);
    workflow.perform(chain);

    verify(ais, resultStore, resourceLocator, injector, chain, binder);
  }

  @Test
  public void actionMissingResult() throws Exception {
    ForwardImpl annotation = new ForwardImpl("/user/edit", "success");
    ActionInvocation ai = makeActionInvocation(HTTPMethod.POST, new EditAction(), "post", "/user/edit", "", "success", annotation);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    expect(resultStore.get()).andReturn("failure");
    resultStore.clear();
    replay(resultStore);

    ResourceLocator resourceLocator = createStrictMock(ResourceLocator.class);
    replay(resourceLocator);

    ForwardResult result = createStrictMock(ForwardResult.class);
    expect(result.execute(isA(Forward.class))).andReturn(true);
    replay(result);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(ForwardResult.class)).andReturn(result);
    replay(injector);

    Binder binder = createStrictMock(Binder.class);
    expect(binder.bind(ForwardResult.class)).andReturn(null);
    replay(binder);

    ResultFactory.addResult(binder, Forward.class, ForwardResult.class);
    ResultFactory factory = new ResultFactory(injector);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, configuration, resultStore, resourceLocator, factory);
    try {
      workflow.perform(chain);
    } catch (PrimeException e) {
      fail("Should not have thrown a PrimeException");
    }

    verify(ais, resultStore, resourceLocator, injector, chain, binder);
  }

  @Test
  public void actionSuppressResult() throws Exception {
    ActionInvocation ai = new ActionInvocation(null, null, null, null, null, null, false);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.clear();
    replay(resultStore);

    ResourceLocator resourceLocator = createStrictMock(ResourceLocator.class);
    replay(resourceLocator);

    Injector injector = createStrictMock(Injector.class);
    replay(injector);

    Binder binder = createStrictMock(Binder.class);
    expect(binder.bind(ForwardResult.class)).andReturn(null);
    replay(binder);

    ResultFactory.addResult(binder, Forward.class, ForwardResult.class);
    ResultFactory factory = new ResultFactory(injector);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, configuration, resultStore, resourceLocator, factory);
    workflow.perform(chain);

    verify(ais, resultStore, resourceLocator, injector, chain, binder);
  }

  @Test
  public void actionWithResult() throws Exception {
    ForwardImpl annotation = new ForwardImpl("/user/edit", "success");
    ActionInvocation ai = makeActionInvocation(HTTPMethod.POST, new EditAction(), "post", "/user/edit", "", "success", annotation);
    ActionInvocationStore ais = createStrictMock(ActionInvocationStore.class);
    expect(ais.getCurrent()).andReturn(ai);
    replay(ais);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    expect(resultStore.get()).andReturn("success");
    resultStore.clear();
    replay(resultStore);

    ResourceLocator resourceLocator = createStrictMock(ResourceLocator.class);
    replay(resourceLocator);

    ForwardResult result = createStrictMock(ForwardResult.class);
    expect(result.execute(annotation)).andReturn(true);
    replay(result);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(ForwardResult.class)).andReturn(result);
    replay(injector);

    Binder binder = createStrictMock(Binder.class);
    expect(binder.bind(ForwardResult.class)).andReturn(null);
    replay(binder);

    ResultFactory.addResult(binder, Forward.class, ForwardResult.class);
    ResultFactory factory = new ResultFactory(injector);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, configuration, resultStore, resourceLocator, factory);
    workflow.perform(chain);

    verify(ais, resultStore, resourceLocator, injector, chain, binder);
  }
}

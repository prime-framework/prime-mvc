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

import org.example.action.user.Edit;
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

import com.google.inject.Injector;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

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
    expect(resourceLocator.locate(ForwardResult.DIR)).andReturn("/foo/bar.ftl");
    replay(resourceLocator);

    ForwardResult result = createStrictMock(ForwardResult.class);
    result.execute(isA(Forward.class));
    replay(result);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(ForwardResult.class)).andReturn(result);
    replay(injector);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, resultStore, resourceLocator, injector);
    workflow.perform(chain);

    verify(ais, resultStore, resourceLocator, injector, chain);
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
    expect(resourceLocator.locate(ForwardResult.DIR)).andReturn(null);
    expect(resourceLocator.locateIndex(ForwardResult.DIR)).andReturn("/foo/bar/");
    replay(resourceLocator);

    RedirectResult result = createStrictMock(RedirectResult.class);
    result.execute(isA(Redirect.class));
    replay(result);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(RedirectResult.class)).andReturn(result);
    replay(injector);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    replay(chain);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, resultStore, resourceLocator, injector);
    workflow.perform(chain);

    verify(ais, resultStore, resourceLocator, injector, chain);
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
    expect(resourceLocator.locate(ForwardResult.DIR)).andReturn(null);
    expect(resourceLocator.locateIndex(ForwardResult.DIR)).andReturn(null);
    replay(resourceLocator);

    Injector injector = createStrictMock(Injector.class);
    replay(injector);

    WorkflowChain chain = createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    replay(chain);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, resultStore, resourceLocator, injector);
    workflow.perform(chain);

    verify(ais, resultStore, resourceLocator, injector, chain);
  }

  @Test
  public void actionWithResult() throws Exception {
    ForwardImpl annotation = new ForwardImpl("/user/edit", "success");
    ActionInvocation ai = makeActionInvocation(HTTPMethod.POST, new Edit(), "post", "/user/edit", "", "success", annotation, ForwardResult.class);
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
    result.execute(annotation);
    replay(result);

    Injector injector = createStrictMock(Injector.class);
    expect(injector.getInstance(ForwardResult.class)).andReturn(result);
    replay(injector);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, resultStore, resourceLocator, injector);
    workflow.perform(chain);

    verify(ais, resultStore, resourceLocator, injector, chain);
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

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, resultStore, resourceLocator, injector);
    workflow.perform(chain);

    verify(ais, resultStore, resourceLocator, injector, chain);
  }

  @Test
  public void actionMissingResult() throws Exception {
    ForwardImpl annotation = new ForwardImpl("/user/edit", "success");
    ActionInvocation ai = makeActionInvocation(HTTPMethod.POST, new Edit(), "post", "/user/edit", "", "success", annotation, ForwardResult.class);
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
    expect(resourceLocator.locate("/WEB-INF/templates")).andReturn(null);
    replay(resourceLocator);

    Injector injector = createStrictMock(Injector.class);
    replay(injector);

    DefaultResultInvocationWorkflow workflow = new DefaultResultInvocationWorkflow(ais, resultStore, resourceLocator, injector);
    try {
      workflow.perform(chain);
      fail("Should have thrown a PrimeException");
    } catch (PrimeException e) {
      // Expected
    }

    verify(ais, resultStore, resourceLocator, injector, chain);
  }
}

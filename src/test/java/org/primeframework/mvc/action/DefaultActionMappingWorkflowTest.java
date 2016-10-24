/*
 * Copyright (c) 2001-2016, Inversoft Inc., All Rights Reserved
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.example.action.ComplexRest;
import org.example.action.user.Edit;
import org.example.action.user.RESTEdit;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;

import com.google.inject.Injector;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.capture;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * This class tests the default action mapping workflow.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionMappingWorkflowTest extends PrimeBaseTest {
  @Test
  public void differentButtonClick() throws Exception {
    request.setUri("/admin/user/edit");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "/admin/user/cancel");
    request.setParameter("cancel", "Cancel");

    run("/admin/user/cancel", null);
  }

  @Test
  public void differentButtonClickRelativeURI() throws Exception {
    request.setUri("/admin/user/edit");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("cancel", "Cancel");

    run("/admin/user/cancel", null);
  }

  @Test
  public void extension() throws Exception {
    request.setUri("/admin/user/edit.xml");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    run("/admin/user/edit", "xml");
  }

  @Test
  public void redirectToIndex() throws Exception {
    request.setUri("/foo");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    ActionConfigurationProvider provider = EasyMock.createStrictMock(ActionConfigurationProvider.class);
    EasyMock.expect(provider.lookup("/foo")).andReturn(null);
    EasyMock.expect(provider.lookup("/foo/index")).andReturn(
        new ActionConfiguration(ComplexRest.class, new HashMap<>(),
            new HashMap<>(), new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashSet<>(), new ArrayList<>(), new HashMap<>(), "/foo/index", new ArrayList<>()
        ));
    EasyMock.replay(provider);

    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(store);

    Injector injector = EasyMock.createStrictMock(Injector.class);
    EasyMock.replay(injector);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    EasyMock.replay(chain);

    DefaultActionMappingWorkflow workflow = new DefaultActionMappingWorkflow(request, response, store, new DefaultActionMapper(provider, injector));
    workflow.perform(chain);

    assertEquals(response.getRedirect(), "/foo/");
    EasyMock.verify(provider, store, injector, chain);
  }

  @Test
  public void requestURI() throws Exception {
    request.setUri("/admin/user/edit");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    run("/admin/user/edit", null);
  }

  @Test
  public void requestURIContext() throws Exception {
    request.setUri("/context-path/admin/user/edit");
    request.setContextPath("/context-path");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    run("/admin/user/edit", null);
  }

  @Test
  public void uirParametersComplexWithWildcard() throws Exception {
    request.setUri("/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods = new HashMap<>();
    executeMethods.put(HTTPMethod.POST, new ExecuteMethodConfiguration(HTTPMethod.POST, Edit.class.getMethod("execute"), null));

    ActionConfigurationProvider provider = EasyMock.createStrictMock(ActionConfigurationProvider.class);
    EasyMock.expect(provider.lookup("/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff")).andReturn(null);
    EasyMock.expect(provider.lookup("/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff/index")).andReturn(null);
    EasyMock.expect(provider.lookup("/complex-rest/brian/static/pontarelli/then/a/bunch/of")).andReturn(null);
    EasyMock.expect(provider.lookup("/complex-rest/brian/static/pontarelli/then/a/bunch")).andReturn(null);
    EasyMock.expect(provider.lookup("/complex-rest/brian/static/pontarelli/then/a")).andReturn(null);
    EasyMock.expect(provider.lookup("/complex-rest/brian/static/pontarelli/then")).andReturn(null);
    EasyMock.expect(provider.lookup("/complex-rest/brian/static/pontarelli")).andReturn(null);
    EasyMock.expect(provider.lookup("/complex-rest/brian/static")).andReturn(null);
    EasyMock.expect(provider.lookup("/complex-rest/brian")).andReturn(null);
    EasyMock.expect(provider.lookup("/complex-rest")).andReturn(
        new ActionConfiguration(ComplexRest.class, executeMethods,
            new HashMap<>(), new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashSet<>(), new ArrayList<>(), new HashMap<>(), "/complex-rest", new ArrayList<>()
        ));
    EasyMock.replay(provider);

    Capture<ActionInvocation> capture = new Capture<>();
    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    store.setCurrent(capture(capture));
    store.removeCurrent();
    EasyMock.replay(store);

    Injector injector = EasyMock.createStrictMock(Injector.class);
    EasyMock.expect(injector.getInstance(ComplexRest.class)).andReturn(new ComplexRest());
    EasyMock.replay(injector);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionMappingWorkflow workflow = new DefaultActionMappingWorkflow(request, response, store, new DefaultActionMapper(provider, injector));
    workflow.perform(chain);

    ActionInvocation ai = capture.getValue();
    assertEquals(ai.actionURI, "/complex-rest");
    assertCollections(asList("brian", "static", "pontarelli", "then", "a", "bunch", "of", "stuff"), ai.uriParameters);
    assertNull(ai.extension);
    assertNotNull(ai.configuration);
    assertTrue(ai.executeResult);

    EasyMock.verify(provider, store, injector, chain);
  }

  @Test
  public void uriParameters() throws Exception {
    request.setUri("/admin/user/rest-edit/12");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods = new HashMap<>();
    executeMethods.put(HTTPMethod.POST, new ExecuteMethodConfiguration(HTTPMethod.POST, Edit.class.getMethod("execute"), null));

    ActionConfigurationProvider provider = EasyMock.createStrictMock(ActionConfigurationProvider.class);
    EasyMock.expect(provider.lookup("/admin/user/rest-edit/12")).andReturn(null);
    EasyMock.expect(provider.lookup("/admin/user/rest-edit/12/index")).andReturn(null);
    EasyMock.expect(provider.lookup("/admin/user/rest-edit")).andReturn(new ActionConfiguration(RESTEdit.class, executeMethods,
        new HashMap<>(), new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashMap<>(),
        new HashMap<>(), new HashMap<>(), new HashSet<>(), new ArrayList<>(), new HashMap<>(), "/admin/user/rest-edit", new ArrayList<>()
    ));
    EasyMock.replay(provider);

    Capture<ActionInvocation> capture = new Capture<>();
    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    store.setCurrent(capture(capture));
    store.removeCurrent();
    EasyMock.replay(store);

    Injector injector = EasyMock.createStrictMock(Injector.class);
    EasyMock.expect(injector.getInstance(RESTEdit.class)).andReturn(new RESTEdit());
    EasyMock.replay(injector);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionMappingWorkflow workflow = new DefaultActionMappingWorkflow(request, response, store, new DefaultActionMapper(provider, injector));
    workflow.perform(chain);

    ActionInvocation ai = capture.getValue();
    assertEquals(ai.actionURI, "/admin/user/rest-edit");
    assertCollections(singletonList("12"), ai.uriParameters);
    assertNull(ai.extension);
    assertNotNull(ai.configuration);
    assertTrue(ai.executeResult);

    EasyMock.verify(provider, store, injector, chain);
  }

  private void assertCollections(Collection<String> strings, Collection<String> strings1) {
    if (strings1.size() != strings.size()) {
      fail(strings + " not equal to " + strings1);
    }

    Iterator<String> i1 = strings.iterator();
    Iterator<String> i2 = strings1.iterator();
    while (i1.hasNext()) {
      if (!i1.next().equals(i2.next())) {
        fail(strings + " not equal to " + strings1);
      }
    }
  }

  private void run(String uri, String extension) throws Exception {
    Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods = new HashMap<>();
    executeMethods.put(HTTPMethod.POST, new ExecuteMethodConfiguration(HTTPMethod.POST, Edit.class.getMethod("post"), null));

    ActionConfigurationProvider provider = EasyMock.createStrictMock(ActionConfigurationProvider.class);
    EasyMock.expect(provider.lookup(uri)).andReturn(
        new ActionConfiguration(Edit.class, executeMethods,
            new HashMap<>(), new ArrayList<>(), null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashSet<>(), new ArrayList<>(), new HashMap<>(), uri, new ArrayList<>()
        ));
    EasyMock.replay(provider);

    Capture<ActionInvocation> capture = new Capture<>();
    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    store.setCurrent(capture(capture));
    store.removeCurrent();
    EasyMock.replay(store);

    Injector injector = EasyMock.createStrictMock(Injector.class);
    EasyMock.expect(injector.getInstance(Edit.class)).andReturn(new Edit());
    EasyMock.replay(injector);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionMappingWorkflow workflow = new DefaultActionMappingWorkflow(request, response, store, new DefaultActionMapper(provider, injector));
    workflow.perform(chain);

    ActionInvocation ai = capture.getValue();
    assertEquals(ai.actionURI, uri);
    assertEquals(ai.extension, extension);
    assertNotNull(ai.configuration);
    assertTrue(ai.executeResult);

    EasyMock.verify(provider, store, injector, chain);
  }
}
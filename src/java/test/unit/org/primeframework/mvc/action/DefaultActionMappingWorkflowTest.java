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
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.easymock.EasyMock;
import org.example.action.ComplexRest;
import org.example.action.user.Edit;
import org.example.action.user.RESTEdit;
import org.primeframework.mvc.ObjectFactory;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.action.config.DefaultActionConfiguration;
import org.primeframework.mvc.servlet.WorkflowChain;
import org.primeframework.mvc.test.Capture;
import org.primeframework.mvc.test.JCatapultBaseTest;
import org.testng.annotations.Test;

import static java.util.Arrays.*;
import static org.testng.Assert.*;

/**
 * <p> This class tests the default action mapping workflow. </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultActionMappingWorkflowTest extends JCatapultBaseTest {
  @Test
  public void testDifferentButtonClick() throws IOException, ServletException {
    request.setUri("/admin/user/edit");
    request.setPost(true);
    request.setParameter("__jc_a_submit", "");
    request.setParameter("__jc_a_cancel", "/admin/user/cancel");
    request.setParameter("cancel", "Cancel");

    run("/admin/user/cancel", null);
  }

  @Test
  public void testDifferentButtonClickRelativeURI() throws IOException, ServletException {
    request.setUri("/admin/user/edit");
    request.setPost(true);
    request.setParameter("__jc_a_submit", "");
    request.setParameter("__jc_a_cancel", "cancel");
    request.setParameter("cancel", "Cancel");

    run("/admin/user/cancel", null);
  }

  @Test
  public void testRequestURI() throws IOException, ServletException {
    request.setUri("/admin/user/edit");
    request.setPost(true);
    request.setParameter("__jc_a_submit", "");
    request.setParameter("__jc_a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    run("/admin/user/edit", null);
  }

  @Test
  public void testRequestURIContext() throws IOException, ServletException {
    request.setUri("/context-path/admin/user/edit");
    request.setContextPath("/context-path");
    request.setPost(true);
    request.setParameter("__jc_a_submit", "");
    request.setParameter("__jc_a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    run("/admin/user/edit", null);
  }

  @Test
  public void testExtension() throws IOException, ServletException {
    request.setUri("/admin/user/edit.xml");
    request.setPost(true);
    request.setParameter("__jc_a_submit", "");
    request.setParameter("__jc_a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    run("/admin/user/edit", "xml");
  }

  @Test
  public void testURIParameters() throws IOException, ServletException {
    request.setUri("/admin/user/rest-edit/12");
    request.setPost(true);
    request.setParameter("__jc_a_submit", "");
    request.setParameter("__jc_a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    ActionConfigurationProvider provider = EasyMock.createStrictMock(ActionConfigurationProvider.class);
    EasyMock.expect(provider.lookup("/admin/user/rest-edit/12")).andReturn(null);
    EasyMock.expect(provider.lookup("/admin/user/rest-edit/12/index")).andReturn(null);
    EasyMock.expect(provider.lookup("/admin/user/rest-edit")).andReturn(new DefaultActionConfiguration(RESTEdit.class, "/admin/user/rest-edit"));
    EasyMock.replay(provider);

    Capture capture = new Capture();
    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    store.setCurrent((ActionInvocation) capture.capture());
    store.removeCurrent();
    EasyMock.replay(store);

    ObjectFactory factory = EasyMock.createStrictMock(ObjectFactory.class);
    EasyMock.expect(factory.create(RESTEdit.class)).andReturn(new RESTEdit());
    EasyMock.replay(factory);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionMappingWorkflow workflow = new DefaultActionMappingWorkflow(request, response, store, new DefaultActionMapper(provider, factory));
    workflow.perform(chain);

    ActionInvocation ai = (ActionInvocation) capture.object;
    assertEquals("/admin/user/rest-edit", ai.actionURI());
    assertCollections(asList("12"), ai.uriParameters());
    assertNull(ai.extension());
    assertNotNull(ai.configuration());
    assertTrue(ai.executeAction());
    assertTrue(ai.executeResult());

    EasyMock.verify(provider, store, factory, chain);
  }

  @Test
  public void testURIParametersComplexWithWildcard() throws IOException, ServletException {
    request.setUri("/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff");
    request.setPost(true);
    request.setParameter("__jc_a_submit", "");
    request.setParameter("__jc_a_cancel", "cancel");
    request.setParameter("submit", "Submit");

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
    EasyMock.expect(provider.lookup("/complex-rest")).andReturn(new DefaultActionConfiguration(ComplexRest.class, "/complex-rest"));
    EasyMock.replay(provider);

    Capture capture = new Capture();
    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    store.setCurrent((ActionInvocation) capture.capture());
    store.removeCurrent();
    EasyMock.replay(store);

    ObjectFactory factory = EasyMock.createStrictMock(ObjectFactory.class);
    EasyMock.expect(factory.create(ComplexRest.class)).andReturn(new ComplexRest());
    EasyMock.replay(factory);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionMappingWorkflow workflow = new DefaultActionMappingWorkflow(request, response, store, new DefaultActionMapper(provider, factory));
    workflow.perform(chain);

    ActionInvocation ai = (ActionInvocation) capture.object;
    assertEquals("/complex-rest", ai.actionURI());
    assertCollections(asList("brian", "static", "pontarelli", "then", "a", "bunch", "of", "stuff"), ai.uriParameters());
    assertNull(ai.extension());
    assertNotNull(ai.configuration());
    assertTrue(ai.executeAction());
    assertTrue(ai.executeResult());

    EasyMock.verify(provider, store, factory, chain);
  }

  @Test
  public void testRedirectToIndex() throws IOException, ServletException {
    request.setUri("/foo");
    request.setPost(true);
    request.setParameter("__jc_a_submit", "");
    request.setParameter("__jc_a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    ActionConfigurationProvider provider = EasyMock.createStrictMock(ActionConfigurationProvider.class);
    EasyMock.expect(provider.lookup("/foo")).andReturn(null);
    EasyMock.expect(provider.lookup("/foo/index")).andReturn(new DefaultActionConfiguration(ComplexRest.class, "/foo/index"));
    EasyMock.replay(provider);

    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(store);

    ObjectFactory factory = EasyMock.createStrictMock(ObjectFactory.class);
    EasyMock.replay(factory);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    EasyMock.replay(chain);

    DefaultActionMappingWorkflow workflow = new DefaultActionMappingWorkflow(request, response, store, new DefaultActionMapper(provider, factory));
    workflow.perform(chain);

    assertEquals("/foo/", response.getRedirect());
    EasyMock.verify(provider, store, factory, chain);
  }

  private void run(String uri, String extension) throws IOException, ServletException {
    ActionConfigurationProvider provider = EasyMock.createStrictMock(ActionConfigurationProvider.class);
    EasyMock.expect(provider.lookup(uri)).andReturn(new DefaultActionConfiguration(Edit.class, uri));
    EasyMock.replay(provider);

    Capture capture = new Capture();
    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    store.setCurrent((ActionInvocation) capture.capture());
    store.removeCurrent();
    EasyMock.replay(store);

    ObjectFactory factory = EasyMock.createStrictMock(ObjectFactory.class);
    EasyMock.expect(factory.create(Edit.class)).andReturn(new Edit());
    EasyMock.replay(factory);

    WorkflowChain chain = EasyMock.createStrictMock(WorkflowChain.class);
    chain.continueWorkflow();
    EasyMock.replay(chain);

    DefaultActionMappingWorkflow workflow = new DefaultActionMappingWorkflow(request, response, store, new DefaultActionMapper(provider, factory));
    workflow.perform(chain);

    ActionInvocation ai = (ActionInvocation) capture.object;
    assertEquals(uri, ai.actionURI());
    assertEquals(extension, ai.extension());
    assertNotNull(ai.configuration());
    assertTrue(ai.executeAction());
    assertTrue(ai.executeResult());

    EasyMock.verify(provider, store, factory, chain);
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
}
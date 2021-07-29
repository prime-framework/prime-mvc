/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.inject.Injector;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.example.action.user.EditAction;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.capture;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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

    run("/admin/user/cancel", "/admin/user/cancel", null);
  }

  @Test
  public void differentButtonClickRelativeURI() throws Exception {
    request.setUri("/admin/user/edit");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("cancel", "Cancel");

    run("/admin/user/cancel", "/admin/user/cancel", null);
  }

  @Test
  public void extension() throws Exception {
    request.setUri("/admin/user/edit.xml");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    run("/admin/user/edit.xml", "/admin/user/edit", "xml");
  }

  @Test
  public void requestURI() throws Exception {
    request.setUri("/admin/user/edit");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    run("/admin/user/edit", "/admin/user/edit", null);
  }

  @Test
  public void requestURIContext() throws Exception {
    request.setUri("/context-path/admin/user/edit");
    request.setContextPath("/context-path");
    request.setPost(true);
    request.setParameter("__a_submit", "");
    request.setParameter("__a_cancel", "cancel");
    request.setParameter("submit", "Submit");

    run("/admin/user/edit", "/admin/user/edit", null);
  }

  private void run(String fullURI, String uri, String extension) throws Exception {
    Map<HTTPMethod, ExecuteMethodConfiguration> executeMethods = new HashMap<>();
    executeMethods.put(HTTPMethod.POST, new ExecuteMethodConfiguration(HTTPMethod.POST, EditAction.class.getMethod("post"), null));

    ActionConfigurationProvider provider = EasyMock.createStrictMock(ActionConfigurationProvider.class);
    EasyMock.expect(provider.lookup(fullURI)).andReturn(
        new ActionInvocation(
            EditAction.class, null, uri, extension,
            new ActionConfiguration(EditAction.class, executeMethods,
                new HashMap<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>(), new ArrayList<>(), new HashMap<>(),
                new ArrayList<>(), new HashMap<>(), new HashMap<>(), null, new HashMap<>(), new HashSet<>(), new ArrayList<>(),
                new ArrayList<>(), new HashMap<>(), uri, new ArrayList<>(), null)
        )
    );
    EasyMock.replay(provider);

    Capture<ActionInvocation> capture = Capture.newInstance();
    ActionInvocationStore store = EasyMock.createStrictMock(ActionInvocationStore.class);
    store.setCurrent(capture(capture));
    store.removeCurrent();
    EasyMock.replay(store);

    Injector injector = EasyMock.createStrictMock(Injector.class);
    EasyMock.expect(injector.getInstance(EditAction.class)).andReturn(new EditAction());
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
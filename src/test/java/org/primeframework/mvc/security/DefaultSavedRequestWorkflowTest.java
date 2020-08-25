/*
 * Copyright (c) 2015-2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security;

import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.result.SavedRequestTools;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * @author Brian Pontarelli
 */
public class DefaultSavedRequestWorkflowTest extends PrimeBaseTest {
  @Test
  public void performNoSavedRequest() throws Exception {
    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    DefaultSavedRequestWorkflow workflow = new DefaultSavedRequestWorkflow(configuration, new DefaultEncryptor(new DefaultCipherProvider(configuration), objectMapper), wrapper, response);

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);

    assertSame(wrapper.getRequest(), request);
  }

  @Test
  public void performSavedRequestGET() throws Exception {
    container.getUserAgent().addCookie(request, SavedRequestTools.toCookie(new SavedHttpRequest(HTTPMethod.GET, "/secure?test=value&test2=value2", null), configuration, new DefaultEncryptor(new DefaultCipherProvider(configuration), objectMapper)));
    SavedRequestTools.markExecuted(configuration, response);
    request.setUri("/secure");

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    DefaultSavedRequestWorkflow workflow = new DefaultSavedRequestWorkflow(configuration, new DefaultEncryptor(new DefaultCipherProvider(configuration), objectMapper), wrapper, response);

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);

    assertNotSame(wrapper.getRequest(), request);
    assertEquals(wrapper.getRequestURI(), "/secure");
    assertTrue(wrapper.getParameterMap().isEmpty());
  }

  @Test
  public void performSavedRequestPOST() throws Exception {
    Map<String, String[]> parameters = new HashMap<>();
    parameters.put("test", new String[]{"value"});
    parameters.put("test2", new String[]{"value2"});

    container.getUserAgent().addCookie(request, SavedRequestTools.toCookie(new SavedHttpRequest(HTTPMethod.POST, "/secure", parameters), configuration, new DefaultEncryptor(new DefaultCipherProvider(configuration), objectMapper)));
    SavedRequestTools.markExecuted(configuration, response);
    request.setUri("/secure");

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
    DefaultSavedRequestWorkflow workflow = new DefaultSavedRequestWorkflow(configuration, new DefaultEncryptor(new DefaultCipherProvider(configuration), objectMapper), wrapper, response);

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);

    assertNotSame(wrapper.getRequest(), request);
    assertEquals(wrapper.getRequestURI(), "/secure");
    assertEquals(wrapper.getParameterMap().get("test"), new String[]{"value"});
    assertEquals(wrapper.getParameterMap().get("test2"), new String[]{"value2"});
  }
}

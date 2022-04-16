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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.result.SavedRequestTools;
import org.primeframework.mvc.http.Cookie;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * @author Brian Pontarelli
 */
public class DefaultSavedRequestWorkflowTest extends PrimeBaseTest {
  @Test
  public void performNoSavedRequest() throws Exception {
    DefaultSavedRequestWorkflow workflow = new DefaultSavedRequestWorkflow(configuration, new DefaultEncryptor(new DefaultCipherProvider(configuration)), objectMapper, request, response);

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);
  }

  @Test
  public void performSavedRequestPOST() throws Exception {
    Map<String, List<String>> parameters = new HashMap<>();
    parameters.put("test", List.of("value"));
    parameters.put("test2", List.of("value2"));

    Cookie cookie = SavedRequestTools.toCookie(new SavedHttpRequest(HTTPMethod.POST, "/secure", parameters), configuration, new DefaultEncryptor(new DefaultCipherProvider(configuration)), objectMapper);
    cookie.setValue("ready_" + cookie.getValue());
    request.addCookies(cookie);
    request.setPath("/secure");

    DefaultSavedRequestWorkflow workflow = new DefaultSavedRequestWorkflow(configuration, new DefaultEncryptor(new DefaultCipherProvider(configuration)), objectMapper, request, response);

    WorkflowChain workflowChain = createStrictMock(WorkflowChain.class);
    workflowChain.continueWorkflow();
    replay(workflowChain);

    workflow.perform(workflowChain);

    verify(workflowChain);

    assertEquals(request.getPath(), "/secure");
    assertEquals(request.getParameters().get("test"), List.of("value"));
    assertEquals(request.getParameters().get("test2"), List.of("value2"));
  }
}

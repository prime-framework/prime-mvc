/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletException;
import java.io.IOException;

import org.primeframework.mock.servlet.MockHttpServletRequest.Method;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.SaveRequestResult.SaveRequestImpl;
import org.primeframework.mvc.action.result.annotation.SaveRequest;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * This class tests the SaveRequest result.
 *
 * @author Brian Pontarelli
 */
public class SaveRequestResultTest extends PrimeBaseTest {
  @Inject public ExpressionEvaluator expressionEvaluator;

  @Inject public MessageStore messageStore;

  @Test
  public void saveRequestGET() throws IOException, ServletException {
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    request.setUri("/test");
    request.setMethod(Method.GET);
    request.setParameter("param1", "value1");
    request.setParameter("param2", "value2");

    SaveRequest annotation = new SaveRequestImpl("/login", "unauthenticated", true, false);
    SaveRequestResult result = new SaveRequestResult(messageStore, expressionEvaluator, response, request, store);
    result.execute(annotation);

    assertEquals(session.getAttribute(SavedHttpRequest.INITIAL_SESSION_KEY), new SavedHttpRequest("/test?param1=value1&param2=value2", null));
    assertEquals(response.getRedirect(), "/login");

    verify(store);
  }

  @Test
  public void saveRequestPOST() throws IOException, ServletException {
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    request.setUri("/test");
    request.setMethod(Method.POST);
    request.setParameter("param1", "value1");
    request.setParameter("param2", "value2");

    SaveRequest annotation = new SaveRequestImpl("/login", "unauthenticated", true, false);
    SaveRequestResult result = new SaveRequestResult(messageStore, expressionEvaluator, response, request, store);
    result.execute(annotation);

    assertEquals(session.getAttribute(SavedHttpRequest.INITIAL_SESSION_KEY), new SavedHttpRequest("/test", request.getParameterMap()));
    assertEquals(response.getRedirect(), "/login");

    verify(store);
  }
}
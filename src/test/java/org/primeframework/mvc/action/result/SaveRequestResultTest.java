/*
 * Copyright (c) 2015-2018, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primeframework.mock.servlet.MockHttpServletRequest.Method;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.SaveRequestResult.SaveRequestImpl;
import org.primeframework.mvc.action.result.annotation.SaveRequest;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.CipherProvider;
import org.primeframework.mvc.security.DefaultCipherProvider;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import static java.util.Collections.singletonList;
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

  @Inject public ObjectMapper objectMapper;

  @Test
  public void saveRequestGET() throws IOException, NoSuchAlgorithmException {
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    request.setUri("/test");
    request.setMethod(Method.GET);
    request.setParameter("param1", "value1");
    request.setParameter("param2", "value2");

    CipherProvider cipherProvider = new DefaultCipherProvider(configuration);
    SaveRequest annotation = new SaveRequestImpl("/login", "unauthenticated", true, false);
    SaveRequestResult result = new SaveRequestResult(messageStore, expressionEvaluator, response, request, store, configuration, objectMapper, cipherProvider);
    result.execute(annotation);

    assertCookieEquals(response.getCookies(), singletonList(SavedRequestTools.toCookie(new SavedHttpRequest(HTTPMethod.GET, "/test?param1=value1&param2=value2", null), objectMapper, configuration, cipherProvider)));
    assertEquals(response.getRedirect(), "/login");

    verify(store);
  }

  @Test
  public void saveRequestPOST() throws IOException, NoSuchAlgorithmException {
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    request.setUri("/test");
    request.setMethod(Method.POST);
    request.setParameter("param1", "value1");
    request.setParameter("param2", "value2");

    CipherProvider cipherProvider = new DefaultCipherProvider(configuration);
    SaveRequest annotation = new SaveRequestImpl("/login", "unauthenticated", true, false);
    SaveRequestResult result = new SaveRequestResult(messageStore, expressionEvaluator, response, request, store, configuration, objectMapper, cipherProvider);
    result.execute(annotation);

    assertCookieEquals(response.getCookies(), singletonList(SavedRequestTools.toCookie(new SavedHttpRequest(HTTPMethod.POST, "/test", request.getParameterMap()), objectMapper, configuration, cipherProvider)));
    assertEquals(response.getRedirect(), "/login");

    verify(store);
  }

  @Test
  public void saveRequestPOST_tooBig() throws IOException, NoSuchAlgorithmException {
    // By default Tomcat limits the HTTP Header to 8k (see Tomcat maxHttpHeaderSize)
    // If we think we might be surpassing that size, we should skip the save request otherwise we'll return a 500 to the client

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    Map<String, List<String>> parameters = new HashMap<>();
    parameters.put("largeParam1", new ArrayList<>(Arrays.asList(new String(new char[2048]).replace('\0', 'a'))));
    parameters.put("largeParam2", new ArrayList<>(Arrays.asList(new String(new char[2048]).replace('\0', 'b'))));
    parameters.put("largeParam3", new ArrayList<>(Arrays.asList(new String(new char[2048]).replace('\0', 'c'))));
    parameters.put("largeParam4", new ArrayList<>(Arrays.asList(new String(new char[2048]).replace('\0', 'd'))));
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", parameters, null, true));
    replay(store);

    request.setUri("/test");
    request.setMethod(Method.POST);
    request.setParameter("largeParam1", parameters.get("largeParam1").get(0));
    request.setParameter("largeParam2", parameters.get("largeParam2").get(0));
    request.setParameter("largeParam3", parameters.get("largeParam3").get(0));
    request.setParameter("largeParam4", parameters.get("largeParam4").get(0));

    CipherProvider cipherProvider = new DefaultCipherProvider(configuration);
    SaveRequest annotation = new SaveRequestImpl("/login", "unauthenticated", true, false);
    SaveRequestResult result = new SaveRequestResult(messageStore, expressionEvaluator, response, request, store, configuration, objectMapper, cipherProvider);
    result.execute(annotation);

    // Expect no cookies in the response, sadly, the cookie was just too big and we omitted it from the HTTP response.
    assertCookieEquals(response.getCookies(), Collections.emptyList());
    assertEquals(response.getRedirect(), "/login");

    verify(store);
  }

  private void assertCookieEquals(List<Cookie> actual, List<Cookie> expected) {
    assertEquals(actual.size(), expected.size(), "Lists are not the same length");
    for (int i = 0; i < actual.size(); i++) {
      assertEquals(actual.get(i).getComment(), expected.get(i).getComment());
      assertEquals(actual.get(i).getDomain(), expected.get(i).getDomain());
      assertEquals(actual.get(i).getMaxAge(), expected.get(i).getMaxAge());
      assertEquals(actual.get(i).getName(), expected.get(i).getName());
      assertEquals(actual.get(i).getPath(), expected.get(i).getPath());
      assertEquals(actual.get(i).getSecure(), expected.get(i).getSecure());
      assertEquals(actual.get(i).getValue(), expected.get(i).getValue());
      assertEquals(actual.get(i).getVersion(), expected.get(i).getVersion());
    }
  }
}
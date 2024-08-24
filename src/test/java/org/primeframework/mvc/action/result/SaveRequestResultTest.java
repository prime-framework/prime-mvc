/*
 * Copyright (c) 2015-2024, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.SaveRequestResult.SaveRequestImpl;
import org.primeframework.mvc.action.result.annotation.SaveRequest;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.DefaultCipherProvider;
import org.primeframework.mvc.security.DefaultEncryptor;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.util.CookieTools;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
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

  @DataProvider
  public Object[][] postScenarios() {
    return new Object[][]{
        {true, "http://localhost"},
        {true, "http://myhouse"},
        {false, "http://localhost"},
        {false, "http://myhouse"}
    };
  }

  @Test
  public void saveRequestGET() throws Exception {
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    HTTPRequest request = new HTTPRequest();
    HTTPResponse response = new HTTPResponse(null, request);
    request.setPath("/test");
    request.setMethod(HTTPMethod.GET);
    request.addURLParameter("param1", "value1");
    request.addURLParameter("param2", "value2");

    Encryptor encryptor = new DefaultEncryptor(new DefaultCipherProvider(configuration));
    SaveRequest annotation = new SaveRequestImpl("/login", "unauthenticated", true, false, false);
    SaveRequestResult result = new SaveRequestResult(messageStore, expressionEvaluator, response, request, store, configuration, encryptor, objectMapper);
    result.execute(annotation);

    // The cookie value will be different each time because the initialization vector is unique per request. Decrypt the actual value to compare it to the expected.
    SavedHttpRequest actual = CookieTools.fromJSONCookie(response.getCookies().get(0).value,
                                                         SavedHttpRequest.class,
                                                         true,
                                                         true,
                                                         encryptor,
                                                         objectMapper);
    SavedHttpRequest expected = new SavedHttpRequest(HTTPMethod.GET, "/test?param1=value1&param2=value2", null);
    assertEquals(actual, expected);

    assertEquals(response.getRedirect(), "/login");

    verify(store);
  }

  @Test(dataProvider = "postScenarios")
  public void saveRequestPOST(boolean allowPost, String origin) throws Exception {
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    HTTPRequest request = new HTTPRequest();
    HTTPResponse response = new HTTPResponse(null, request);
    request.setScheme("http");
    request.setHost("localhost");
    request.setPath("/test");
    request.setMethod(HTTPMethod.POST);
    request.addURLParameter("param1", "value1");
    request.addURLParameter("param2", "value2");
    request.addHeader("Origin", origin);

    Encryptor encryptor = new DefaultEncryptor(new DefaultCipherProvider(configuration));
    SaveRequest annotation = new SaveRequestImpl("/login", "unauthenticated", true, false, allowPost);
    SaveRequestResult result = new SaveRequestResult(messageStore, expressionEvaluator, response, request, store, configuration, encryptor, objectMapper);
    result.execute(annotation);

    if (allowPost && origin.equals(request.getBaseURL())) {
      // The cookie value will be different each time because the initialization vector is unique per request. Decrypt the actual value to compare it to the expected.
      SavedHttpRequest actual = CookieTools.fromJSONCookie(response.getCookies().get(0).value,
                                                           SavedHttpRequest.class,
                                                           true,
                                                           true,
                                                           encryptor,
                                                           objectMapper);
      SavedHttpRequest expected = new SavedHttpRequest(HTTPMethod.POST, "/test", request.getParameters());
      assertEquals(actual, expected);
    } else {
      // Note that POST by default does not work with saved request, so expect it to not work at all yo.
      assertEquals(response.getCookies(), List.of());
    }

    assertEquals(response.getRedirect(), "/login");

    verify(store);
  }

  @Test
  public void saveRequestPOST_tooBig() throws IOException {
    // By default, Tomcat limits the HTTP Header to 8 KB (see Tomcat maxHttpHeaderSize)
    // If we think we might be surpassing that size, we should skip the save request otherwise we'll return a 500 to the client

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    Map<String, List<String>> parameters = new HashMap<>();
    parameters.put("largeParam1", new ArrayList<>(List.of(new String(new char[2048]).replace('\0', 'a'))));
    parameters.put("largeParam2", new ArrayList<>(List.of(new String(new char[2048]).replace('\0', 'b'))));
    parameters.put("largeParam3", new ArrayList<>(List.of(new String(new char[2048]).replace('\0', 'c'))));
    parameters.put("largeParam4", new ArrayList<>(List.of(new String(new char[2048]).replace('\0', 'd'))));
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", parameters, null, true));
    replay(store);

    HTTPRequest request = new HTTPRequest();
    HTTPResponse response = new HTTPResponse(null, request);
    request.setPath("/test");
    request.setMethod(HTTPMethod.POST);
    request.addURLParameter("largeParam1", parameters.get("largeParam1").get(0));
    request.addURLParameter("largeParam2", parameters.get("largeParam2").get(0));
    request.addURLParameter("largeParam3", parameters.get("largeParam3").get(0));
    request.addURLParameter("largeParam4", parameters.get("largeParam4").get(0));

    Encryptor encryptor = new DefaultEncryptor(new DefaultCipherProvider(configuration));
    SaveRequest annotation = new SaveRequestImpl("/login", "unauthenticated", true, false, false);
    SaveRequestResult result = new SaveRequestResult(messageStore, expressionEvaluator, response, request, store, configuration, encryptor, objectMapper);
    result.execute(annotation);

    // Expect no cookies in the response, sadly, the cookie was just too big and we omitted it from the HTTP response.
    assertEquals(simulator.userAgent.getCookies(request), List.of());
    assertEquals(response.getRedirect(), "/login");

    verify(store);
  }
}

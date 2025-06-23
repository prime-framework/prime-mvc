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
import java.util.List;

import io.fusionauth.http.Cookie;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.ReexecuteSavedRequestResult.ReexecuteSavedRequestImpl;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.CBCCipherProvider;
import org.primeframework.mvc.security.DefaultEncryptor;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.GCMCipherProvider;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * This class tests the Saved Request result.
 *
 * @author Brian Pontarelli
 */
public class ReexecuteSavedRequestResultTest extends PrimeBaseTest {
  @Test
  public void noSavedRequest() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HTTPRequest request = new HTTPRequest();
    HTTPResponse response = new HTTPResponse();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    ReexecuteSavedRequest redirect = new ReexecuteSavedRequestImpl("/", "success", true, false);
    ReexecuteSavedRequestResult result = new ReexecuteSavedRequestResult(messageStore, ee, response, request, store, configuration, new DefaultEncryptor(new CBCCipherProvider(configuration), new GCMCipherProvider(configuration)), objectMapper);
    result.execute(redirect);

    verify(ee, store, messageStore);

    assertEquals(response.getStatus(), 301);
    assertEquals(response.getRedirect(), "/");
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  @Test
  public void savedRequest() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    SavedHttpRequest savedRequest = new SavedHttpRequest(HTTPMethod.GET, "/secure?test=value1&test2=value2", null);
    simulator.userAgent.addCookie(SavedRequestTools.toCookie(savedRequest, configuration, new DefaultEncryptor(new CBCCipherProvider(configuration), new GCMCipherProvider(configuration)), objectMapper));

    List<Message> messages = new ArrayList<>();
    Encryptor encryptor = new DefaultEncryptor(new CBCCipherProvider(configuration), new GCMCipherProvider(configuration));
    Cookie cookie = SavedRequestTools.toCookie(savedRequest, configuration, encryptor, objectMapper);

    HTTPRequest request = new HTTPRequest().with(r -> r.addCookies(cookie));
    HTTPResponse response = new HTTPResponse();
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    ReexecuteSavedRequest redirect = new ReexecuteSavedRequestImpl("/", "success", true, false);
    ReexecuteSavedRequestResult result = new ReexecuteSavedRequestResult(messageStore, ee, response, request, store, configuration, new DefaultEncryptor(new CBCCipherProvider(configuration), new GCMCipherProvider(configuration)), objectMapper);
    result.execute(redirect);

    verify(ee, store, messageStore);

    assertEquals(response.getCookies().size(), 1);
    assertEquals(response.getCookies().getFirst().name, configuration.savedRequestCookieName);
    assertEquals(response.getCookies().getFirst().maxAge.longValue(), 0);
    assertFalse(response.getCookies().getFirst().value.startsWith("ready_"));
    assertEquals(response.getStatus(), 301);
    assertEquals(response.getRedirect(), "/secure?test=value1&test2=value2");
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }
}

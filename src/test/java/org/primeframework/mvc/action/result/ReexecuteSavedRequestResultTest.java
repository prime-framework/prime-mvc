/*
 * Copyright (c) 2015-2019, Inversoft Inc., All Rights Reserved
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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.ReexecuteSavedRequestResult.ReexecuteSavedRequestImpl;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.FlashScope;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.DefaultCipherProvider;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * This class tests the Saved Request result.
 *
 * @author Brian Pontarelli
 */
public class ReexecuteSavedRequestResultTest extends PrimeBaseTest {
  @Test
  public void noSavedRequest() throws IOException, ServletException, NoSuchAlgorithmException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(FlashScope.KEY)).andReturn(messages);
    expect(request.getCookies()).andReturn(null);
    expect(request.getContextPath()).andReturn("");
    expect(request.getRequestURI()).andReturn("/");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.sendRedirect("/");
    response.setStatus(301);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    ReexecuteSavedRequest redirect = new ReexecuteSavedRequestImpl("/", "success", true, false);
    ReexecuteSavedRequestResult result = new ReexecuteSavedRequestResult(messageStore, ee, response, request, store, new DefaultCipherProvider(configuration), configuration, objectMapper);
    result.execute(redirect);

    verify(response, request, ee, store, messageStore);
  }

  @Test
  public void savedRequest() throws IOException, ServletException, NoSuchAlgorithmException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    SavedHttpRequest savedRequest = new SavedHttpRequest(HTTPMethod.GET, "/secure?test=value1&test2=value2", null);
    HttpSession session = createStrictMock(HttpSession.class);
    session.setAttribute(SavedHttpRequest.LOGGED_IN_SESSION_KEY, savedRequest);
    replay(session);

    List<Message> messages = new ArrayList<>();
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    DefaultCipherProvider cipherProvider = new DefaultCipherProvider(configuration);
    Cookie cookie = SavedRequestTools.toCookie(savedRequest, objectMapper, configuration, cipherProvider);
    expect(request.getAttribute(FlashScope.KEY)).andReturn(messages);
    expect(request.getCookies()).andReturn(new Cookie[]{cookie});
    expect(request.getSession(true)).andReturn(session);
    expect(request.getContextPath()).andReturn("");
    expect(request.getRequestURI()).andReturn("/");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.addCookie(cookie);
    response.sendRedirect("/secure?test=value1&test2=value2");
    response.setStatus(301);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    ReexecuteSavedRequest redirect = new ReexecuteSavedRequestImpl("/", "success", true, false);
    ReexecuteSavedRequestResult result = new ReexecuteSavedRequestResult(messageStore, ee, response, request, store, cipherProvider, configuration, objectMapper);
    result.execute(redirect);

    verify(response, request, ee, store, messageStore, session);
  }
}
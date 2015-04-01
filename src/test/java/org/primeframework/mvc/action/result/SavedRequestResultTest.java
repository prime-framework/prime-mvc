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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.SavedRequestResult.SavedRequestImpl;
import org.primeframework.mvc.action.result.annotation.SavedRequest;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
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
public class SavedRequestResultTest {
  @Test
  public void noSavedRequest() throws IOException, ServletException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(false)).andReturn(null);
    expect(request.getContextPath()).andReturn("");
    expect(request.getRequestURI()).andReturn("/");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(301);
    response.sendRedirect("/");
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    List<Message> messages = new ArrayList<>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    SavedRequest redirect = new SavedRequestImpl("/", "success", true, false);
    SavedRequestResult result = new SavedRequestResult(messageStore, ee, response, request, store);
    result.execute(redirect);

    verify(response, request, ee, store, messageStore);
  }

  @Test
  public void savedRequest() throws IOException, ServletException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    HttpSession session = createStrictMock(HttpSession.class);
    expect(session.getAttribute(SavedHttpRequest.SESSION_KEY)).andReturn(new SavedHttpRequest("/secure?test=value1&test2=value2", null));
    replay(session);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(false)).andReturn(session);
    expect(request.getContextPath()).andReturn("");
    expect(request.getRequestURI()).andReturn("/");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(301);
    response.sendRedirect("/secure?test=value1&test2=value2");
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    replay(store);

    List<Message> messages = new ArrayList<>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    SavedRequest redirect = new SavedRequestImpl("/", "success", true, false);
    SavedRequestResult result = new SavedRequestResult(messageStore, ee, response, request, store);
    result.execute(redirect);

    verify(response, request, ee, store, messageStore, session);
  }
}
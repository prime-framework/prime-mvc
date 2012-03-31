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
package org.primeframework.mvc.action.result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * This class tests the redirect result.
 *
 * @author Brian Pontarelli
 */
public class RedirectResultTest {
  @Test
  public void fullyQualified() throws IOException, ServletException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getContextPath()).andReturn("");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(301);
    response.sendRedirect("http://www.google.com");
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "/foo", "", null));
    replay(store);

    List<Message> messages = new ArrayList<Message>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "http://www.google.com", true, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    verify(response);
  }

  @Test
  public void relative() throws IOException, ServletException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getContextPath()).andReturn("");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(302);
    response.sendRedirect("/foo/bar.jsp");
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "foo", "", null));
    replay(store);

    List<Message> messages = new ArrayList<Message>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "/foo/bar.jsp", false, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    verify(response);
  }

  @Test
  public void relativeContext() throws IOException, ServletException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getContextPath()).andReturn("/context-path");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(302);
    response.sendRedirect("/context-path/foo/bar.jsp");
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "foo", "", null));
    replay(store);

    List<Message> messages = new ArrayList<Message>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "/foo/bar.jsp", false, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    verify(response);
  }

  @Test
  public void relativeContextNoSlash() throws IOException, ServletException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getContextPath()).andReturn("/context-path");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(302);
    response.sendRedirect("foo/bar.jsp");
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(null, "foo", "", null));
    replay(store);

    List<Message> messages = new ArrayList<Message>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "foo/bar.jsp", false, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    verify(response);
  }

  @Test
  public void expand() throws IOException, ServletException {
    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.expand("${foo}", action, false)).andReturn("result");
    replay(ee);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getContextPath()).andReturn("");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(302);
    response.sendRedirect("result");
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(action, "foo", "", null));
    replay(store);

    List<Message> messages = new ArrayList<Message>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "${foo}", false, false);
    RedirectResult result = new RedirectResult(messageStore, ee, response, request, store);
    result.execute(redirect);

    verify(response);
  }

  @Test
  public void encode() throws IOException, ServletException {
    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.expand("${foo}", action, true)).andReturn("result");
    replay(ee);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getContextPath()).andReturn("");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(302);
    response.sendRedirect("result");
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new DefaultActionInvocation(action, "foo", "", null));
    replay(store);

    List<Message> messages = new ArrayList<Message>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "${foo}", false, true);
    RedirectResult result = new RedirectResult(messageStore, ee, response, request, store);
    result.execute(redirect);

    verify(response);
  }

  public class RedirectImpl implements Redirect {
    private final String code;
    private final String uri;
    private final boolean perm;
    private final boolean encode;

    public RedirectImpl(String code, String uri, boolean perm, boolean encode) {
      this.code = code;
      this.uri = uri;
      this.perm = perm;
      this.encode = encode;
    }

    public String code() {
      return code;
    }

    public String uri() {
      return uri;
    }

    public boolean perm() {
      return perm;
    }

    public boolean encodeVariables() {
      return encode;
    }

    public Class<? extends Annotation> annotationType() {
      return Redirect.class;
    }
  }
}
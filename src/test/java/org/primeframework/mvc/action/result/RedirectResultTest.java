/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.easymock.internal.matchers.Any;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.FlashScope;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * This class tests the redirect result.
 *
 * @author Brian Pontarelli
 */
public class RedirectResultTest {
  @Test
  public void encode() throws IOException {
    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.expand("${foo}", action, true)).andReturn("result");
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(FlashScope.KEY)).andReturn(messages);
    request.removeAttribute(FlashScope.KEY);
    expect(request.getContextPath()).andReturn("/");
    expect(request.getRequestURI()).andReturn("/");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.sendRedirect("result");
    response.setStatus(302);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, null, "foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "${foo}", false, true);
    RedirectResult result = new RedirectResult(messageStore, ee, response, request, store);
    result.execute(redirect);

    verify(response);
  }

  @Test
  public void expand() throws IOException {
    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.expand("${foo}", action, false)).andReturn("result");
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(FlashScope.KEY)).andReturn(messages);
    request.removeAttribute(FlashScope.KEY);
    expect(request.getContextPath()).andReturn("");
    expect(request.getRequestURI()).andReturn("/");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.sendRedirect("result");
    response.setStatus(302);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, null, "foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "${foo}", false, false);
    RedirectResult result = new RedirectResult(messageStore, ee, response, request, store);
    result.execute(redirect);

    verify(response);
  }

  @Test
  public void fullyQualified() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(FlashScope.KEY)).andReturn(messages);
    request.removeAttribute(FlashScope.KEY);
    expect(request.getContextPath()).andReturn("");
    expect(request.getRequestURI()).andReturn("/");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.sendRedirect("http://www.google.com");
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

    Redirect redirect = new RedirectImpl("success", "http://www.google.com", true, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    verify(response);
  }

  @Test
  public void relative() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(FlashScope.KEY)).andReturn(messages);
    request.removeAttribute(FlashScope.KEY);
    expect(request.getContextPath()).andReturn("");
    expect(request.getRequestURI()).andReturn("/");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.sendRedirect("/foo/bar.jsp");
    response.setStatus(302);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "/foo/bar.jsp", false, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    verify(response);
  }

  @Test
  public void relativeContext() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(FlashScope.KEY)).andReturn(messages);
    request.removeAttribute(FlashScope.KEY);
    expect(request.getContextPath()).andReturn("/context-path");
    expect(request.getRequestURI()).andReturn("/");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.sendRedirect("/context-path/foo/bar.jsp");
    response.setStatus(302);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "/foo/bar.jsp", false, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    verify(response);
  }

  @Test
  public void relativeContextNoSlash() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(FlashScope.KEY)).andReturn(messages);
    request.removeAttribute(FlashScope.KEY);
    expect(request.getContextPath()).andReturn("/context-path");
    expect(request.getRequestURI()).andReturn("/");
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.sendRedirect("foo/bar.jsp");
    response.setStatus(302);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "foo/bar.jsp", false, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    verify(response);
  }

  public class RedirectImpl implements Redirect {
    private final String code;

    private final boolean encode;

    private final boolean perm;

    private final String uri;

    public RedirectImpl(String code, String uri, boolean perm, boolean encode) {
      this.code = code;
      this.uri = uri;
      this.perm = perm;
      this.encode = encode;
    }

    public Class<? extends Annotation> annotationType() {
      return Redirect.class;
    }

    public String code() {
      return code;
    }

    public boolean encodeVariables() {
      return encode;
    }

    public boolean perm() {
      return perm;
    }

    public String uri() {
      return uri;
    }
  }
}
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.http.DefaultHTTPRequest;
import org.primeframework.mvc.http.DefaultHTTPResponse;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.testng.Assert.assertEquals;

/**
 * This class tests the redirect result.
 *
 * @author Brian Pontarelli
 */
public class RedirectResultTest extends PrimeBaseTest {
  @Test
  public void encode() throws IOException {
    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.expand("${foo}", action, true)).andReturn("result");
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HTTPRequest request = new DefaultHTTPRequest().with(r -> r.contextPath = "/")
                                                  .with(r -> r.path = "/");

    HTTPResponse response = new DefaultHTTPResponse(new ByteArrayOutputStream());
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, null, "foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "${foo}", false, true);
    RedirectResult result = new RedirectResult(messageStore, ee, response, request, store);
    result.execute(redirect);

    assertEquals(response.getStatus(), 302);
    assertEquals(response.getRedirect(), "result");
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  @Test
  public void expand() throws IOException {
    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.expand("${foo}", action, false)).andReturn("result");
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HTTPRequest request = new DefaultHTTPRequest();
    HTTPResponse response = new DefaultHTTPResponse(new ByteArrayOutputStream());
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, null, "foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "${foo}", false, false);
    RedirectResult result = new RedirectResult(messageStore, ee, response, request, store);
    result.execute(redirect);

    assertEquals(response.getStatus(), 302);
    assertEquals(response.getRedirect(), "result");
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  @Test
  public void fullyQualified() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HTTPRequest request = new DefaultHTTPRequest();
    HTTPResponse response = new DefaultHTTPResponse(new ByteArrayOutputStream());
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "http://www.google.com", true, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    assertEquals(response.getStatus(), 301);
    assertEquals(response.getRedirect(), "http://www.google.com");
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  @Test
  public void relative() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HTTPRequest request = new DefaultHTTPRequest();
    HTTPResponse response = new DefaultHTTPResponse(new ByteArrayOutputStream());
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "/foo/bar.jsp", false, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    assertEquals(response.getStatus(), 302);
    assertEquals(response.getRedirect(), "/foo/bar.jsp");
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  @Test
  public void relativeContext() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HTTPRequest request = new DefaultHTTPRequest().with(r -> r.contextPath = "/context-path");
    HTTPResponse response = new DefaultHTTPResponse(new ByteArrayOutputStream());
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "/foo/bar.jsp", false, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    assertEquals(response.getStatus(), 302);
    assertEquals(response.getRedirect(), "/context-path/foo/bar.jsp");
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  @Test
  public void relativeContextNoSlash() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    List<Message> messages = new ArrayList<>();
    HTTPRequest request = new DefaultHTTPRequest().with(r -> r.contextPath = "/context-path");
    HTTPResponse response = new DefaultHTTPResponse(new ByteArrayOutputStream());
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "foo", "", null));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Redirect redirect = new RedirectImpl("success", "foo/bar.jsp", false, false);
    RedirectResult forwardResult = new RedirectResult(messageStore, ee, response, request, store);
    forwardResult.execute(redirect);

    assertEquals(response.getStatus(), 302);
    assertEquals(response.getRedirect(), "foo/bar.jsp");
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  public class RedirectImpl implements Redirect {
    private final String cacheControl;

    private final String code;

    private final boolean disableCacheControl;

    private final boolean encode;

    private final boolean perm;

    private final String uri;

    public RedirectImpl(String code, String uri, boolean perm, boolean encode) {
      this.cacheControl = "no-cache";
      this.code = code;
      this.disableCacheControl = false;
      this.encode = encode;
      this.perm = perm;
      this.uri = uri;
    }

    public Class<? extends Annotation> annotationType() {
      return Redirect.class;
    }

    @Override
    public String cacheControl() {
      return cacheControl;
    }

    public String code() {
      return code;
    }

    @Override
    public boolean disableCacheControl() {
      return disableCacheControl;
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
/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.Header;
import org.primeframework.mvc.http.DefaultHTTPResponse;
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
 * This class tests the status result.
 *
 * @author Brian Pontarelli
 */
public class StatusResultTest extends PrimeBaseTest {
  @Test
  public void expansion() throws IOException {
    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.expand("someFieldName", action, false)).andReturn("200");
    replay(ee);

    HTTPResponse response = new DefaultHTTPResponse(new ByteArrayOutputStream());
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, null, "/foo", "", null));
    replay(store);

    List<Message> messages = new ArrayList<Message>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Status status = new StatusImpl("success", 200, "someFieldName");
    StatusResult result = new StatusResult(ee, response, store);
    result.execute(status);

    assertEquals(response.getStatus(), 200);
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  @Test
  public void headers() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    HTTPResponse response = new DefaultHTTPResponse(new ByteArrayOutputStream());
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    List<Message> messages = new ArrayList<Message>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Status status = new StatusImpl("success", 200, "", new HeaderImpl("foo", "bar"), new HeaderImpl("baz", "fred"));
    StatusResult result = new StatusResult(ee, response, store);
    result.execute(status);

    assertEquals(response.getStatus(), 200);
    assertEquals(response.getHeader("foo"), "bar");
    assertEquals(response.getHeader("baz"), "fred");
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  @Test
  public void noHeaders() throws IOException {
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    HTTPResponse response = new DefaultHTTPResponse(new ByteArrayOutputStream());
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/foo", "", null));
    replay(store);

    List<Message> messages = new ArrayList<Message>();
    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(messages);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
    replay(messageStore);

    Status status = new StatusImpl("success", 200, "");
    StatusResult result = new StatusResult(ee, response, store);
    result.execute(status);

    assertEquals(response.getStatus(), 200);
    assertEquals(response.getHeader("Cache-Control"), "no-cache");
  }

  public class HeaderImpl implements Header {
    private final String name;

    private final String value;

    public HeaderImpl(String name, String value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return Header.class;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public String value() {
      return value;
    }
  }

  public class StatusImpl implements Status {
    private final String cacheControl;

    private final String code;

    private final boolean disableCacheControl;

    private final Header[] headers;

    private final int status;

    private final String statusStr;

    public StatusImpl(String code, int status, String statusStr, Header... headers) {
      this.cacheControl = "no-cache";
      this.code = code;
      this.disableCacheControl = false;
      this.headers = headers;
      this.status = status;
      this.statusStr = statusStr;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return Status.class;
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

    @Override
    public Header[] headers() {
      return headers;
    }

    @Override
    public int status() {
      return status;
    }

    @Override
    public String statusStr() {
      return statusStr;
    }
  }
}
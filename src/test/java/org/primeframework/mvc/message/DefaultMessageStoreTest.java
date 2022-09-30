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
package org.primeframework.mvc.message;

import java.util.ArrayList;
import java.util.List;

import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.message.scope.ApplicationScope;
import org.primeframework.mvc.message.scope.FlashScope;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.message.scope.RequestScope;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * This tests the default message store.
 *
 * @author Brian Pontarelli
 */
public class DefaultMessageStoreTest {
  // TODO : Re-Enable when we get a released version of EasyMock
  @Test(enabled = false)
  public void bulk() {
    List<Message> messages = new ArrayList<>();
    messages.add(new SimpleFieldMessage(MessageType.ERROR, "foo.bar", "code", "message"));
    messages.add(new SimpleFieldMessage(MessageType.ERROR, "foo.baz", "code", "message"));

    RequestScope scope = createStrictMock(RequestScope.class);
    scope.addAll(messages);
    replay(scope);

    HTTPRequest httpRequest = createStrictMock(HTTPRequest.class);
    DefaultMessageStore store = new DefaultMessageStore(null, null, scope, httpRequest);
    store.addAll(MessageScope.REQUEST, messages);

    verify(scope);
  }

  // TODO : Re-Enable when we get a released version of EasyMock
  @Test(enabled = false)
  public void get() {
    List<Message> requestMessages = new ArrayList<>();
    requestMessages.add(new SimpleMessage(MessageType.ERROR, "code1", "request1"));
    requestMessages.add(new SimpleMessage(MessageType.ERROR, "code2", "request2"));

    List<Message> flashMessages = new ArrayList<>();
    flashMessages.add(new SimpleMessage(MessageType.ERROR, "code3", "flash1"));
    flashMessages.add(new SimpleMessage(MessageType.ERROR, "code4", "flash2"));

    List<Message> applicationMessages = new ArrayList<>();
    applicationMessages.add(new SimpleMessage(MessageType.ERROR, "code7", "application1"));
    applicationMessages.add(new SimpleMessage(MessageType.ERROR, "code8", "application2"));

    RequestScope request = createStrictMock(RequestScope.class);
    expect(request.get()).andReturn(requestMessages);
    replay(request);

    FlashScope flash = createStrictMock(FlashScope.class);
    expect(flash.get()).andReturn(flashMessages);
    replay(flash);

    ApplicationScope application = createStrictMock(ApplicationScope.class);
    expect(application.get()).andReturn(applicationMessages);
    replay(application);

    HTTPRequest httpRequest = createStrictMock(HTTPRequest.class);
    DefaultMessageStore store = new DefaultMessageStore(application, flash, request, httpRequest);
    List<Message> messages = store.get();
    assertEquals(messages.size(), 6);

    int index = 0;
    assertEquals(messages.get(index++).toString(), "request1");
    assertEquals(messages.get(index++).toString(), "request2");
    assertEquals(messages.get(index++).toString(), "flash1");
    assertEquals(messages.get(index++).toString(), "flash2");
    assertEquals(messages.get(index++).toString(), "application1");
    assertEquals(messages.get(index).toString(), "application2");

    verify(request, flash, application);
  }

  // TODO : Re-Enable when we get a released version of EasyMock
  @Test(enabled = false)
  public void getScope() {
    List<Message> requestMessages = new ArrayList<>();
    requestMessages.add(new SimpleMessage(MessageType.ERROR, "code1", "request1"));
    requestMessages.add(new SimpleMessage(MessageType.ERROR, "code2", "request2"));

    RequestScope request = createStrictMock(RequestScope.class);
    expect(request.get()).andReturn(requestMessages);
    replay(request);

    HTTPRequest httpRequest = createStrictMock(HTTPRequest.class);
    DefaultMessageStore store = new DefaultMessageStore(null, null, request, httpRequest);
    List<Message> messages = store.get(MessageScope.REQUEST);
    assertEquals(messages.size(), 2);

    int index = 0;
    assertEquals(messages.get(index++).toString(), "request1");
    assertEquals(messages.get(index).toString(), "request2");

    verify(request);
  }

  // TODO : Re-Enable when we get a released version of EasyMock
  @Test(enabled = false)
  public void request() {
    Message message = new SimpleFieldMessage(MessageType.ERROR, "foo.bar", "code", "message");

    RequestScope scope = createStrictMock(RequestScope.class);
    scope.add(message);
    replay(scope);

    HTTPRequest httpRequest = createStrictMock(HTTPRequest.class);
    DefaultMessageStore store = new DefaultMessageStore(null, null, scope, httpRequest);
    store.add(message);

    verify(scope);
  }

  @Test
  public void scoped() {
    Message message = new SimpleFieldMessage(MessageType.ERROR, "foo.bar", "code", "message");

    FlashScope scope = createStrictMock(FlashScope.class);
    scope.add(message);
    replay(scope);

    HTTPRequest httpRequest = createStrictMock(HTTPRequest.class);
    DefaultMessageStore store = new DefaultMessageStore(null, scope, null, httpRequest);
    store.add(MessageScope.FLASH, message);

    verify(scope);
  }
}
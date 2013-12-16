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

import org.primeframework.mvc.message.scope.*;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertEquals;

/**
 * This tests the default message store.
 *
 * @author Brian Pontarelli
 */
public class DefaultMessageStoreTest {
  @Test
  public void request() {
    Message message = new SimpleFieldMessage(MessageType.ERROR, "foo.bar", "code", "message");

    RequestScope scope = createStrictMock(RequestScope.class);
    scope.add(message);
    replay(scope);

    DefaultMessageStore store = new DefaultMessageStore(null, null, null, scope);
    store.add(message);

    verify(scope);
  }

  @Test
  public void scoped() {
    Message message = new SimpleFieldMessage(MessageType.ERROR, "foo.bar", "code", "message");

    FlashScope scope = createStrictMock(FlashScope.class);
    scope.add(message);
    replay(scope);

    DefaultMessageStore store = new DefaultMessageStore(null, null, scope, null);
    store.add(MessageScope.FLASH, message);

    verify(scope);
  }

  @Test
  public void bulk() {
    List<Message> messages = new ArrayList<Message>();
    messages.add(new SimpleFieldMessage(MessageType.ERROR, "foo.bar", "code", "message"));
    messages.add(new SimpleFieldMessage(MessageType.ERROR, "foo.baz", "code", "message"));

    SessionScope scope = createStrictMock(SessionScope.class);
    scope.addAll(messages);
    replay(scope);

    DefaultMessageStore store = new DefaultMessageStore(null, scope, null, null);
    store.addAll(MessageScope.SESSION, messages);

    verify(scope);
  }

  @Test
  public void get() {
    List<Message> requestMessages = new ArrayList<Message>();
    requestMessages.add(new SimpleMessage(MessageType.ERROR, "code1", "request1"));
    requestMessages.add(new SimpleMessage(MessageType.ERROR, "code2", "request2"));

    List<Message> flashMessages = new ArrayList<Message>();
    flashMessages.add(new SimpleMessage(MessageType.ERROR, "code3", "flash1"));
    flashMessages.add(new SimpleMessage(MessageType.ERROR, "code4", "flash2"));

    List<Message> sessionMessages = new ArrayList<Message>();
    sessionMessages.add(new SimpleMessage(MessageType.ERROR, "code5", "session1"));
    sessionMessages.add(new SimpleMessage(MessageType.ERROR, "code6", "session2"));

    List<Message> applicationMessages = new ArrayList<Message>();
    applicationMessages.add(new SimpleMessage(MessageType.ERROR, "code7", "application1"));
    applicationMessages.add(new SimpleMessage(MessageType.ERROR, "code8", "application2"));

    RequestScope request = createStrictMock(RequestScope.class);
    expect(request.get()).andReturn(requestMessages);
    replay(request);

    FlashScope flash = createStrictMock(FlashScope.class);
    expect(flash.get()).andReturn(flashMessages);
    replay(flash);

    SessionScope session = createStrictMock(SessionScope.class);
    expect(session.get()).andReturn(sessionMessages);
    replay(session);

    ApplicationScope application = createStrictMock(ApplicationScope.class);
    expect(application.get()).andReturn(applicationMessages);
    replay(application);

    DefaultMessageStore store = new DefaultMessageStore(application, session, flash, request);
    List<Message> messages = store.get();
    assertEquals(messages.size(), 8);
    
    int index = 0;
    assertEquals(messages.get(index++).toString(), "request1");
    assertEquals(messages.get(index++).toString(), "request2");
    assertEquals(messages.get(index++).toString(), "flash1");
    assertEquals(messages.get(index++).toString(), "flash2");
    assertEquals(messages.get(index++).toString(), "session1");
    assertEquals(messages.get(index++).toString(), "session2");
    assertEquals(messages.get(index++).toString(), "application1");
    assertEquals(messages.get(index).toString(), "application2");

    verify(request, flash, session, application);
  }

  @Test
  public void getScope() {
    List<Message> requestMessages = new ArrayList<Message>();
    requestMessages.add(new SimpleMessage(MessageType.ERROR, "code1", "request1"));
    requestMessages.add(new SimpleMessage(MessageType.ERROR, "code2", "request2"));

    RequestScope request = createStrictMock(RequestScope.class);
    expect(request.get()).andReturn(requestMessages);
    replay(request);

    DefaultMessageStore store = new DefaultMessageStore(null, null, null, request);
    List<Message> messages = store.get(MessageScope.REQUEST);
    assertEquals(messages.size(), 2);

    int index = 0;
    assertEquals(messages.get(index++).toString(), "request1");
    assertEquals(messages.get(index).toString(), "request2");

    verify(request);
  }
}
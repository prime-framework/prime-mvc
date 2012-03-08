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

import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.message.scope.Scope;
import org.primeframework.mvc.message.scope.ScopeProvider;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This tests the default message store.
 *
 * @author Brian Pontarelli
 */
public class DefaultMessageStoreTest {
  @Test
  public void request() {
    Message message = new SimpleFieldMessage("foo.bar", "message");

    Scope scope = createStrictMock(Scope.class);
    scope.add(message);
    replay(scope);

    ScopeProvider sp = createStrictMock(ScopeProvider.class);
    expect(sp.lookup(MessageScope.REQUEST)).andReturn(scope);
    replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(sp);
    store.add(message);

    verify(scope, sp);
  }

  @Test
  public void scoped() {
    Message message = new SimpleFieldMessage("foo.bar", "message");

    Scope scope = createStrictMock(Scope.class);
    scope.add(message);
    replay(scope);

    ScopeProvider sp = createStrictMock(ScopeProvider.class);
    expect(sp.lookup(MessageScope.FLASH)).andReturn(scope);
    replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(sp);
    store.add(MessageScope.FLASH, message);

    verify(scope, sp);
  }

  @Test
  public void bulk() {
    List<Message> messages = new ArrayList<Message>();
    messages.add(new SimpleFieldMessage("foo.bar", "message"));
    messages.add(new SimpleFieldMessage("foo.baz", "message"));

    Scope scope = createStrictMock(Scope.class);
    scope.addAll(messages);
    replay(scope);

    ScopeProvider sp = createStrictMock(ScopeProvider.class);
    expect(sp.lookup(MessageScope.SESSION)).andReturn(scope);
    replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(sp);
    store.addAll(MessageScope.SESSION, messages);

    verify(scope, sp);
  }

  @Test
  public void get() {
    List<Message> requestMessages = new ArrayList<Message>();
    requestMessages.add(new SimpleMessage("request1"));
    requestMessages.add(new SimpleMessage("request2"));

    List<Message> flashMessages = new ArrayList<Message>();
    flashMessages.add(new SimpleMessage("flash1"));
    flashMessages.add(new SimpleMessage("flash2"));

    List<Message> sessionMessages = new ArrayList<Message>();
    sessionMessages.add(new SimpleMessage("session1"));
    sessionMessages.add(new SimpleMessage("session2"));

    List<Message> applicationMessages = new ArrayList<Message>();
    applicationMessages.add(new SimpleMessage("application1"));
    applicationMessages.add(new SimpleMessage("application2"));

    Scope request = createStrictMock(Scope.class);
    expect(request.get()).andReturn(requestMessages);
    replay(request);

    Scope flash = createStrictMock(Scope.class);
    expect(flash.get()).andReturn(flashMessages);
    replay(flash);

    Scope session = createStrictMock(Scope.class);
    expect(session.get()).andReturn(sessionMessages);
    replay(session);

    Scope application = createStrictMock(Scope.class);
    expect(application.get()).andReturn(applicationMessages);
    replay(application);

    ScopeProvider sp = createStrictMock(ScopeProvider.class);
    expect(sp.getAllScopes()).andReturn(asList(request, flash, session, application));
    replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(sp);
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

    verify(request, flash, session, application, sp);
  }

  @Test
  public void getScope() {
    List<Message> requestMessages = new ArrayList<Message>();
    requestMessages.add(new SimpleMessage("request1"));
    requestMessages.add(new SimpleMessage("request2"));

    Scope request = createStrictMock(Scope.class);
    expect(request.get()).andReturn(requestMessages);
    replay(request);

    ScopeProvider sp = createStrictMock(ScopeProvider.class);
    expect(sp.lookup(MessageScope.REQUEST)).andReturn(request);
    replay(sp);

    DefaultMessageStore store = new DefaultMessageStore(sp);
    List<Message> messages = store.get(MessageScope.REQUEST);
    assertEquals(messages.size(), 2);

    int index = 0;
    assertEquals(messages.get(index++).toString(), "request1");
    assertEquals(messages.get(index).toString(), "request2");

    verify(request, sp);
  }
}
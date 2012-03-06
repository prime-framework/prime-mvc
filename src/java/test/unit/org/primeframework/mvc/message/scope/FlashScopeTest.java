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
package org.primeframework.mvc.message.scope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.SimpleMessage;
import org.testng.annotations.Test;

import static java.util.Arrays.*;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This tests the flash scope.
 *
 * @author Brian Pontarelli
 */
public class FlashScopeTest {
  @Test
  public void get() {
    HttpSession session = createStrictMock(HttpSession.class);
    expect(session.getAttribute(FlashScope.KEY)).andReturn(asList(new SimpleMessage("Session")));
    replay(session);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(FlashScope.KEY)).andReturn(asList(new SimpleMessage("Request")));
    expect(request.getSession(false)).andReturn(session);
    replay(request);

    FlashScope scope = new FlashScope(request);
    List<Message> messages = scope.get();
    assertEquals(messages.size(), 2);
    assertEquals(messages.get(0), "Request");
    assertEquals(messages.get(1), "Session");

    verify(session, request);
  }

  @Test
  public void add() {
    List<Message> messages = new ArrayList<Message>();

    HttpSession session = createStrictMock(HttpSession.class);
    expect(session.getAttribute(FlashScope.KEY)).andReturn(messages);
    replay(session);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(true)).andReturn(session);
    replay(request);

    FlashScope scope = new FlashScope(request);
    scope.add(new SimpleMessage("Foo"));
    assertEquals(messages.size(), 1);
    assertEquals(messages.get(0), "Foo");

    verify(session, request);
  }

  @Test
  public void addAll() {
    List<Message> messages = new ArrayList<Message>();

    HttpSession session = createStrictMock(HttpSession.class);
    expect(session.getAttribute(FlashScope.KEY)).andReturn(messages);
    replay(session);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(true)).andReturn(session);
    replay(request);

    FlashScope scope = new FlashScope(request);
    scope.addAll(Arrays.<Message>asList(new SimpleMessage("Foo"), new SimpleMessage("Bar")));
    assertEquals(messages.size(), 2);
    assertEquals(messages.get(0), "Foo");
    assertEquals(messages.get(1), "Bar");

    verify(session, request);
  }

  @Test
  public void transferFlash() {
    List<Message> messages = new ArrayList<Message>();

    HttpSession session = createStrictMock(HttpSession.class);
    expect(session.getAttribute(FlashScope.KEY)).andReturn(messages);
    session.removeAttribute(FlashScope.KEY);
    replay(session);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(true)).andReturn(session);
    request.setAttribute(FlashScope.KEY, messages);
    replay(request);

    FlashScope scope = new FlashScope(request);
    scope.transferFlash();

    verify(session, request);
  }
}
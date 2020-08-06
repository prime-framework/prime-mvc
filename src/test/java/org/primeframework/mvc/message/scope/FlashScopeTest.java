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
package org.primeframework.mvc.message.scope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * This tests the flash scope.
 *
 * @author Brian Pontarelli
 */
public class FlashScopeTest {
  @Test
  public void add() {
    List<Message> messages = new ArrayList<>();

    HttpSession session = createStrictMock(HttpSession.class);
    expect(session.getAttribute(FlashScope.KEY)).andReturn(messages);
    replay(session);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(true)).andReturn(session);
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    replay(response);

    MVCConfiguration configuration = new MockConfiguration();
    ObjectMapper objectMapper = new ObjectMapper();
    FlashScope scope = new FlashScope(configuration, objectMapper, request, response);

    scope.add(new SimpleMessage(MessageType.ERROR, "code", "Foo"));
    assertEquals(messages.size(), 1);
    assertEquals(messages.get(0).toString(), "Foo");

    verify(session, request);
  }

  @Test
  public void addAll() {
    List<Message> messages = new ArrayList<>();

    HttpSession session = createStrictMock(HttpSession.class);
    expect(session.getAttribute(FlashScope.KEY)).andReturn(messages);
    replay(session);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(true)).andReturn(session);
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    replay(response);

    MVCConfiguration configuration = new MockConfiguration();
    ObjectMapper objectMapper = new ObjectMapper();
    FlashScope scope = new FlashScope(configuration, objectMapper, request, response);

    scope.addAll(Arrays.asList(new SimpleMessage(MessageType.ERROR, "code1", "Foo"), new SimpleMessage(MessageType.ERROR, "code2", "Bar")));
    assertEquals(messages.size(), 2);
    assertEquals(messages.get(0).toString(), "Foo");
    assertEquals(messages.get(1).toString(), "Bar");

    verify(session, request);
  }

  @Test
  public void get() {
    HttpSession session = createStrictMock(HttpSession.class);
    expect(session.getAttribute(FlashScope.KEY)).andReturn(asList(new SimpleMessage(MessageType.ERROR, "code1", "Session")));
    replay(session);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(FlashScope.KEY)).andReturn(asList(new SimpleMessage(MessageType.ERROR, "code2", "Request")));
    expect(request.getSession(false)).andReturn(session);
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    replay(response);

    MVCConfiguration configuration = new MockConfiguration();
    ObjectMapper objectMapper = new ObjectMapper();
    FlashScope scope = new FlashScope(configuration, objectMapper, request, response);

    List<Message> messages = scope.get();
    assertEquals(messages.size(), 2);
    assertEquals(messages.get(0).toString(), "Request");
    assertEquals(messages.get(1).toString(), "Session");

    verify(session, request);
  }

  @Test
  public void transferFlash() {
    List<Message> messages = new ArrayList<>();

    HttpSession session = createStrictMock(HttpSession.class);
    expect(session.getAttribute(FlashScope.KEY)).andReturn(messages);
    session.removeAttribute(FlashScope.KEY);
    replay(session);

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(false)).andReturn(session);
    request.setAttribute(FlashScope.KEY, messages);
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    replay(response);

    MVCConfiguration configuration = new MockConfiguration();
    ObjectMapper objectMapper = new ObjectMapper();
    FlashScope scope = new FlashScope(configuration, objectMapper, request, response);

    scope.transferFlash();

    verify(session, request);
  }

  @Test
  public void transferFlashNoSession() {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(false)).andReturn(null);
    replay(request);

    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    replay(response);

    MVCConfiguration configuration = new MockConfiguration();
    ObjectMapper objectMapper = new ObjectMapper();
    FlashScope scope = new FlashScope(configuration, objectMapper, request, response);

    scope.transferFlash();

    verify(request);
  }
}
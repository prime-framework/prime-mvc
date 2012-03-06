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
 * This tests the request scope.
 *
 * @author Brian Pontarelli
 */
public class RequestScopeTest {
  @Test
  public void get() {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(RequestScope.KEY)).andReturn(asList(new SimpleMessage("Test message")));
    replay(request);

    RequestScope scope = new RequestScope(request);
    List<Message> messages = scope.get();
    assertEquals(messages.size(), 1);
    assertEquals(messages.get(0), "Test message");

    verify(request);
  }

  @Test
  public void add() {
    List<Message> messages = new ArrayList<Message>();

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(SessionScope.KEY)).andReturn(messages);
    replay(request);

    RequestScope scope = new RequestScope(request);
    scope.add(new SimpleMessage("Foo"));
    assertEquals(messages.size(), 1);
    assertEquals(messages.get(0), "Foo");

    verify(request);
  }

  @Test
  public void addAll() {
    List<Message> messages = new ArrayList<Message>();

    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getAttribute(RequestScope.KEY)).andReturn(messages);
    replay(request);

    RequestScope scope = new RequestScope(request);
    scope.addAll(Arrays.<Message>asList(new SimpleMessage("Foo"), new SimpleMessage("Bar")));
    assertEquals(messages.size(), 2);
    assertEquals(messages.get(0), "Foo");
    assertEquals(messages.get(1), "Bar");

    verify(request);
  }
}
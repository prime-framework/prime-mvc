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

import java.util.ArrayList;
import java.util.List;

import org.primeframework.mvc.http.HTTPContext;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * This tests the application scope.
 *
 * @author Brian Pontarelli
 */
public class ApplicationScopeTest {
  // TODO : Re-Enable when we get a released version of EasyMock
  @Test(enabled = false)
  public void add() {
    List<Message> messages = new ArrayList<>();

    HTTPContext context = createStrictMock(HTTPContext.class);
    expect(context.getAttribute(ApplicationScope.KEY)).andReturn(messages);
    replay(context);

    ApplicationScope scope = new ApplicationScope(context);
    scope.add(new SimpleMessage(MessageType.ERROR, "code", "Foo"));
    assertEquals(messages.size(), 1);
    assertEquals(messages.get(0).toString(), "Foo");

    verify(context);
  }

  // TODO : Re-Enable when we get a released version of EasyMock
  @Test(enabled = false)
  public void addAll() {
    List<Message> messages = new ArrayList<>();

    HTTPContext context = createStrictMock(HTTPContext.class);
    expect(context.getAttribute(ApplicationScope.KEY)).andReturn(messages);
    replay(context);

    ApplicationScope scope = new ApplicationScope(context);
    scope.addAll(List.of(new SimpleMessage(MessageType.ERROR, "code1", "Foo"), new SimpleMessage(MessageType.ERROR, "code2", "Bar")));
    assertEquals(messages.size(), 2);
    assertEquals(messages.get(0).toString(), "Foo");
    assertEquals(messages.get(1).toString(), "Bar");

    verify(context);
  }

  // TODO : Re-Enable when we get a released version of EasyMock
  @Test(enabled = false)
  public void get() {
    HTTPContext context = createStrictMock(HTTPContext.class);
    expect(context.getAttribute(ApplicationScope.KEY)).andReturn(List.of(new SimpleMessage(MessageType.ERROR, "code", "Test message")));
    replay(context);

    ApplicationScope scope = new ApplicationScope(context);
    List<Message> messages = scope.get();
    assertEquals(messages.size(), 1);
    assertEquals(messages.get(0).toString(), "Test message");

    verify(context);
  }
}
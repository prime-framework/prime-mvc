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
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import static java.util.Arrays.*;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * <p> This tests the request scope. </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class RequestScopeTest {
  @Test
  public void testAction() {
    action(RequestScope.ACTION_ERROR_KEY, MessageType.ERROR);
    action(RequestScope.ACTION_MESSAGE_KEY, MessageType.PLAIN);
  }

  protected void action(String key, MessageType type) {
    {
      HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
      EasyMock.expect(request.getAttribute(key)).andReturn(asList("Test message"));
      EasyMock.replay(request);

      RequestScope scope = new RequestScope(request);
      List<String> messages = scope.getActionMessages(type);
      assertEquals(1, messages.size());
      assertEquals("Test message", messages.get(0));

      EasyMock.verify(request);
    }

    {
      HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
      EasyMock.expect(request.getAttribute(key)).andReturn(null);
      EasyMock.replay(request);

      RequestScope scope = new RequestScope(request);
      List<String> messages = scope.getActionMessages(type);
      assertEquals(0, messages.size());

      EasyMock.verify(request);
    }

    {
      List<String> messages = new ArrayList<String>();
      HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
      EasyMock.expect(request.getAttribute(key)).andReturn(messages);
      EasyMock.replay(request);

      RequestScope scope = new RequestScope(request);
      scope.addActionMessage(type, "Test message");
      assertEquals(1, messages.size());
      assertEquals("Test message", messages.get(0));

      EasyMock.verify(request);
    }

    {
      Capture<List<String>> list = new Capture<List<String>>();
      HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
      EasyMock.expect(request.getAttribute(key)).andReturn(null);
      request.setAttribute(eq(key), capture(list));
      EasyMock.replay(request);

      RequestScope scope = new RequestScope(request);
      scope.addActionMessage(type, "Test message");
      List<String> messages = list.getValue();
      assertEquals(1, messages.size());
      assertEquals("Test message", messages.get(0));

      EasyMock.verify(request);
    }
  }

  @Test
  public void testField() {
    field(RequestScope.FIELD_ERROR_KEY, MessageType.ERROR);
    field(RequestScope.FIELD_MESSAGE_KEY, MessageType.PLAIN);
  }

  protected void field(String key, MessageType type) {
    {
      FieldMessages fm = new FieldMessages();
      fm.addMessage("user.name", "Test message");

      HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
      EasyMock.expect(request.getAttribute(key)).andReturn(fm);
      EasyMock.replay(request);

      RequestScope scope = new RequestScope(request);
      Map<String, List<String>> messages = scope.getFieldMessages(type);
      assertEquals(1, messages.size());
      assertEquals(1, messages.get("user.name").size());
      assertEquals("Test message", messages.get("user.name").get(0));

      EasyMock.verify(request);
    }

    {
      HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
      EasyMock.expect(request.getAttribute(key)).andReturn(null);
      EasyMock.replay(request);

      RequestScope scope = new RequestScope(request);
      Map<String, List<String>> messages = scope.getFieldMessages(type);
      assertEquals(0, messages.size());

      EasyMock.verify(request);
    }

    {
      FieldMessages messages = new FieldMessages();
      HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
      EasyMock.expect(request.getAttribute(key)).andReturn(messages);
      EasyMock.replay(request);

      RequestScope scope = new RequestScope(request);
      scope.addFieldMessage(type, "user.name", "Test message");
      assertEquals(1, messages.size());
      assertEquals(1, messages.get("user.name").size());
      assertEquals("Test message", messages.get("user.name").get(0));

      EasyMock.verify(request);
    }

    {
      Capture<Map<String, List<String>>> map = new Capture<Map<String, List<String>>>();
      HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
      EasyMock.expect(request.getAttribute(key)).andReturn(null);
      request.setAttribute(eq(key), capture(map));
      EasyMock.replay(request);

      RequestScope scope = new RequestScope(request);
      scope.addFieldMessage(type, "user.name", "Test message");
      Map<String, List<String>> messages = map.getValue();
      assertEquals(1, messages.size());
      assertEquals(1, messages.get("user.name").size());
      assertEquals("Test message", messages.get("user.name").get(0));

      EasyMock.verify(request);
    }
  }
}
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

import javax.servlet.ServletContext;
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
 * <p> This tests the context scope. </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ApplicationScopeTest {
  @Test
  public void testAction() {
    action(ApplicationScope.ACTION_ERROR_KEY, MessageType.ERROR);
    action(ApplicationScope.ACTION_MESSAGE_KEY, MessageType.PLAIN);
  }

  protected void action(String key, MessageType type) {
    {
      ServletContext context = EasyMock.createStrictMock(ServletContext.class);
      EasyMock.expect(context.getAttribute(key)).andReturn(asList("Test message"));
      EasyMock.replay(context);

      ApplicationScope scope = new ApplicationScope(context);
      List<String> messages = scope.getActionMessages(type);
      assertEquals(1, messages.size());
      assertEquals("Test message", messages.get(0));

      EasyMock.verify(context);
    }

    {
      ServletContext context = EasyMock.createStrictMock(ServletContext.class);
      EasyMock.expect(context.getAttribute(key)).andReturn(null);
      EasyMock.replay(context);

      ApplicationScope scope = new ApplicationScope(context);
      List<String> messages = scope.getActionMessages(type);
      assertEquals(0, messages.size());

      EasyMock.verify(context);
    }

    {
      List<String> messages = new ArrayList<String>();
      ServletContext context = EasyMock.createStrictMock(ServletContext.class);
      EasyMock.expect(context.getAttribute(key)).andReturn(messages);
      EasyMock.replay(context);

      ApplicationScope scope = new ApplicationScope(context);
      scope.addActionMessage(type, "Test message");
      assertEquals(1, messages.size());
      assertEquals("Test message", messages.get(0));

      EasyMock.verify(context);
    }

    {
      Capture<List<String>> list = new Capture<List<String>>();
      ServletContext context = EasyMock.createStrictMock(ServletContext.class);
      EasyMock.expect(context.getAttribute(key)).andReturn(null);
      context.setAttribute(eq(key), EasyMock.capture(list));
      EasyMock.replay(context);

      ApplicationScope scope = new ApplicationScope(context);
      scope.addActionMessage(type, "Test message");
      List<String> messages = list.getValue();
      assertEquals(1, messages.size());
      assertEquals("Test message", messages.get(0));

      EasyMock.verify(context);
    }
  }

  @Test
  public void testField() {
    field(ApplicationScope.FIELD_ERROR_KEY, MessageType.ERROR);
    field(ApplicationScope.FIELD_MESSAGE_KEY, MessageType.PLAIN);
  }

  protected void field(String key, MessageType type) {
    {
      FieldMessages fm = new FieldMessages();
      fm.addMessage("user.name", "Test message");

      ServletContext context = EasyMock.createStrictMock(ServletContext.class);
      EasyMock.expect(context.getAttribute(key)).andReturn(fm);
      EasyMock.replay(context);

      ApplicationScope scope = new ApplicationScope(context);
      Map<String, List<String>> messages = scope.getFieldMessages(type);
      assertEquals(1, messages.size());
      assertEquals(1, messages.get("user.name").size());
      assertEquals("Test message", messages.get("user.name").get(0));

      EasyMock.verify(context);
    }

    {
      ServletContext context = EasyMock.createStrictMock(ServletContext.class);
      EasyMock.expect(context.getAttribute(key)).andReturn(null);
      EasyMock.replay(context);

      ApplicationScope scope = new ApplicationScope(context);
      Map<String, List<String>> messages = scope.getFieldMessages(type);
      assertEquals(0, messages.size());

      EasyMock.verify(context);
    }

    {
      FieldMessages messages = new FieldMessages();
      ServletContext context = EasyMock.createStrictMock(ServletContext.class);
      EasyMock.expect(context.getAttribute(key)).andReturn(messages);
      EasyMock.replay(context);

      ApplicationScope scope = new ApplicationScope(context);
      scope.addFieldMessage(type, "user.name", "Test message");
      assertEquals(1, messages.size());
      assertEquals(1, messages.get("user.name").size());
      assertEquals("Test message", messages.get("user.name").get(0));

      EasyMock.verify(context);
    }

    {
      Capture<Map<String, List<String>>> map = new Capture<Map<String, List<String>>>();
      ServletContext context = EasyMock.createStrictMock(ServletContext.class);
      EasyMock.expect(context.getAttribute(key)).andReturn(null);
      context.setAttribute(eq(key), capture(map));
      EasyMock.replay(context);

      ApplicationScope scope = new ApplicationScope(context);
      scope.addFieldMessage(type, "user.name", "Test message");
      Map<String, List<String>> messages = map.getValue();
      assertEquals(1, messages.size());
      assertEquals(1, messages.get("user.name").size());
      assertEquals("Test message", messages.get("user.name").get(0));

      EasyMock.verify(context);
    }
  }
}
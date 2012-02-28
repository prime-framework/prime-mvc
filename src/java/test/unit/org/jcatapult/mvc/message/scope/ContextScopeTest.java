/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.mvc.message.scope;

import java.util.ArrayList;
import static java.util.Arrays.*;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.jcatapult.test.Capture;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * <p>
 * This tests the context scope.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ContextScopeTest {
    @Test
    public void testAction() {
        action(ContextScope.ACTION_ERROR_KEY, MessageType.ERROR);
        action(ContextScope.ACTION_MESSAGE_KEY, MessageType.PLAIN);
    }

    protected void action(String key, MessageType type) {
        {
            ServletContext context = EasyMock.createStrictMock(ServletContext.class);
            EasyMock.expect(context.getAttribute(key)).andReturn(asList("Test message"));
            EasyMock.replay(context);

            ContextScope scope = new ContextScope(context);
            List<String> messages = scope.getActionMessages(type);
            assertEquals(1, messages.size());
            assertEquals("Test message", messages.get(0));

            EasyMock.verify(context);
        }

        {
            ServletContext context = EasyMock.createStrictMock(ServletContext.class);
            EasyMock.expect(context.getAttribute(key)).andReturn(null);
            EasyMock.replay(context);

            ContextScope scope = new ContextScope(context);
            List<String> messages = scope.getActionMessages(type);
            assertEquals(0, messages.size());

            EasyMock.verify(context);
        }

        {
            List<String> messages = new ArrayList<String>();
            ServletContext context = EasyMock.createStrictMock(ServletContext.class);
            EasyMock.expect(context.getAttribute(key)).andReturn(messages);
            EasyMock.replay(context);

            ContextScope scope = new ContextScope(context);
            scope.addActionMessage(type, "Test message");
            assertEquals(1, messages.size());
            assertEquals("Test message", messages.get(0));

            EasyMock.verify(context);
        }

        {
            Capture list = new Capture();
            ServletContext context = EasyMock.createStrictMock(ServletContext.class);
            EasyMock.expect(context.getAttribute(key)).andReturn(null);
            context.setAttribute(eq(key), list.capture());
            EasyMock.replay(context);

            ContextScope scope = new ContextScope(context);
            scope.addActionMessage(type, "Test message");
            List<String> messages = (List<String>) list.object;
            assertEquals(1, messages.size());
            assertEquals("Test message", messages.get(0));

            EasyMock.verify(context);
        }
    }

    @Test
    public void testField() {
        field(ContextScope.FIELD_ERROR_KEY, MessageType.ERROR);
        field(ContextScope.FIELD_MESSAGE_KEY, MessageType.PLAIN);
    }

    protected void field(String key, MessageType type) {
        {
            FieldMessages fm = new FieldMessages();
            fm.addMessage("user.name", "Test message");

            ServletContext context = EasyMock.createStrictMock(ServletContext.class);
            EasyMock.expect(context.getAttribute(key)).andReturn(fm);
            EasyMock.replay(context);

            ContextScope scope = new ContextScope(context);
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

            ContextScope scope = new ContextScope(context);
            Map<String, List<String>> messages = scope.getFieldMessages(type);
            assertEquals(0, messages.size());

            EasyMock.verify(context);
        }

        {
            FieldMessages messages = new FieldMessages();
            ServletContext context = EasyMock.createStrictMock(ServletContext.class);
            EasyMock.expect(context.getAttribute(key)).andReturn(messages);
            EasyMock.replay(context);

            ContextScope scope = new ContextScope(context);
            scope.addFieldMessage(type, "user.name", "Test message");
            assertEquals(1, messages.size());
            assertEquals(1, messages.get("user.name").size());
            assertEquals("Test message", messages.get("user.name").get(0));

            EasyMock.verify(context);
        }

        {
            Capture map = new Capture();
            ServletContext context = EasyMock.createStrictMock(ServletContext.class);
            EasyMock.expect(context.getAttribute(key)).andReturn(null);
            context.setAttribute(eq(key), map.capture());
            EasyMock.replay(context);

            ContextScope scope = new ContextScope(context);
            scope.addFieldMessage(type, "user.name", "Test message");
            Map<String, List<String>> messages = (Map<String, List<String>>) map.object;
            assertEquals(1, messages.size());
            assertEquals(1, messages.get("user.name").size());
            assertEquals("Test message", messages.get("user.name").get(0));

            EasyMock.verify(context);
        }
    }
}
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
package org.primeframework.mvc.message.scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.primeframework.test.Capture;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * <p>
 * This tests the flash scope.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class FlashScopeTest {
    @Test
    public void testAction() {
        action(FlashScope.FLASH_ACTION_ERRORS_KEY, MessageType.ERROR);
        action(FlashScope.FLASH_ACTION_MESSAGES_KEY, MessageType.PLAIN);
    }

    protected void action(String key, MessageType type) {
        {
            List<String> sessionMessages = new ArrayList<String>();
            sessionMessages.add("Test session message");
            List<String> requestMessages = new ArrayList<String>();
            requestMessages.add("Test request message");

            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(sessionMessages);
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getAttribute(key)).andReturn(requestMessages);
            EasyMock.expect(request.getSession(false)).andReturn(session);
            EasyMock.replay(request);

            FlashScope scope = new FlashScope(request);
            List<String> messages = scope.getActionMessages(type);
            assertEquals(2, messages.size());
            assertEquals("Test request message", messages.get(0));
            assertEquals("Test session message", messages.get(1));

            EasyMock.verify(request, session);
        }

        {
            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(null);
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getAttribute(key)).andReturn(null);
            EasyMock.expect(request.getSession(false)).andReturn(session);
            EasyMock.replay(request);

            FlashScope scope = new FlashScope(request);
            List<String> messages = scope.getActionMessages(type);
            assertEquals(0, messages.size());

            EasyMock.verify(request, session);
        }

        {
            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getAttribute(key)).andReturn(null);
            EasyMock.expect(request.getSession(false)).andReturn(null);
            EasyMock.replay(request);

            FlashScope scope = new FlashScope(request);
            List<String> messages = scope.getActionMessages(type);
            assertEquals(0, messages.size());

            EasyMock.verify(request);
        }

        {
            List<String> messages = new ArrayList<String>();

            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(messages);
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(true)).andReturn(session);
            EasyMock.replay(request);

            FlashScope scope = new FlashScope(request);
            scope.addActionMessage(type, "Test message");
            assertEquals(1, messages.size());
            assertEquals("Test message", messages.get(0));

            EasyMock.verify(request, session);
        }

        {
            Capture list = new Capture();
            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(null);
            session.setAttribute(eq(key), list.capture());
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(true)).andReturn(session);
            EasyMock.replay(request);

            FlashScope scope = new FlashScope(request);
            scope.addActionMessage(type, "Test message");
            List<String> messages = (List<String>) list.object;
            assertEquals(1, messages.size());
            assertEquals("Test message", messages.get(0));

            EasyMock.verify(request, session);
        }
    }

    @Test
    public void testField() {
        field(FlashScope.FLASH_FIELD_ERRORS_KEY, MessageType.ERROR);
        field(FlashScope.FLASH_FIELD_MESSAGES_KEY, MessageType.PLAIN);
    }

    protected void field(String key, MessageType type) {
        {
            FieldMessages sessionMessages = new FieldMessages();
            sessionMessages.addMessage("user.name", "Test session message");
            FieldMessages requestMessages = new FieldMessages();
            requestMessages.addMessage("user.name", "Test request message");

            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(sessionMessages);
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getAttribute(key)).andReturn(requestMessages);
            EasyMock.expect(request.getSession(false)).andReturn(session);
            EasyMock.replay(request);

            FlashScope scope = new FlashScope(request);
            Map<String, List<String>> messages = scope.getFieldMessages(type);
            assertEquals(1, messages.size());
            assertEquals(2, messages.get("user.name").size());
            assertEquals("Test request message", messages.get("user.name").get(0));
            assertEquals("Test session message", messages.get("user.name").get(1));

            EasyMock.verify(request, session);
        }

        {
            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(null);
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getAttribute(key)).andReturn(null);
            EasyMock.expect(request.getSession(false)).andReturn(session);
            EasyMock.replay(request);

            FlashScope scope = new FlashScope(request);
            Map<String, List<String>> messages = scope.getFieldMessages(type);
            assertEquals(0, messages.size());

            EasyMock.verify(request, session);
        }

        {
            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getAttribute(key)).andReturn(null);
            EasyMock.expect(request.getSession(false)).andReturn(null);
            EasyMock.replay(request);

            FlashScope scope = new FlashScope(request);
            Map<String, List<String>> messages = scope.getFieldMessages(type);
            assertEquals(0, messages.size());

            EasyMock.verify(request);
        }

        {
            FieldMessages messages = new FieldMessages();

            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(messages);
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(true)).andReturn(session);
            EasyMock.replay(request);

            FlashScope scope = new FlashScope(request);
            scope.addFieldMessage(type, "user.name", "Test message");
            assertEquals(1, messages.size());
            assertEquals(1, messages.get("user.name").size());
            assertEquals("Test message", messages.get("user.name").get(0));

            EasyMock.verify(request, session);
        }

        {
            Capture capture = new Capture();
            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(null);
            session.setAttribute(eq(key), capture.capture());
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(true)).andReturn(session);
            EasyMock.replay(request);

            FlashScope scope = new FlashScope(request);
            scope.addFieldMessage(type, "user.name", "Test message");
            FieldMessages messages = (FieldMessages) capture.object;
            assertEquals(1, messages.size());
            assertEquals(1, messages.get("user.name").size());
            assertEquals("Test message", messages.get("user.name").get(0));

            EasyMock.verify(request, session);
        }
    }
}
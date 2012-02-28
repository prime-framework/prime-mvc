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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.jcatapult.test.Capture;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * <p>
 * This tests the request scope.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class SessionScopeTest {
    @Test
    public void testAction() {
        action(SessionScope.ACTION_ERROR_KEY, MessageType.ERROR);
        action(SessionScope.ACTION_MESSAGE_KEY, MessageType.PLAIN);
    }

    protected void action(String key, MessageType type) {
        {
            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(asList("Test message"));
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(false)).andReturn(session);
            EasyMock.replay(request);

            SessionScope scope = new SessionScope(request);
            List<String> messages = scope.getActionMessages(type);
            assertEquals(1, messages.size());
            assertEquals("Test message", messages.get(0));

            EasyMock.verify(session, request);
        }

        {
            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(null);
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(false)).andReturn(session);
            EasyMock.replay(request);

            SessionScope scope = new SessionScope(request);
            List<String> messages = scope.getActionMessages(type);
            assertEquals(0, messages.size());

            EasyMock.verify(session, request);
        }

        {
            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(false)).andReturn(null);
            EasyMock.replay(request);

            SessionScope scope = new SessionScope(request);
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

            SessionScope scope = new SessionScope(request);
            scope.addActionMessage(type, "Test message");
            assertEquals(1, messages.size());
            assertEquals("Test message", messages.get(0));

            EasyMock.verify(session, request);
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

            SessionScope scope = new SessionScope(request);
            scope.addActionMessage(type, "Test message");
            List<String> messages = (List<String>) list.object;
            assertEquals(1, messages.size());
            assertEquals("Test message", messages.get(0));

            EasyMock.verify(session, request);
        }
    }

    @Test
    public void testField() {
        field(SessionScope.FIELD_ERROR_KEY, MessageType.ERROR);
        field(SessionScope.FIELD_MESSAGE_KEY, MessageType.PLAIN);
    }

    protected void field(String key, MessageType type) {
        {
            FieldMessages fm = new FieldMessages();
            fm.addMessage("user.name", "Test message");

            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(fm);
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(false)).andReturn(session);
            EasyMock.replay(request);

            SessionScope scope = new SessionScope(request);
            Map<String, List<String>> messages = scope.getFieldMessages(type);
            assertEquals(1, messages.size());
            assertEquals(1, messages.get("user.name").size());
            assertEquals("Test message", messages.get("user.name").get(0));

            EasyMock.verify(session, request);
        }

        {
            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(null);
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(false)).andReturn(session);
            EasyMock.replay(request);

            SessionScope scope = new SessionScope(request);
            Map<String, List<String>> messages = scope.getFieldMessages(type);
            assertEquals(0, messages.size());

            EasyMock.verify(session, request);
        }

        {
            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(false)).andReturn(null);
            EasyMock.replay(request);

            SessionScope scope = new SessionScope(request);
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

            SessionScope scope = new SessionScope(request);
            scope.addFieldMessage(type, "user.name", "Test message");
            assertEquals(1, messages.size());
            assertEquals(1, messages.get("user.name").size());
            assertEquals("Test message", messages.get("user.name").get(0));

            EasyMock.verify(session, request);
        }

        {
            Capture map = new Capture();
            HttpSession session = EasyMock.createStrictMock(HttpSession.class);
            EasyMock.expect(session.getAttribute(key)).andReturn(null);
            session.setAttribute(eq(key), map.capture());
            EasyMock.replay(session);

            HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
            EasyMock.expect(request.getSession(true)).andReturn(session);
            EasyMock.replay(request);

            SessionScope scope = new SessionScope(request);
            scope.addFieldMessage(type, "user.name", "Test message");
            Map<String, List<String>> messages = (Map<String, List<String>>) map.object;
            assertEquals(1, messages.size());
            assertEquals(1, messages.get("user.name").size());
            assertEquals("Test message", messages.get("user.name").get(0));

            EasyMock.verify(session, request);
        }
    }
}
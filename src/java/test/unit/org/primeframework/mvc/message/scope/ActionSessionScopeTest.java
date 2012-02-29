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
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.example.action.user.Edit;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.testng.annotations.Test;

import static java.util.Arrays.*;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * <p> This tests the action session scope. </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ActionSessionScopeTest {
  @Test
  public void testGetActionMessage() {
    Map<String, List<String>> actionSession = new HashMap<String, List<String>>();
    actionSession.put("org.example.action.user.Edit", asList("Test message"));

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute(ActionSessionScope.ACTION_SESSION_ACTION_MESSAGE_KEY)).andReturn(actionSession);
    EasyMock.replay(session);

    HttpServletRequest request = makeRequest(session, false);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    List<String> messages = scope.getActionMessages(MessageType.PLAIN);
    assertEquals(1, messages.size());
    assertEquals("Test message", messages.get(0));

    EasyMock.verify(request, session);
  }

  @Test
  public void testGetActionMessageNoSession() {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(false)).andReturn(null);
    replay(request);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    List<String> messages = scope.getActionMessages(MessageType.PLAIN);
    assertEquals(0, messages.size());

    EasyMock.verify(request);
  }

  @Test
  public void testSetActionMessage() {
    Capture<Object> map = new Capture<Object>();
    HttpSession session = makeSession(map, ActionSessionScope.ACTION_SESSION_ACTION_MESSAGE_KEY);
    HttpServletRequest request = makeRequest(session, true);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    scope.addActionMessage(MessageType.PLAIN, "Test message");

    verifyAction(map, session, request);
  }

  @Test
  public void testGetActionError() {
    Map<String, List<String>> actionSession = new HashMap<String, List<String>>();
    actionSession.put("org.example.action.user.Edit", asList("Test message"));

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute(ActionSessionScope.ACTION_SESSION_ACTION_ERROR_KEY)).andReturn(actionSession);
    EasyMock.replay(session);

    HttpServletRequest request = makeRequest(session, false);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    List<String> messages = scope.getActionMessages(MessageType.ERROR);
    assertEquals(1, messages.size());
    assertEquals("Test message", messages.get(0));

    EasyMock.verify(request, session);
  }

  @Test
  public void testGetActionErrorNoSession() {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(false)).andReturn(null);
    replay(request);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    List<String> messages = scope.getActionMessages(MessageType.ERROR);
    assertEquals(0, messages.size());

    EasyMock.verify(request);
  }

  @Test
  public void testSetActionError() {
    Capture<Object> map = new Capture<Object>();
    HttpSession session = makeSession(map, ActionSessionScope.ACTION_SESSION_ACTION_ERROR_KEY);
    HttpServletRequest request = makeRequest(session, true);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    scope.addActionMessage(MessageType.ERROR, "Test message");

    verifyAction(map, session, request);
  }

  @Test
  public void testGetFieldMessage() {
    FieldMessages fm = new FieldMessages();
    fm.addMessage("user.name", "Test message");

    Map<String, FieldMessages> actionSession = new HashMap<String, FieldMessages>();
    actionSession.put("org.example.action.user.Edit", fm);

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute(ActionSessionScope.ACTION_SESSION_FIELD_MESSAGE_KEY)).andReturn(actionSession);
    EasyMock.replay(session);

    HttpServletRequest request = makeRequest(session, false);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    Map<String, List<String>> messages = scope.getFieldMessages(MessageType.PLAIN);
    assertEquals(1, messages.size());
    assertEquals(1, messages.get("user.name").size());
    assertEquals("Test message", messages.get("user.name").get(0));

    EasyMock.verify(request, session);
  }

  @Test
  public void testGetFieldMessageNoSession() {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(false)).andReturn(null);
    replay(request);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    Map<String, List<String>> messages = scope.getFieldMessages(MessageType.PLAIN);
    assertEquals(0, messages.size());

    EasyMock.verify(request);
  }

  @Test
  public void testSetFieldMessage() {
    Capture<Object> map = new Capture<Object>();
    HttpSession session = makeSession(map, ActionSessionScope.ACTION_SESSION_FIELD_MESSAGE_KEY);
    HttpServletRequest request = makeRequest(session, true);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    scope.addFieldMessage(MessageType.PLAIN, "user.name", "Test message");

    verifyField(map, session, request);
  }

  @Test
  public void testGetFieldErrors() {
    FieldMessages fm = new FieldMessages();
    fm.addMessage("user.name", "Test message");

    Map<String, FieldMessages> actionSession = new HashMap<String, FieldMessages>();
    actionSession.put("org.example.action.user.Edit", fm);

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute(ActionSessionScope.ACTION_SESSION_FIELD_ERROR_KEY)).andReturn(actionSession);
    EasyMock.replay(session);

    HttpServletRequest request = makeRequest(session, false);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    Map<String, List<String>> messages = scope.getFieldMessages(MessageType.ERROR);
    assertEquals(1, messages.size());
    assertEquals(1, messages.get("user.name").size());
    assertEquals("Test message", messages.get("user.name").get(0));

    EasyMock.verify(request, session);
  }

  @Test
  public void testGetFieldErrorsNoSession() {
    HttpServletRequest request = createStrictMock(HttpServletRequest.class);
    expect(request.getSession(false)).andReturn(null);
    replay(request);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    Map<String, List<String>> messages = scope.getFieldMessages(MessageType.ERROR);
    assertEquals(0, messages.size());

    EasyMock.verify(request);
  }

  @Test
  public void testSetFieldError() {
    Capture<Object> map = new Capture<Object>();
    HttpSession session = makeSession(map, ActionSessionScope.ACTION_SESSION_FIELD_ERROR_KEY);
    HttpServletRequest request = makeRequest(session, true);

    final Edit action = new Edit();
    ActionSessionScope scope = new ActionSessionScope(request, new ActionInvocationStore() {
      public ActionInvocation getCurrent() {
        return new DefaultActionInvocation(action, "", null, null);
      }

      public void setCurrent(ActionInvocation invocation) {
      }

      public void removeCurrent() {
      }

      public Deque<ActionInvocation> getDeque() {
        return null;
      }
    });
    scope.addFieldMessage(MessageType.ERROR, "user.name", "Test message");

    verifyField(map, session, request);
  }


  // --------------------------------- Helper Methods ----------------------------------

  private HttpServletRequest makeRequest(HttpSession session, boolean create) {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(create)).andReturn(session);
    EasyMock.replay(request);
    return request;
  }

  private HttpSession makeSession(Capture<Object> map, String key) {
    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute(key)).andReturn(null);
    session.setAttribute(eq(key), EasyMock.capture(map));
    EasyMock.replay(session);
    return session;
  }

  private void verifyAction(Capture map, HttpSession session, HttpServletRequest request) {
    Map<String, List<String>> actionSession = (Map<String, List<String>>) map.getValue();
    assertEquals(1, actionSession.size());
    assertEquals(1, actionSession.get("org.example.action.user.Edit").size());
    assertEquals("Test message", actionSession.get("org.example.action.user.Edit").get(0));

    EasyMock.verify(session, request);
  }

  private void verifyField(Capture map, HttpSession session, HttpServletRequest request) {
    Map<String, Map<String, List<String>>> actionSession = (Map<String, Map<String, List<String>>>) map.getValue();
    assertEquals(1, actionSession.size());
    assertEquals(1, actionSession.get("org.example.action.user.Edit").size());
    assertEquals(1, actionSession.get("org.example.action.user.Edit").get("user.name").size());
    assertEquals("Test message", actionSession.get("org.example.action.user.Edit").get("user.name").get(0));

    EasyMock.verify(session, request);
  }
}
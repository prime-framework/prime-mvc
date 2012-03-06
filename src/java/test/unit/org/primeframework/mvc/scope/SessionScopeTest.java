/*
 * Copyright (c) 2001-2010, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.scope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;

import org.easymock.EasyMock;
import org.primeframework.mvc.scope.annotation.Session;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * This class tests the action session scope.
 *
 * @author Brian Pontarelli
 */
public class SessionScopeTest {
  @Test
  public void get() {
    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("test")).andReturn("value");
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(session);
    EasyMock.replay(request);

    SessionScope scope = new SessionScope(request);
    assertSame("value", scope.get("test", new Session() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return Session.class;
      }

      public Class<? extends Annotation> annotationType() {
        return Session.class;
      }
    }));

    EasyMock.verify(session, request);
  }

  @Test
  public void getNoSession() {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(null);
    EasyMock.replay(request);

    SessionScope scope = new SessionScope(request);
    assertNull(scope.get("test", new Session() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return Session.class;
      }

      public Class<? extends Annotation> annotationType() {
        return Session.class;
      }
    }));

    EasyMock.verify(request);
  }

  @Test
  public void set() {
    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    session.setAttribute("test", "value");
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(true)).andReturn(session);
    EasyMock.replay(request);

    SessionScope scope = new SessionScope(request);
    scope.set("test", "value", new Session() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return Session.class;
      }

      public Class<? extends Annotation> annotationType() {
        return Session.class;
      }
    });

    EasyMock.verify(session, request);
  }

  @Test
  public void setNullSession() {
    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    session.removeAttribute("test");
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(session);
    EasyMock.replay(request);

    SessionScope scope = new SessionScope(request);
    scope.set("test", null, new Session() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return Session.class;
      }

      public Class<? extends Annotation> annotationType() {
        return Session.class;
      }
    });

    EasyMock.verify(session, request);
  }

  @Test
  public void setNullNoSession() {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(null);
    EasyMock.replay(request);

    SessionScope scope = new SessionScope(request);
    scope.set("test", null, new Session() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return Session.class;
      }

      public Class<? extends Annotation> annotationType() {
        return Session.class;
      }
    });

    EasyMock.verify(request);
  }
}

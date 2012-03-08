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
package org.primeframework.mvc.scope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.example.action.Simple;
import org.example.action.user.Edit;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.scope.annotation.ActionSession;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * This class tests the action session scope.
 *
 * @author Brian Pontarelli
 */
public class ActionSessionScopeTest {
  @Test
  public void get() {
    Object value = new Object();
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
    Map<String, Object> as = new HashMap<String, Object>();
    map.put("org.example.action.user.Edit", as);
    as.put("test", value);

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(new Edit(), null, null, null));
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    assertSame(value, scope.get("test", new ActionSession() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return ActionSession.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    }));

    EasyMock.verify(session, request, ais);
  }

  @Test
  public void getNoSession() {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(null);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    assertNull(scope.get("test", new ActionSession() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return ActionSession.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    }));

    EasyMock.verify(request, ais);
  }

  @Test
  public void getOtherActionSession() {
    Object value = new Object();
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
    Map<String, Object> as = new HashMap<String, Object>();
    map.put("org.example.action.Simple", as);
    as.put("test", value);

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    assertSame(value, scope.get("test", new ActionSession() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return Simple.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    }));

    EasyMock.verify(session, request, ais);
  }

  @Test
  public void getDifferentKey() {
    Object value = new Object();
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
    Map<String, Object> as = new HashMap<String, Object>();
    map.put("org.example.action.user.Edit", as);
    as.put("other", value);

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(new Edit(), null, null, null));
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    assertSame(value, scope.get("test", new ActionSession() {
      public String value() {
        return "other";
      }

      public Class<?> action() {
        return ActionSession.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    }));

    EasyMock.verify(session, request, ais);
  }

  @Test
  public void getDifferentKeyOtherActionSession() {
    Object value = new Object();
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
    Map<String, Object> as = new HashMap<String, Object>();
    map.put("org.example.action.Simple", as);
    as.put("other", value);

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    assertSame(value, scope.get("test", new ActionSession() {
      public String value() {
        return "other";
      }

      public Class<?> action() {
        return Simple.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    }));

    EasyMock.verify(session, request, ais);
  }

  @Test
  public void failedGetNoAction() {
    Object value = new Object();
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
    Map<String, Object> as = new HashMap<String, Object>();
    map.put("org.example.action.user.Edit", as);
    as.put("test", value);

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(null, null, null, null));
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    try {
      scope.get("test", new ActionSession() {
        public String value() {
          return "##field-name##";
        }

        public Class<?> action() {
          return ActionSession.class;
        }

        public Class<? extends Annotation> annotationType() {
          return ActionSession.class;
        }
      });
      fail("Should have failed");
    } catch (IllegalStateException e) {
      // Expected
    }

    EasyMock.verify(session, request, ais);
  }

  @Test
  public void set() {
    Object value = new Object();
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(true)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(new Edit(), null, null, null));
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    scope.set("test", value, new ActionSession() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return ActionSession.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    });
    assertSame(value, map.get("org.example.action.user.Edit").get("test"));

    EasyMock.verify(session, request, ais);
  }

  @Test
  public void setNull() {
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
    map.put("org.example.action.user.Edit", new HashMap<String, Object>());
    map.get("org.example.action.user.Edit").put("test", "value");

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(new Edit(), null, null, null));
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    scope.set("test", null, new ActionSession() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return ActionSession.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    });
    assertFalse(map.get("org.example.action.user.Edit").containsKey("test"));

    EasyMock.verify(session, request, ais);
  }

  @Test
  public void setNullNoSession() {
    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(false)).andReturn(null);
    EasyMock.replay(request);

    ActionSessionScope scope = new ActionSessionScope(request, null);
    scope.set("test", null, new ActionSession() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return ActionSession.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    });

    EasyMock.verify(request);
  }

  @Test
  public void setOtherActionSession() {
    Object value = new Object();
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(true)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    scope.set("test", value, new ActionSession() {
      public String value() {
        return "##field-name##";
      }

      public Class<?> action() {
        return Simple.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    });
    assertSame(value, map.get("org.example.action.Simple").get("test"));

    EasyMock.verify(session, request, ais);
  }

  @Test
  public void setDifferentKey() {
    Object value = new Object();
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(true)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(new Edit(), null, null, null));
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    scope.set("test", value, new ActionSession() {
      public String value() {
        return "other";
      }

      public Class<?> action() {
        return ActionSession.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    });
    assertSame(value, map.get("org.example.action.user.Edit").get("other"));

    EasyMock.verify(session, request, ais);
  }

  @Test
  public void setDifferentKeyOtherActionSession() {
    Object value = new Object();
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(true)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    scope.set("test", value, new ActionSession() {
      public String value() {
        return "other";
      }

      public Class<?> action() {
        return Simple.class;
      }

      public Class<? extends Annotation> annotationType() {
        return ActionSession.class;
      }
    });
    assertSame(value, map.get("org.example.action.Simple").get("other"));

    EasyMock.verify(session, request, ais);
  }

  @Test
  public void failedSetNoAction() {
    Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();

    HttpSession session = EasyMock.createStrictMock(HttpSession.class);
    EasyMock.expect(session.getAttribute("primeActionSession")).andReturn(map);
    EasyMock.replay(session);

    HttpServletRequest request = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(request.getSession(true)).andReturn(session);
    EasyMock.replay(request);

    ActionInvocationStore ais = EasyMock.createStrictMock(ActionInvocationStore.class);
    EasyMock.expect(ais.getCurrent()).andReturn(new DefaultActionInvocation(null, null, null, null));
    EasyMock.replay(ais);

    ActionSessionScope scope = new ActionSessionScope(request, ais);
    try {
      scope.set("test", new Object(), new ActionSession() {
        public String value() {
          return "##field-name##";
        }

        public Class<?> action() {
          return ActionSession.class;
        }

        public Class<? extends Annotation> annotationType() {
          return ActionSession.class;
        }
      });
      fail("Should have failed");
    } catch (IllegalStateException e) {
      // Expected
    }

    EasyMock.verify(session, request, ais);
  }
}

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
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.scope.annotation.ActionSession;

/**
 * This is the request scope which fetches and stores values in the HttpSession, but those values are associated with a
 * specific action. In order to accomplish this, a Map is put into the session under the key
 * <strong>primeActionSession</code> and the values are stored inside that Map. The key is the name of the action
 * class and the value is a Map. The second Map's key is the fieldName and the value is the value being stored.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ActionSessionScope implements Scope<ActionSession> {
  public static final String ACTION_SESSION_KEY = "primeActionSession";

  private final ActionInvocationStore actionInvocationStore;

  private final HttpServletRequest request;

  @Inject
  public ActionSessionScope(HttpServletRequest request, ActionInvocationStore actionInvocationStore) {
    this.actionInvocationStore = actionInvocationStore;
    this.request = request;
  }

  /**
   * Helper method used when you may need to manually extract an action session value prior to the MVC workflow.
   *
   * @param request the HTTP servlet request
   * @param key     the key
   * @param action  the action class
   * @param <T>     the type
   * @return the value found in the session or null if no value is found.
   */
  public static <T> T get(HttpServletRequest request, String key, Class<?> action) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> actionSession = (Map<String, Object>) session.getAttribute(ACTION_SESSION_KEY);
      if (actionSession != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> requestedSession = (Map<String, Object>) actionSession.get(action.getName());
        if (requestedSession != null) {
          //noinspection unchecked
          return (T) requestedSession.get(key);
        }
      }
    } catch (Exception ignore) {
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Object get(String fieldName, ActionSession scope) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      Map<String, Map<String, Object>> actionSession = (Map<String, Map<String, Object>>) session.getAttribute(ACTION_SESSION_KEY);
      if (actionSession == null) {
        return null;
      }

      String className = getActionClassName(scope);
      Map<String, Object> values = actionSession.get(className);
      if (values == null) {
        return null;
      }

      String key = scope.value().equals("##field-name##") ? fieldName : scope.value();
      return values.get(key);
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void set(String fieldName, Object value, ActionSession scope) {
    HttpSession session;
    if (value != null) {
      session = request.getSession(true);
    } else {
      session = request.getSession(false);
    }

    if (session == null) {
      return;
    }

    Map<String, Map<String, Object>> actionSession = (Map<String, Map<String, Object>>) session.getAttribute(ACTION_SESSION_KEY);
    if (actionSession == null) {
      actionSession = new HashMap<>();
      session.setAttribute(ACTION_SESSION_KEY, actionSession);
    }

    String className = getActionClassName(scope);
    Map<String, Object> values = actionSession.computeIfAbsent(className, k -> new HashMap<>());

    String key = scope.value().equals("##field-name##") ? fieldName : scope.value();
    if (value != null) {
      values.put(key, value);
    } else {
      values.remove(key);
    }
  }

  /**
   * Using the annotation or the current action invocation, this determines the name of the action used to get the
   * action session.
   *
   * @param scope The scope annotation.
   * @return The action class name.
   */
  protected String getActionClassName(ActionSession scope) {
    String className;
    if (scope.action() != ActionSession.class) {
      className = scope.action().getName();
    } else {
      ActionInvocation ai = actionInvocationStore.getCurrent();
      if (ai.action == null) {
        throw new PrimeException("Attempting to store a value in the action session but the current request URL isn'" +
            "t associated with an action class");
      }
      className = ai.action.getClass().getName();
    }
    return className;
  }
}

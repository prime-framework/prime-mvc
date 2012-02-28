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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

/**
 * <p> This is the message scope which fetches and stores values in the HttpSession. </p>
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class SessionScope extends AbstractJEEScope {
  private final HttpServletRequest request;

  @Inject
  public SessionScope(HttpServletRequest request) {
    this.request = request;
  }

  /**
   * Correctly synchronizes the session.
   *
   * @param type Used to determine the key to lookup the message map from.
   * @return The Map or an empty map if the session doesn't contain the FieldMessages yet.
   */
  public Map<String, List<String>> getFieldMessages(MessageType type) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      FieldMessages messages;
      synchronized (session) {
        messages = (FieldMessages) session.getAttribute(fieldKey(type));
        if (messages == null) {
          return Collections.emptyMap();
        }

        // Copy the map to protect it from threading
        return new LinkedHashMap<String, List<String>>(messages);
      }
    }

    return Collections.emptyMap();
  }

  /**
   * Correctly synchronizes the context and the FieldMessages object.
   *
   * @param type      Used to determine the key to lookup the message map from.
   * @param fieldName The name of the field.
   * @param message   The message to append.
   */
  public void addFieldMessage(MessageType type, String fieldName, String message) {
    HttpSession session = request.getSession(true);
    FieldMessages messages;
    synchronized (session) {
      String key = fieldKey(type);
      messages = (FieldMessages) session.getAttribute(key);
      if (messages == null) {
        messages = new FieldMessages();
        session.setAttribute(key, messages);
      }
    }

    synchronized (messages) {
      messages.addMessage(fieldName, message);
    }
  }

  /**
   * Correctly synchronizes the session.
   *
   * @param type Used to determine the key to lookup the message map from.
   * @return The List or an empty List if the session doesn't contain the action messages yet.
   */
  public List<String> getActionMessages(MessageType type) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      List<String> messages;
      synchronized (session) {
        messages = (List<String>) session.getAttribute(actionKey(type));
        if (messages == null) {
          return Collections.emptyList();
        }

        // Copy the map to protect it from threading
        return new ArrayList<String>(messages);
      }
    }

    return Collections.emptyList();
  }

  /**
   * Correctly synchronizes the session and the action messages List.
   *
   * @param type    Used to determine the key to lookup the message map from.
   * @param message The message to append.
   */
  public void addActionMessage(MessageType type, String message) {
    HttpSession session = request.getSession(true);
    List<String> messages;
    synchronized (session) {
      String key = actionKey(type);
      messages = (List<String>) session.getAttribute(key);
      if (messages == null) {
        messages = new ArrayList<String>();
        session.setAttribute(key, messages);
      }
    }

    synchronized (messages) {
      messages.add(message);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void clearActionMessages(MessageType type) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      synchronized (session) {
        if (type == MessageType.ERROR) {
          session.removeAttribute(ACTION_ERROR_KEY);
        } else {
          session.removeAttribute(ACTION_MESSAGE_KEY);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void clearFieldMessages(MessageType type) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      synchronized (session) {
        if (type == MessageType.ERROR) {
          session.removeAttribute(FIELD_ERROR_KEY);
        } else {
          session.removeAttribute(FIELD_MESSAGE_KEY);
        }
      }
    }
  }
}

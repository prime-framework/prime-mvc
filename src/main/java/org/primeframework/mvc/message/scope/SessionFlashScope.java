/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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
import java.util.Collection;
import java.util.List;

import com.google.inject.Inject;
import org.primeframework.mvc.message.Message;

/**
 * This is the flash scope which stores messages in the HttpSession under the flash key. It fetches values from the
 * HttpServletRequest under the same key as well as the HttpSession under that key. This allows for flash messages to be
 * migrated from the session to the request during request handling so that they are not persisted in the session
 * forever. However, it also allows flash values to be retrieved during the initial request from the session.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class SessionFlashScope extends AbstractSessionScope implements FlashScope {
  @Inject
  public SessionFlashScope(HttpServletRequest request) {
    super(request, KEY);
  }

  @Override
  public void add(Message message) {
    addMessage(message);
  }

  @Override
  public void addAll(Collection<Message> messages) {
    addAllMessages(messages);
  }

  @Override
  public void clear() {
    request.removeAttribute(KEY);

    HttpSession session = request.getSession(false);
    if (session != null) {
      session.removeAttribute(KEY);
    }
  }

  @Override
  public List<Message> get() {
    List<Message> messages = new ArrayList<>();
    List<Message> requestList = (List<Message>) request.getAttribute(KEY);
    if (requestList != null) {
      messages.addAll(requestList);
    }

    HttpSession session = request.getSession(false);
    if (session != null) {
      //noinspection SynchronizationOnLocalVariableOrMethodParameter
      synchronized (session) {
        List<Message> sessionList = (List<Message>) session.getAttribute(KEY);
        if (sessionList != null) {
          messages.addAll(sessionList);
        }
      }
    }

    return messages;
  }

  /**
   * Moves the flash from the session to the request.
   */
  @Override
  public void transferFlash() {
    HttpSession session = request.getSession(false);
    if (session != null) {
      //noinspection SynchronizationOnLocalVariableOrMethodParameter
      synchronized (session) {
        List<Message> messages = (List<Message>) session.getAttribute(KEY);
        if (messages != null) {
          session.removeAttribute(KEY);
          request.setAttribute(KEY, messages);
        }
      }
    }
  }
}

/*
 * Copyright (c) 2018, Inversoft Inc., All Rights Reserved
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

import org.primeframework.mvc.message.Message;

/**
 * @author Daniel DeGroff
 */
public abstract class AbstractSessionScope {
  protected final HttpServletRequest request;

  private final String sessionKey;

  public AbstractSessionScope(HttpServletRequest request, String sessionKey) {
    this.request = request;
    this.sessionKey = sessionKey;
  }

  @SuppressWarnings("unchecked")
  protected void addAllMessages(Collection<Message> messages) {
    if (messages.isEmpty()) {
      return;
    }

    HttpSession session = request.getSession(true);
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (session) {
      List<Message> scopeMessages = (List<Message>) session.getAttribute(sessionKey);
      if (scopeMessages == null) {
        scopeMessages = new ArrayList<>();
        session.setAttribute(sessionKey, scopeMessages);
      }

      scopeMessages.addAll(messages);
    }
  }

  @SuppressWarnings("unchecked")
  protected void addMessage(Message message) {
    HttpSession session = request.getSession(true);
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (session) {
      List<Message> messages = (List<Message>) session.getAttribute(sessionKey);
      if (messages == null) {
        messages = new ArrayList<>();
        session.setAttribute(sessionKey, messages);
      }

      messages.add(message);
    }
  }
}

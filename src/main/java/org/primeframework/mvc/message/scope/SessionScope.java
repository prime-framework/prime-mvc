/*
 * Copyright (c) 2001-2018, Inversoft Inc., All Rights Reserved
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.primeframework.mvc.message.Message;

import com.google.inject.Inject;

/**
 * This is the message scope which fetches and stores values in the HttpSession.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class SessionScope extends AbstractSessionScope implements Scope {
  public static final String KEY = "primeMessages";

  @Inject
  public SessionScope(HttpServletRequest request) {
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
  public List<Message> get() {
    HttpSession session = request.getSession(false);
    if (session != null) {
      synchronized (session) {
        List<Message> messages = (List<Message>) session.getAttribute(KEY);
        if (messages != null) {
          return messages;
        }
      }
    }

    return Collections.emptyList();
  }

  @Override
  public void clear() {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.removeAttribute(KEY);
    }
  }
}

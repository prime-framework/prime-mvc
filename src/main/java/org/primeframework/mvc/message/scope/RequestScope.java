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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.primeframework.mvc.message.Message;

import com.google.inject.Inject;

/**
 * This is the message scope which fetches and stores values in the HttpServletRequest.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class RequestScope implements Scope {
  public static final String KEY = "primeMessages";
  private final HttpServletRequest request;

  @Inject
  public RequestScope(HttpServletRequest request) {
    this.request = request;
  }

  @Override
  public void add(Message message) {
    List<Message> messages = (List<Message>) request.getAttribute(KEY);
    if (messages == null) {
      messages = new ArrayList<Message>();
      request.setAttribute(KEY, messages);
    }

    messages.add(message);
  }

  @Override
  public void addAll(Collection<Message> messages) {
    List<Message> scopeMessages = (List<Message>) request.getAttribute(KEY);
    if (scopeMessages == null) {
      scopeMessages = new ArrayList<Message>();
      request.setAttribute(KEY, scopeMessages);
    }

    scopeMessages.addAll(messages);
  }

  @Override
  public List<Message> get() {
    List<Message> messages = (List<Message>) request.getAttribute(KEY);
    if (messages != null) {
      return messages;
    }

    return Collections.emptyList();
  }

  @Override
  public void clear() {
    request.removeAttribute(KEY);
  }
}

/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;
import org.primeframework.mvc.http.HTTPContext;
import org.primeframework.mvc.message.Message;

/**
 * This is the message scope which fetches and stores values in the Context The values are stored in the servlet context
 * using a variety of different keys.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ApplicationScope implements Scope {
  public static final String KEY = "primeMessages";

  private final HTTPContext context;

  @Inject
  public ApplicationScope(HTTPContext context) {
    this.context = context;
  }

  @Override
  public void add(Message message) {
    List<Message> messages = (List<Message>) context.getAttribute(KEY);
    if (messages == null) {
      messages = new ArrayList<>();
      context.setAttribute(KEY, messages);
    }

    messages.add(message);
  }

  @Override
  public void addAll(Collection<Message> messages) {
    List<Message> scopeMessages = (List<Message>) context.getAttribute(KEY);
    if (scopeMessages == null) {
      scopeMessages = new ArrayList<>();
      context.setAttribute(KEY, scopeMessages);
    }

    scopeMessages.addAll(messages);
  }

  @Override
  public void clear() {
    context.getAttributes().remove(KEY);
  }

  @Override
  public List<Message> get() {
    List<Message> messages = (List<Message>) context.getAttribute(KEY);
    if (messages != null) {
      return messages;
    }

    return Collections.emptyList();
  }
}

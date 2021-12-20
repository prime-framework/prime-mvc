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
package org.primeframework.mvc.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import org.primeframework.mvc.message.scope.ApplicationScope;
import org.primeframework.mvc.message.scope.FlashScope;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.message.scope.RequestScope;
import org.primeframework.mvc.message.scope.Scope;

/**
 * This is the default message workflow implementation. It removes all flash messages from the session and places them
 * in the request.
 *
 * @author Brian Pontarelli
 */
public class DefaultMessageStore implements MessageStore {
  private final Map<MessageScope, Scope> scopes = new LinkedHashMap<>();

  private MessageObserver observer;

  @Inject
  public DefaultMessageStore(ApplicationScope applicationScope, FlashScope flashScope, RequestScope requestScope) {
    scopes.put(MessageScope.REQUEST, requestScope);
    scopes.put(MessageScope.FLASH, flashScope);
    scopes.put(MessageScope.APPLICATION, applicationScope);
  }

  @Override
  public void add(Message message) {
    add(MessageScope.REQUEST, message);
  }

  @Override
  public void add(MessageScope scope, Message message) {
    Scope s = scopes.get(scope);
    s.add(message);

    if (observer != null) {
      observer.added(scope, message);
    }
  }

  @Override
  public void addAll(MessageScope scope, Collection<Message> messages) {
    Scope s = scopes.get(scope);
    s.addAll(messages);

    if (observer != null) {
      messages.forEach(m -> observer.added(scope, m));
    }
  }

  @Override
  public void clear() {
    for (MessageScope scope : scopes.keySet()) {
      clear(scope);
    }
  }

  @Override
  public void clear(MessageScope scope) {
    scopes.get(scope).clear();

    if (observer != null) {
      observer.cleared(scope);
    }
  }

  @Override
  public List<Message> get() {
    List<Message> messages = new ArrayList<>();
    for (Scope scope : scopes.values()) {
      messages.addAll(scope.get());
    }
    return messages;
  }

  @Override
  public List<Message> get(MessageScope scope) {
    Scope s = scopes.get(scope);
    return new ArrayList<>(s.get());
  }

  @Override
  public Map<String, List<FieldMessage>> getFieldMessages() {
    return getFieldMessages(null);
  }

  @Override
  public Map<String, List<FieldMessage>> getFieldMessages(MessageScope scope) {
    List<Message> messages = scope == null ? get() : get(scope);
    Map<String, List<FieldMessage>> map = new HashMap<>();
    for (Message message : messages) {
      if (message instanceof FieldMessage) {
        FieldMessage fm = (FieldMessage) message;
        List<FieldMessage> list = map.computeIfAbsent(fm.getField(), k -> new ArrayList<>());
        list.add(fm);
      }
    }

    return map;
  }

  @Override
  public List<Message> getGeneralMessages() {
    List<Message> messages = get();
    List<Message> list = new ArrayList<>();
    for (Message message : messages) {
      if (!(message instanceof FieldMessage)) {
        list.add(message);
      }
    }

    return list;
  }

  @Inject(optional = true)
  public void setObserver(MessageObserver observer) {
    this.observer = observer;
  }
}

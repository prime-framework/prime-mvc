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
package org.primeframework.mvc.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.message.scope.Scope;

import com.google.inject.Inject;

/**
 * This is the default message workflow implementation. It removes all flash messages from the session and places them
 * in the request.
 *
 * @author Brian Pontarelli
 */
public class DefaultMessageStore implements MessageStore {
  private final Map<MessageScope, Scope> scopes;

  @Inject
  public DefaultMessageStore(Map<MessageScope, Scope> scopes) {
    this.scopes = scopes;
  }

  @Override
  public void add(Message message) {
    Scope scope = scopes.get(MessageScope.REQUEST);
    scope.add(message);
  }

  @Override
  public void add(MessageScope scope, Message message) {
    Scope s = scopes.get(scope);
    s.add(message);
  }

  @Override
  public void addAll(MessageScope scope, Collection<Message> messages) {
    Scope s = scopes.get(scope);
    s.addAll(messages);
  }

  @Override
  public List<Message> get() {
    List<Message> messages = new ArrayList<Message>();
    for (Scope scope : scopes.values()) {
      messages.addAll(scope.get());
    }
    return messages;
  }

  @Override
  public List<Message> get(MessageScope scope) {
    List<Message> messages = new ArrayList<Message>();
    Scope s = scopes.get(scope);
    messages.addAll(s.get());
    return messages;
  }

  @Override
  public Map<String, List<FieldMessage>> getFieldMessages() {
    List<Message> messages = get();
    Map<String, List<FieldMessage>> map = new HashMap<String, List<FieldMessage>>();
    for (Message message : messages) {
      if (message instanceof FieldMessage) {
        FieldMessage fm = (FieldMessage) message;
        List<FieldMessage> list = map.get(fm.getField());
        if (list == null) {
          list = new ArrayList<FieldMessage>();
          map.put(fm.getField(), list);
        }

        list.add(fm);
      }
    }

    return map;
  }

  @Override
  public Map<String, List<FieldMessage>> getFieldMessages(MessageScope scope) {
    List<Message> messages = get(scope);
    Map<String, List<FieldMessage>> map = new HashMap<String, List<FieldMessage>>();
    for (Message message : messages) {
      if (message instanceof FieldMessage) {
        FieldMessage fm = (FieldMessage) message;
        List<FieldMessage> list = map.get(fm.getField());
        if (list == null) {
          list = new ArrayList<FieldMessage>();
          map.put(fm.getField(), list);
        }

        list.add(fm);
      }
    }

    return map;
  }
}

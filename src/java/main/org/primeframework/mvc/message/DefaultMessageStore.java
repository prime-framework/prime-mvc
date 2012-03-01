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
import java.util.List;

import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.message.scope.Scope;
import org.primeframework.mvc.message.scope.ScopeProvider;

import com.google.inject.Inject;

/**
 * This is the default message workflow implementation. It removes all flash messages from the session and places them
 * in the request.
 *
 * @author Brian Pontarelli
 */
public class DefaultMessageStore implements MessageStore {
  private final ScopeProvider scopeProvider;

  @Inject
  public DefaultMessageStore(ScopeProvider scopeProvider) {
    this.scopeProvider = scopeProvider;
  }

  @Override
  public void add(Message message) {
    Scope scope = scopeProvider.lookup(MessageScope.REQUEST);
    scope.add(message);
  }

  @Override
  public void add(MessageScope scope, Message message) {
    Scope s = scopeProvider.lookup(scope);
    s.add(message);
  }

  @Override
  public List<Message> get() {
    List<Message> messages = new ArrayList<Message>();
    List<Scope> scopes = scopeProvider.getAllScopes();
    for (Scope scope : scopes) {
      messages.addAll(scope.get());
    }
    return messages;
  }

  @Override
  public List<Message> get(MessageScope scope) {
    List<Message> messages = new ArrayList<Message>();
    Scope s = scopeProvider.lookup(scope);
    messages.addAll(s.get());
    return messages;
  }
}

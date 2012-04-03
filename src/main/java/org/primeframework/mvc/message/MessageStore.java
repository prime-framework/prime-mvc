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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.primeframework.mvc.message.scope.MessageScope;

import com.google.inject.ImplementedBy;

/**
 * This interface defines the mechanism by which messages are added and fetched.
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultMessageStore.class)
public interface MessageStore {
  /**
   * Adds the given message to the request scope as an error message.
   *
   * @param message The message.
   */
  void add(Message message);

  /**
   * Adds the message in a scope.
   *
   * @param scope   The scope.
   * @param message The message.
   */
  void add(MessageScope scope, Message message);

  /**
   * Adds all the messages to the scope.
   *
   * @param scope    The scope.
   * @param messages The messages.
   */
  void addAll(MessageScope scope, Collection<Message> messages);

  /**
   * @return All of the messages in all the scopes.
   */
  List<Message> get();

  /**
   * @param scope The scope.
   * @return All of the messages in the scope.
   */
  List<Message> get(MessageScope scope);

  /**
   * @return All of the fields messages in all the scopes. This Map is not live.
   */
  Map<String, List<FieldMessage>> getFieldMessages();

  /**
   * @param scope The scope.
   * @return All of the fields messages in the given scope. This Map is not live.
   */
  Map<String, List<FieldMessage>> getFieldMessages(MessageScope scope);

  /**
   * Clears all messages in all scopes.
   */
  void clear();
}
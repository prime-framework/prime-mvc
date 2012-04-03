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

import java.util.Collection;
import java.util.List;

import org.primeframework.mvc.message.Message;

/**
 * This interface defines the handler for a specific message scope.
 *
 * @author Brian Pontarelli
 */
public interface Scope {
  /**
   * Adds the message to the scope.
   *
   * @param message The message.
   */
  void add(Message message);

  /**
   * Adds all the messages to the scope.
   *
   * @param messages The messages.
   */
  void addAll(Collection<Message> messages);

  /**
   * @return All the messages in the scope.
   */
  List<Message> get();

  /**
   * Clears the messages in the scope.
   */
  void clear();
}
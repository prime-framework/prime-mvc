/*
 * Copyright (c) 2021-2022, Inversoft Inc., All Rights Reserved
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

import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.message.scope.MessageScope;

/**
 * An observer that is notified when new messages are added to or cleared from the {@link MessageStore}.
 *
 * @author Brian Pontarelli
 */
public interface MessageObserver {
  /**
   * Called when a new message is added.
   *
   * @param httpRequest The current HTTP request.
   * @param scope       The scope of the message.
   * @param message     The message.
   */
  void messageAdded(HTTPRequest httpRequest, MessageScope scope, Message message);

  /**
   * Reset the message observer.
   */
  void reset();

  /**
   * Called when messages are cleared from the store.
   *
   * @param httpRequest The current HTTP request.
   * @param scope       The scope being cleared.
   */
  void scopeCleared(HTTPRequest httpRequest, MessageScope scope);
}

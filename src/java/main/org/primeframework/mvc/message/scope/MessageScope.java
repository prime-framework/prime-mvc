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

/**
 * <p> This class contains the various scopes that messages can be stored in the JCatapult MessageStore. </p>
 *
 * @author Brian Pontarelli
 */
public enum MessageScope {
  /**
   * The request scope, when messages are stored in the request.
   */
  REQUEST(RequestScope.class),

  /**
   * The flash scope, when messages are stored in the session, but are only available on the next request.
   */
  FLASH(FlashScope.class),

  /**
   * The session scope, when messages are stored in the session.
   */
  SESSION(SessionScope.class),

  /**
   * The action session scope, when messages are stored in the session, but are only associated and available to a
   * specific action.
   */
  ACTION_SESSION(ActionSessionScope.class),

  /**
   * The servlet context scope, when messages are stored in the servlet context. Useful for messages that should be
   * displayed to all users.
   */
  CONTEXT(ContextScope.class);

  private final Class<? extends Scope> type;

  private MessageScope(Class<? extends Scope> type) {
    this.type = type;
  }

  public Class<? extends Scope> getType() {
    return type;
  }
}
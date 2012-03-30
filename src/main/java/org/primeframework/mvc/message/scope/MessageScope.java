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
 * Defines a message scope that a message can be stored in the MessageStore under. This interface also defines the
 * implementation of the {@link Scope} that handles the storing and retrieving.
 *
 * @author Brian Pontarelli
 */
public interface MessageScope {
  /**
   * The request scope, when messages are stored in the request.
   */
  MessageScope REQUEST = new PrimeMessageScope(){};

  /**
   * The flash scope, when messages are stored in the session, but are only available on the next request.
   */
  MessageScope FLASH = new PrimeMessageScope(){};

  /**
   * The session scope, when messages are stored in the session.
   */
  MessageScope SESSION = new PrimeMessageScope(){};

  /**
   * The servlet context (application) scope, when messages are stored in the servlet context. Useful for messages that
   * should be displayed to all users.
   */
  MessageScope APPLICATION = new PrimeMessageScope(){};

  /**
   * Internal implementation for the Prime scopes.
   */
  class PrimeMessageScope implements MessageScope {
    private PrimeMessageScope() {
    }
  }
}
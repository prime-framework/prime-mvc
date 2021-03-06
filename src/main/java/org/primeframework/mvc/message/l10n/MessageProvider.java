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
package org.primeframework.mvc.message.l10n;

/**
 * This interface defines how localized messages can be found and processed. A common implementation will use
 * ResourceBundles and TextFormatter or something similar.
 *
 * @author Brian Pontarelli
 */
public interface MessageProvider {
  /**
   * Finds an untyped message with the given key and for the current locale. This should then process the message using
   * the attributes and values given in order to token replace and format the message correctly.
   *
   * @param key    The key of the message.
   * @param values Any additional values for the message.
   * @return The message.
   * @throws MissingMessageException If the message is missing.
   */
  String getMessage(String key, Object... values) throws MissingMessageException;

  /**
   * Finds an untyped message with the given key and for the current locale. This should then process the message using
   * the attributes and values given in order to token replace and format the message correctly.
   *
   * @param key    The key of the message.
   * @param values Any additional values for the message.
   * @return The message or null if the message doesn't exist.
   */
  String getOptionalMessage(String key, Object... values);
}
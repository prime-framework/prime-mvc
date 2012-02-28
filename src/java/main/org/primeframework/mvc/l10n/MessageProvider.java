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
package org.primeframework.mvc.l10n;

import java.util.Map;

import com.google.inject.ImplementedBy;

/**
 * <p> This interface defines how localized messages can be found and processed. A common implementation will use
 * ResourceBundles and TextFormatters or something similar. </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(ResourceBundleMessageProvider.class)
public interface MessageProvider {
  /**
   * Finds a message with the given key and for the given locale. This should then process the message using the
   * attributes and values given in order to token replace and format the message correctly.
   *
   * @param location   The name of the location to pull the message from. This might be a ResourceBundle or a database
   *                   or a table name, etc.
   * @param key        The key of the message.
   * @param attributes Any additional attributes for the message.
   * @param values     Any additional values for the message.
   * @return The message.
   * @throws MissingMessageException If the message is missing.
   */
  String getMessage(String location, String key, Map<String, String> attributes, Object... values)
    throws MissingMessageException;

  /**
   * Finds a message with the given key and for the given locale.
   *
   * @param location The name of the location to pull the message from. This might be a ResourceBundle or a database or
   *                 a table name, etc.
   * @param key      The key of the message.
   * @param values   Any additional values for the message.
   * @return The message.
   * @throws MissingMessageException If the message is missing.
   */
  String getMessage(String location, String key, Object... values) throws MissingMessageException;
}
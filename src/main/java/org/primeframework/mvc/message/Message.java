/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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

import java.util.Map;

/**
 * This is a marker interface for Prime messages.
 *
 * @author Brian Pontarelli
 */
public interface Message {
  /**
   * @return The code of the message (this is the code that was used to lookup the message in the MessageStore).
   */
  String getCode();

  /**
   * @return the map of key value pairs serialized with this message. May be null.
   */
  Map<String, Object> getData();

  /**
   * @return The type of the message.
   */
  MessageType getType();
}

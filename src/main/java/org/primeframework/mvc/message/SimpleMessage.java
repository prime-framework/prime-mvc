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
import java.util.Objects;

/**
 * A simple message.
 *
 * @author Brian Pontarelli
 */
public class SimpleMessage implements Message {
  public final String code;

  public final Map<String, Object> data;

  public final String message;

  public final MessageType type;

  // Jackson constructor
  public SimpleMessage() {
    this(null, null, null, null);
  }

  public SimpleMessage(MessageType type, String code, String message, Map<String, Object> data) {
    this.type = type;
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public SimpleMessage(MessageType type, String code, String message) {
    this.type = type;
    this.code = code;
    this.message = message;
    this.data = null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SimpleMessage that = (SimpleMessage) o;
    return Objects.equals(type, that.type) && Objects.equals(code, that.code) && Objects.equals(message, that.message) && Objects.equals(data, that.data);
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public Map<String, Object> getData() {
    return data;
  }

  @Override
  public MessageType getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, code, message, data);
  }

  @Override
  public String toString() {
    return message;
  }
}

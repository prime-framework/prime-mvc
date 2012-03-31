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

/**
 * A simple field message.
 *
 * @author Brian Pontarelli
 */
public class SimpleFieldMessage implements FieldMessage {
  public final MessageType type;
  public final String field;
  public final String message;

  public SimpleFieldMessage(MessageType type, String field, String message) {
    this.type = type;
    this.field = field;
    this.message = message;
  }

  @Override
  public MessageType getType() {
    return type;
  }

  @Override
  public String getField() {
    return field;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final SimpleFieldMessage that = (SimpleFieldMessage) o;
    return field.equals(that.field) && message.equals(that.message) && type.equals(that.type);
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + field.hashCode();
    result = 31 * result + message.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return message;
  }
}
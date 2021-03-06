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
 * Defines a message type.
 *
 * @author Brian Pontarelli
 */
public interface MessageType {
  /**
   * Prime error messages.
   */
  MessageType ERROR = new PrimeErrorMessageType();

  /**
   * Prime info messages.
   */
  MessageType INFO = new PrimeInfoMessageType();

  /**
   * Prime warning messages.
   */
  MessageType WARNING = new PrimeWarningMessageType();

  /**
   * Internal implementation for the Prime error message.
   */
  class PrimeErrorMessageType implements MessageType {
    private PrimeErrorMessageType() {
    }

    @Override
    public String toString() {
      return "ERROR";
    }
  }

  /**
   * Internal implementation for the Prime info message.
   */
  class PrimeInfoMessageType implements MessageType {
    private PrimeInfoMessageType() {
    }

    @Override
    public String toString() {
      return "INFO";
    }
  }

  /**
   * Internal implementation for the Prime warning message.
   */
  class PrimeWarningMessageType implements MessageType {
    private PrimeWarningMessageType() {
    }

    @Override
    public String toString() {
      return "WARNING";
    }
  }
}
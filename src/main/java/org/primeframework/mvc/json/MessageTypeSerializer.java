/*
 * Copyright (c) 2020, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.MessageType.PrimeInfoMessageType;
import org.primeframework.mvc.message.MessageType.PrimeWarningMessageType;

/**
 * @author Daniel DeGroff
 */
public class MessageTypeSerializer extends StdSerializer<MessageType> {
  public MessageTypeSerializer() {
    super(MessageType.class);
  }

  @Override
  public void serialize(MessageType value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if (value == null) {
      gen.writeNull();
    } else {
      if (value instanceof PrimeInfoMessageType) {
        gen.writeString("INFO");
      } else if (value instanceof PrimeWarningMessageType) {
        gen.writeString("WARING");
      } else {
        gen.writeString("ERROR");
      }
    }
  }
}

/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content.json.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.apache.commons.lang3.LocaleUtils;

import java.io.IOException;
import java.util.Locale;

/**
 * Jackson deserializer for Locales.
 *
 * @author Brian Pontarelli
 */
public class LocaleDeserializer extends StdScalarDeserializer<Locale> {
  protected LocaleDeserializer() {
    super(Locale.class);
  }

  @Override
  public Locale deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonToken t = jp.getCurrentToken();
    if (t == JsonToken.VALUE_STRING) {
      String str = jp.getText().trim();
      if (str.length() == 0) {
        return null;
      }

      return LocaleUtils.toLocale(str);
    }

    throw ctxt.mappingException(getValueClass());
  }
}

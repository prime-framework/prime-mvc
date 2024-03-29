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
package org.primeframework.mvc.parameter.convert.converters;

import java.util.Map;

import com.google.inject.Inject;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.annotation.GlobalConverter;

/**
 * This class is the type converter for characters.
 *
 * @author Brian Pontarelli
 */
@GlobalConverter
public class CharacterConverter extends AbstractPrimitiveConverter {
  @Inject
  public CharacterConverter(MVCConfiguration configuration) {
    super(configuration);
  }

  /**
   * Returns a single character with a unicode value of 0.
   */
  protected Object defaultPrimitive(Class convertTo, Map<String, String> attributes)
      throws ConversionException, ConverterStateException {
    return '\u0000';
  }

  protected String primitiveToString(Object value, Class convertFrom, Map<String, String> attributes)
      throws ConversionException, ConverterStateException {
    return value.toString();
  }

  /**
   * If String is longer than one character, this throws an exception. Otherwise, that character is returned. If the
   * value is null or empty, this throws an exception.
   */
  protected Object stringToPrimitive(String value, Class convertTo, Map<String, String> attributes)
      throws ConversionException, ConverterStateException {
    if (value.length() > 1) {
      throw new ConversionException("Conversion from String to character must be a String" +
          " of length 1 - [" + value + "] is invalid");
    }

    return value.charAt(0);
  }
}
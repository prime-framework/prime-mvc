/*
 * Copyright (c) 2001-2023, Inversoft Inc., All Rights Reserved
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

import java.lang.reflect.Type;
import java.util.Map;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.parameter.convert.AbstractGlobalConverter;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.MultipleParametersUnsupportedException;
import org.primeframework.mvc.parameter.convert.annotation.GlobalConverter;

/**
 * This converts to and from Enums.
 *
 * @author Brian Pontarelli
 */
@GlobalConverter
@SuppressWarnings("unchecked")
public class EnumConverter extends AbstractGlobalConverter {
  private final boolean emptyIsNull;

  @Inject
  public EnumConverter(MVCConfiguration configuration) {
    this.emptyIsNull = configuration.emptyParametersAreNull();
  }

  protected String objectToString(Object value, Type convertFrom, Map<String, String> attributes, String expression)
      throws ConversionException, ConverterStateException {
    return value.toString();
  }

  protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
      throws ConversionException, ConverterStateException {
    if (emptyIsNull && StringUtils.isBlank(value)) {
      return null;
    }

    try {
      return Enum.valueOf((Class<Enum>) convertTo, value);
    } catch (IllegalArgumentException e) {
      throw new ConversionException(e);
    }
  }

  protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> attributes, String expression)
      throws ConversionException, ConverterStateException {
    throw new MultipleParametersUnsupportedException("You are attempting to map a form field that contains " +
        "multiple parameters to a property on the action class that is of type Enum. This isn't " +
        "allowed.");
  }
}

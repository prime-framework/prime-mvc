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

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.primeframework.mvc.config.PrimeMVCConfiguration;
import org.primeframework.mvc.parameter.convert.AbstractGlobalConverter;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.annotation.GlobalConverter;

import com.google.inject.Inject;

/**
 * This converts to and from DateTime.
 *
 * @author Brian Pontarelli
 */
@GlobalConverter(forTypes = {DateTime.class})
public class DateTimeConverter extends AbstractGlobalConverter {
  private final boolean emptyIsNull;

  @Inject
  public DateTimeConverter(PrimeMVCConfiguration configuration) {
    this.emptyIsNull = configuration.emptyParametersAreNull();
  }

  protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
    if (emptyIsNull && StringUtils.isBlank(value)) {
      return null;
    }

    String format = attributes.get("dateTimeFormat");
    if (format == null) {
      throw new ConverterStateException("You must provide the dateTimeFormat dynamic attribute for " +
        "the form fields [" + expression + "] that maps to DateTime properties in the action. " +
        "If you are using a text field it will look like this: [@jc.text _dateTimeFormat=\"MM/dd/yyyy\"]");
    }

    return toDateTime(value, format);
  }

  protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
    throw new UnsupportedOperationException("You are attempting to map a form field that contains " +
      "multiple parameters to a property on the action class that is of type DateTime. This isn't " +
      "allowed.");
  }

  protected String objectToString(Object value, Type convertFrom, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
    String format = attributes.get("dateTimeFormat");
    if (format == null) {
      throw new ConverterStateException("You must provide the dateTimeFormat dynamic attribute for " +
        "the form fields [" + expression + "] that maps to DateTime properties in the action. " +
        "If you are using a text field it will look like this: [@jc.text _dateTimeFormat=\"MM/dd/yyyy\"]");
    }

    return DateTimeFormat.forPattern(format).print((ReadableInstant) value);
  }

  private DateTime toDateTime(String value, String format) {
    try {
      return DateTimeFormat.forPattern(format).withOffsetParsed().parseDateTime(value);
    } catch (IllegalArgumentException e) {
      throw new ConversionException("Invalid date [" + value + "] for format [" + format + "]", e);
    }
  }
}
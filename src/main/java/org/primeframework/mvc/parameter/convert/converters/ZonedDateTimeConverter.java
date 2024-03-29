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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
 * This converts to and from ZonedDateTime.
 *
 * @author Brian Pontarelli
 */
@GlobalConverter
public class ZonedDateTimeConverter extends AbstractGlobalConverter {
  private final boolean emptyIsNull;

  @Inject
  public ZonedDateTimeConverter(MVCConfiguration configuration) {
    this.emptyIsNull = configuration.emptyParametersAreNull();
  }

  protected String objectToString(Object value, Type convertFrom, Map<String, String> attributes, String expression)
      throws ConversionException, ConverterStateException {
    DateTimeFormatter formatter;
    String format = attributes.get("dateTimeFormat");
    if (format != null) {
      // Multiple formats are supported using a bracket syntax [M/dd/yyyy][MM/dd/yyyy]
      // Use the first pattern if multiple exists for displaying the value
      formatter = format.indexOf('[') == 0
          ? DateTimeFormatter.ofPattern(format.substring(1, format.indexOf("]", 1)))
          : DateTimeFormatter.ofPattern(format);
    } else {
      // Using the HTML standard (https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/datetime-local)
      formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    return ((ZonedDateTime) value).format(formatter);
  }

  protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
      throws ConversionException, ConverterStateException {
    if (emptyIsNull && StringUtils.isBlank(value)) {
      return null;
    }

    DateTimeFormatter formatter;
    String format = attributes.get("dateTimeFormat");
    if (format != null) {
      formatter = DateTimeFormatter.ofPattern(format);
    } else {
      // Try checking if this is an instant
      try {
        long instant = Long.parseLong(value);
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(instant), ZoneOffset.UTC);
      } catch (NumberFormatException e) {
        // Using the HTML standard (https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/datetime-local)
        formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
      }
    }

    return toDateTime(value, formatter);
  }

  protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> attributes, String expression)
      throws ConversionException, ConverterStateException {
    throw new MultipleParametersUnsupportedException("You are attempting to map a form field that contains " +
        "multiple parameters to a property on the action class that is of type DateTime. This isn't " +
        "allowed.");
  }

  private ZonedDateTime toDateTime(String value, DateTimeFormatter formatter) {
    try {
      return ZonedDateTime.parse(value, formatter);
    } catch (DateTimeParseException e) {
      throw new ConversionException("Invalid date [" + value + "] for format [" + formatter + "]", e);
    }
  }
}

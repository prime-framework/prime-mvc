/*
 * Copyright (c) 2001-2018, Inversoft Inc., All Rights Reserved
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.parameter.convert.AbstractGlobalConverter;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.annotation.GlobalConverter;

import com.google.inject.Inject;

/**
 * This converts to and from LocalDate.
 *
 * @author Brian Pontarelli
 */
@GlobalConverter
public class LocalDateConverter extends AbstractGlobalConverter {
  private final boolean emptyIsNull;

  @Inject
  public LocalDateConverter(MVCConfiguration configuration) {
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
          "the form fields [" + expression + "] that maps to LocalDate properties in the action. " +
          "If you are using a text field it will look like this: [@jc.text _dateTimeFormat=\"MM/dd/yyyy\"]");
    }

    return toLocalDate(value, format);
  }

  protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> attributes, String expression)
      throws ConversionException, ConverterStateException {
    throw new UnsupportedOperationException("You are attempting to map a form field that contains " +
        "multiple parameters to a property on the action class that is of type LocalDate. This isn't " +
        "allowed.");
  }

  protected String objectToString(Object value, Type convertFrom, Map<String, String> attributes, String expression)
      throws ConversionException, ConverterStateException {
    String format = attributes.get("dateTimeFormat");
    if (format == null) {
      throw new ConverterStateException("You must provide the dateTimeFormat dynamic attribute for " +
          "the form fields [" + expression + "] that maps to LocalDate properties in the action. " +
          "If you are using a text field it will look like this: [@jc.text _dateTimeFormat=\"MM/dd/yyyy\"]");
    }

    // Multiple formats are supported using a bracket syntax [M/dd/yyyy][MM/dd/yyyy]
    // Use the first pattern if multiple exists for displaying the value
    DateTimeFormatter formatter = format.indexOf("[") == 0
        ? DateTimeFormatter.ofPattern(format.substring(1, format.indexOf("]", 1)))
        : DateTimeFormatter.ofPattern(format);

    return ((LocalDate) value).format(formatter);
  }

  private LocalDate toLocalDate(String value, String format) {
    try {
      return LocalDate.parse(value, DateTimeFormatter.ofPattern(format));
    } catch (DateTimeParseException e) {
      throw new ConversionException("Invalid date [" + value + "] for format [" + format + "]", e);
    }
  }
}
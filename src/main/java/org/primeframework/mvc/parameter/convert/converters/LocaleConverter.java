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
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.primeframework.mvc.parameter.convert.AbstractGlobalConverter;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.annotation.GlobalConverter;

/**
 * This converts to and from Locales.
 *
 * @author Brian Pontarelli
 */
@GlobalConverter
public class LocaleConverter extends AbstractGlobalConverter {
  // Mostly copied from jackson-databind version 2.13.0

  protected final static String LOCALE_EXT_MARKER = "_#";

  protected int _firstHyphenOrUnderscore(String str) {
    for (int i = 0, end = str.length(); i < end; ++i) {
      char c = str.charAt(i);
      if (c == '_' || c == '-') {
        return i;
      }
    }
    return -1;
  }

  protected String objectToString(Object value, Type convertFrom, Map<String, String> attributes, String expression)
      throws org.primeframework.mvc.parameter.convert.ConversionException, ConverterStateException {
    return value.toString();
  }

  protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
      throws ConversionException, ConverterStateException {
    if (value == null) {
      return null;
    }

    if (StringUtils.isBlank(value)) {
      return Locale.ROOT;
    }

    try {
      return _deserializeLocale(value);
    } catch (Exception e) {
      throw new ConversionException("Invalid locale [" + value + "]", e);
    }
  }

  protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> attributes, String expression)
      throws org.primeframework.mvc.parameter.convert.ConversionException, ConverterStateException {
    return toLocale(values);
  }

  private Locale _deSerializeBCP47Locale(String value, int ix, String first, String second,
                                         int extMarkerIx) {
    String third = "";
    try {
      // Below condition checks if variant value is present to handle empty variant values such as
      // en__#Latn_x-ext
      // _US_#Latn
      if (extMarkerIx > 0 && extMarkerIx > ix) {
        third = value.substring(ix + 1, extMarkerIx);
      }
      value = value.substring(extMarkerIx + 2);

      if (value.indexOf('_') < 0 && value.indexOf('-') < 0) {
        return new Locale.Builder().setLanguage(first)
                                   .setRegion(second).setVariant(third).setScript(value).build();
      }
      if (value.indexOf('_') < 0) {
        ix = value.indexOf('-');
        return new Locale.Builder().setLanguage(first)
                                   .setRegion(second).setVariant(third)
                                   .setExtension(value.charAt(0), value.substring(ix + 1))
                                   .build();
      }
      ix = value.indexOf('_');
      return new Locale.Builder().setLanguage(first)
                                 .setRegion(second).setVariant(third)
                                 .setScript(value.substring(0, ix))
                                 .setExtension(value.charAt(ix + 1), value.substring(ix + 3))
                                 .build();
    } catch (IllformedLocaleException ex) {
      // should we really just swallow the exception?
      return new Locale(first, second, third);
    }
  }

  private Locale _deserializeLocale(String value) {
    int ix = _firstHyphenOrUnderscore(value);
    if (ix < 0) { // single argument
      return new Locale(value);
    }
    String first = value.substring(0, ix);
    value = value.substring(ix + 1);
    ix = _firstHyphenOrUnderscore(value);
    if (ix < 0) { // two pieces
      return new Locale(first, value);
    }
    String second = value.substring(0, ix);
    // [databind#3259]: Support for BCP 47 java.util.Locale ser/deser
    int extMarkerIx = value.indexOf(LOCALE_EXT_MARKER);
    if (extMarkerIx < 0) {
      return new Locale(first, second, value.substring(ix + 1));
    }
    return _deSerializeBCP47Locale(value, ix, first, second, extMarkerIx);
  }

  private Locale toLocale(String[] parts) {
    if (parts.length == 1) {
      try {
        return _deserializeLocale(parts[0]);
      } catch (Exception e) {
        throw new ConversionException("Invalid locale [" + parts[0] + "]", e);
      }
    } else if (parts.length == 2) {
      return new Locale(parts[0], parts[1]);
    }

    return new Locale(parts[0], parts[1], parts[2]);
  }
}
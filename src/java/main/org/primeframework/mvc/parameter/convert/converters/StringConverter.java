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
import org.primeframework.mvc.config.PrimeMVCConfiguration;
import org.primeframework.mvc.parameter.convert.AbstractGlobalConverter;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.annotation.GlobalConverter;

import com.google.inject.Inject;

/**
 * This converts to and from Strings.
 *
 * @author Brian Pontarelli
 */
@GlobalConverter(forTypes = {String.class})
public class StringConverter extends AbstractGlobalConverter {
  private final boolean emptyIsNull;

  @Inject
  public StringConverter(PrimeMVCConfiguration configuration) {
    this.emptyIsNull = configuration.emptyParametersAreNull();
  }

  protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
    throws org.primeframework.mvc.parameter.convert.ConversionException, ConverterStateException {
    if (emptyIsNull && StringUtils.isBlank(value)) {
      return null;
    }

    return value;
  }

  protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> attributes, String expression)
    throws org.primeframework.mvc.parameter.convert.ConversionException, ConverterStateException {
    return StringUtils.join(values, ",");
  }

  protected String objectToString(Object value, Type convertFrom, Map<String, String> attributes, String expression)
    throws org.primeframework.mvc.parameter.convert.ConversionException, ConverterStateException {
    return value.toString();
  }
}
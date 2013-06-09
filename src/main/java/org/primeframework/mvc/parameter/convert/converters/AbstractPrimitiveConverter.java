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
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.parameter.convert.AbstractGlobalConverter;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.util.TypeTools;

/**
 * Overrides the abstract type converter to add abstract methods for handling primitives.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public abstract class AbstractPrimitiveConverter extends AbstractGlobalConverter {
  private final boolean emptyIsNull;

  protected AbstractPrimitiveConverter(MVCConfiguration configuration) {
    this.emptyIsNull = configuration.emptyParametersAreNull();
  }

  protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
    if (emptyIsNull && StringUtils.isBlank(value)) {
      value = null;
    }

    Class<?> rawType = TypeTools.rawType(convertTo);
    if (value == null && rawType.isPrimitive()) {
      return defaultPrimitive(rawType, attributes);
    } else if (value == null) {
      return null;
    }

    return stringToPrimitive(value, rawType, attributes);
  }

  protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
    throw new ConverterStateException("The primitive converter doesn't support String[] to Object conversion.");
  }

  protected String objectToString(Object value, Type convertFrom, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
    Class<?> rawType = TypeTools.rawType(convertFrom);
    return primitiveToString(value, rawType, attributes);
  }

  /**
   * Returns the default primitive value for the given primitive type. This must use the wrapper classes as return
   * types.
   *
   * @param convertTo The type of primitive to return the default value for.
   * @param attributes Any attributes associated with the parameter being converted. Parameter attributes are described
   *                   in the {@link org.primeframework.mvc.parameter.ParameterWorkflow} class comment.
   * @return The wrapper that contains the default value for the primitive.
   * @throws ConversionException If the default value could not be determined.
   * @throws ConverterStateException If the state of the request, response, locale or attributes was such that
   *                                 conversion could not occur. This is normally a fatal exception that is fixable
   *                                 during development but not in production.
   */
  protected abstract Object defaultPrimitive(Class convertTo, Map<String, String> attributes)
    throws ConversionException, ConverterStateException;

  /**
   * Converts the given String (always non-null) to a primitive denoted by the convertTo parameter.
   *
   * @param value      The value to convert.
   * @param convertTo  The type to convert the value to.
   * @param attributes Any attributes associated with the parameter being converted. Parameter attributes are described
   *                   in the {@link org.primeframework.mvc.parameter.ParameterWorkflow} class comment.
   * @return The converted value.
   * @throws ConversionException     If there was a problem converting the given value to the given type.
   * @throws ConverterStateException If the state of the request, response, locale or attributes was such that
   *                                 conversion could not occur. This is normally a fatal exception that is fixable
   *                                 during development but not in production.
   */
  protected abstract Object stringToPrimitive(String value, Class convertTo, Map<String, String> attributes)
    throws ConversionException, ConverterStateException;

  /**
   * Converts the given String (always non-null) to a primitive denoted by the convertTo parameter.
   *
   * @param value       The Object value to convert.
   * @param convertFrom The type to convert the value from.
   * @param attributes  Any attributes associated with the parameter being converted. Parameter attributes are described
   *                    in the {@link org.primeframework.mvc.parameter.ParameterWorkflow} class comment.
   * @return The converted value.
   * @throws ConversionException     If there was a problem converting the given value to the given type.
   * @throws ConverterStateException If the state of the request, response, locale or attributes was such that
   *                                 conversion could not occur. This is normally a fatal exception that is fixable
   *                                 during development but not in production.
   */
  protected abstract String primitiveToString(Object value, Class convertFrom, Map<String, String> attributes)
    throws ConversionException, ConverterStateException;
}
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.primeframework.mvc.parameter.convert.AbstractGlobalConverter;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.parameter.el.TypeTools;

import com.google.inject.Inject;
import static java.util.Arrays.*;

/**
 * This converts to and from Collection types. This handles complex parameterized types by first creating the
 * Collection instance (ArrayList, HashSet, LinkedList, etc) and then by leveraging the other converters for the
 * parametrized type.
 *
 * @author Brian Pontarelli
 */
@org.primeframework.mvc.parameter.convert.annotation.GlobalConverter(forTypes = {Collection.class})
@SuppressWarnings("unchecked")
public class CollectionConverter extends AbstractGlobalConverter {
  private final ConverterProvider provider;

  @Inject
  public CollectionConverter(ConverterProvider provider) {
    this.provider = provider;
  }

  /**
   * Creates the correct collection type and then converts the value String to the parameterized type (if one exists)
   * and then adds it. If there is no parameterized type, this just shoves the String value in the collection.
   *
   * @param value             The value.
   * @param convertTo         The type to convert to.
   * @param dynamicAttributes The dynamic attributes used to assist in conversion.
   * @param expression        The full path to the expression that is causing the conversion.
   * @return The converted value.
   * @throws ConversionException     If the conversion failed.
   * @throws ConverterStateException if the converter didn't have all of the information it needed to perform the
   *                                 conversion.
   */
  protected Object stringToObject(String value, Type convertTo, Map<String, String> dynamicAttributes,
                                  String expression)
    throws ConversionException, ConverterStateException {
    return stringsToObject(StringUtils.split(value, ','), convertTo, dynamicAttributes, expression);
  }

  /**
   * Creates the correct collection type and then converts the value Strings to the parametrized type (if one exists)
   * and then adds each one. If there is no parameterized type, this just shoves the String values in the collection.
   *
   * @param values            The values.
   * @param convertTo         The type to convert to.
   * @param dynamicAttributes The dynamic attributes used to assist in conversion.
   * @param expression        The full path to the expression that is causing the conversion.
   * @return The converted value.
   * @throws ConversionException     If the conversion failed.
   * @throws ConverterStateException if the converter didn't have all of the information it needed to perform the
   *                                 conversion.
   */
  protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> dynamicAttributes,
                                   String expression)
    throws ConversionException, ConverterStateException {
    Class<?> rawType = TypeTools.rawType(convertTo);
    Class<?> parameter = parameterType(convertTo);
    Collection collection = makeCollection(rawType);
    if (parameter == null) {
      collection.addAll(asList(values));
    } else {
      GlobalConverter converter = provider.lookup(parameter);
      if (converter == null) {
        throw new ConverterStateException("Unable to convert to the type [" + convertTo +
          "] because the parameter type [" + parameter + "] doesn't have a Converter " +
          "associated with it.");
      }

      for (String value : values) {
        collection.add(converter.convertFromStrings(parameter, dynamicAttributes, expression, StringUtils.split(value, ',')));
      }
    }

    return collection;
  }

  /**
   * Iterates over the given collection and converts each collection item to a String based on the parameterized type.
   * If there is no parameterized type, this just joins the collection.
   *
   * @param value             The value.
   * @param convertFrom       The type to convert from.
   * @param dynamicAttributes The dynamic attributes used to assist in conversion.
   * @param expression        The full path to the expression that is causing the conversion.
   * @return The converted value.
   * @throws ConversionException     If the conversion failed.
   * @throws ConverterStateException if the converter didn't have all of the information it needed to perform the
   *                                 conversion.
   */
  protected String objectToString(Object value, Type convertFrom, Map<String, String> dynamicAttributes,
                                  String expression)
    throws ConversionException, ConverterStateException {
    Collection collection = (Collection) value;
    Class<?> parameter = parameterType(convertFrom);
    if (parameter == null) {
      return StringUtils.join(collection, ",");
    } else {
      GlobalConverter converter = provider.lookup(parameter);
      if (converter == null) {
        throw new ConverterStateException("Unable to convert to the type [" + convertFrom +
          "] because the parameter type [" + parameter + "] doesn't have a Converter " +
          "associated with it.");
      }

      StringBuilder build = new StringBuilder();
      for (Object o : collection) {
        String str = converter.convertToString(parameter, dynamicAttributes, expression, o);
        if (build.length() > 0) {
          build.append(",");
        }
        build.append(str);
      }

      return build.toString();
    }
  }

  /**
   * Creates a new instance of the given collection type. If this type is a collection interface, the most common
   * implementation is created. Otherwise, the type is instantiated directly. If that fails, this conversion will fail.
   *
   * @param type The type of collection.
   * @return The new collection.
   */
  protected Collection makeCollection(Class<?> type) {
    if (type == List.class) {
      return new ArrayList();
    } else if (type == SortedSet.class) {
      return new TreeSet();
    } else if (type == Set.class) {
      return new HashSet();
    } else if (type == Queue.class) {
      return new LinkedList();
    }

    // Try to instantiate it directly
    try {
      return (Collection) type.newInstance();
    } catch (Exception e) {
      throw new ConverterStateException("The type [" + type + "] is a collection but could not " +
        "be instantiated by the converter class");
    }
  }
}

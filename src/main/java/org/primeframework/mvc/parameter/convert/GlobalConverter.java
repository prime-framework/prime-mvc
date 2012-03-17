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
package org.primeframework.mvc.parameter.convert;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Converters are used to convert objects from one type to another, convert strings to objects and to convert from
 * objects and strings to arrays of objects. The way that this is setup is that a Converter implementation is
 * registered with the ConverterRegistry. The registry can then be queried for a particular Conveter (see {@link
 * ConverterProvider} for more information about retrieval). Next, the Converter can be used for conversions using one
 * of the methods described below. Any given Converter may be used to convert to many different types because of the way
 * that the ConverterRegistry searches for Converters. Because of this flexibility when converting the Converter must
 * be told what type to convert to. This is the reason for the second Class parameter on all of the convert methods.
 * <p/>
 * The type that appears on all of the methods in this interface can be any type that is support by the Converter, but
 * generally is a type that is the type or a sub-type of the type the converter is registered for. For example, let's
 * say the converter is registered to java.lang.Number, then this converter should be able to convert Integer, Long,
 * Double, etc. unless a converter is registered for each one of these. So, we could call the converter with ("0.5",
 * Double.class) in order to convert the String to a Double object.
 * <p/>
 * <h3>Arrays</h3>
 * <p/>
 * Arrays pose a significant issue when considering null values and multi-dimensional arrays as well as communicative
 * conversion (i.e. a -> b -> a). Because of this, implementations are free to NOT support multi-dimensional arrays,
 * null values or communicative conversions. When supporting these concepts however implementations must take care to
 * use array delimiters and null indicators. The recommended versions of these are:
 * <p/>
 * <table>
 * <tr><th>Name</th><th>Recommendation</th></tr>
 * <tr><td>Array open</td><td>{</td></tr>
 * <tr><td>Array close</td><td>}</td></tr>
 * <tr><td>Array separator</td><td>,</td></tr>
 * <tr><td>Null indicator</td><td>__#null#__</td></tr>
 * </table>
 * <p/>
 * We highly recommend against crazy array handling and just focus on simple String/Object conversions. However, single
 * dimensional arrays should almost always be handled correctly. You only need to support String to array conversions.
 * Array to String conversions are rarely necessary.
 * <p/>
 * <h3>Null values</h3>
 * <p/>
 * When peforming any conversion of a single value that is null, it is recommended that implementations return null.
 * <p/>
 * <h3>Primitives</h3>
 * <p/>
 * There are already pre-written conversion for primitives, but when overriding these, implementations must ensure that
 * during conversions of nulls, that the default value of the primitive is returned.
 * <p/>
 * <h3>Custom converters</h3>
 * <p/>
 * In order to write a custom type converter, you need to implement this interface AND also annotate your converter
 * class with the {@link org.primeframework.mvc.parameter.convert.annotation.GlobalConverter} annotation and specify in
 * that annotation the types the Converter supports. Next, you need to define the Converter in a Guice module that will
 * be discovered by Prime like this:
 * <p/>
 * <pre>
 * public void bind() {
 *   bind(MyCustomerConverter.class);
 * }
 * </pre>
 *
 * @author Brian Pontarelli
 */
public interface GlobalConverter {
  /**
   * Converts the given object to the given type.
   * <p/>
   * This method should handle all variations of String[] conversion. The values incoming are pulled directly from the
   * HTTP request parameters so there might be one or many. Here are some of the possible variations that should be
   * handled.
   * <p/>
   * <ul>
   * <li>Single String to Object</li>
   * <li>String array to Object (very uncommon)</li>
   * <li>Single String to array</li>
   * <li>Single String to multi-dimensional array</li>
   * <li>String array to array</li>
   * <li>String array to multi-dimensional array</li>
   * </ul>
   *
   * @param convertTo  The type to convert the value to. This might be a Class or it might be a parameterized type such
   *                   as List&lt;String>.
   * @param attributes Any attributes associated with the parameter being converted. Parameter attributes are described
   *                   in the {@link org.primeframework.mvc.parameter.ParameterWorkflow} class comment.
   * @param expression The full path to the expression that is causing the conversion.
   * @param values     The value(s) to convert. This might be a single value or multiple values.
   * @return The converted value.
   * @throws ConversionException     If there was a problem converting the given value to the given type.
   * @throws ConverterStateException If the state of the request, response, locale or attributes was such that
   *                                 conversion could not occur. This is normally a fatal exception that is fixable
   *                                 during development but not in production.
   */
  Object convertFromStrings(Type convertTo, Map<String, String> attributes, String expression, String... values)
    throws ConversionException, ConverterStateException;

  /**
   * Converts the given Object to a String for display purposes.
   * <p/>
   * This method should handle all variations of Object to String conversion. The value is always pulled from the object
   * in the action and is converted to a single String to display.
   * <p/>
   * <ul>
   * <li>Object to String</li>
   * <li>Array to String</li>
   * <li>Multi-dimensional array to String</li>
   * </ul>
   *
   * @param convertFrom The type to convert the value from. This might be a Class or it might be a parameterized type
   *                    such as List&lt;String>.
   * @param attributes  Any attributes associated with the parameter being converted. Parameter attributes are
   *                    described
   *                    in the {@link org.primeframework.mvc.parameter.ParameterWorkflow} class comment.
   * @param expression  The expression.
   * @param value       The Object value to convert.
   * @return The converted value.
   * @throws ConversionException     If there was a problem converting the given value to the given type.
   * @throws ConverterStateException If the state of the request, response, locale or attributes was such that
   *                                 conversion could not occur. This is normally a fatal exception that is fixable
   *                                 during development but not in production.
   */
  String convertToString(Type convertFrom, Map<String, String> attributes, String expression, Object value)
    throws ConversionException;
}

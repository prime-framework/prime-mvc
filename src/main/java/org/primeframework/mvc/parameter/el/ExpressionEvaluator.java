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
package org.primeframework.mvc.parameter.el;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;

/**
 * This interface defines the mechanism by which expressions are evaluated in order to get values from Objects and set
 * values from the HTTP request parameters into Objects.
 *
 * @author Brian Pontarelli
 */
public interface ExpressionEvaluator {
  /**
   * Retrieves a value defined by the given expression from the given object. No type conversion is performed and you
   * can get ClassCastExceptions because of the type erasure of the generics.
   *
   * @param expression The expression that defines the value to get from the object.
   * @param object     The object to get the value from.
   * @return The value from the object.
   * @throws ExpressionException If the expression is invalid or there was an error during processing.
   */
  <T> T getValue(String expression, Object object) throws ExpressionException;

  /**
   * Retrieves a value defined by the given expression from the given object and converts that value into a String using
   * the appropriate {@link org.primeframework.mvc.parameter.convert.GlobalConverter}.
   *
   * @param expression The expression that defines the value to get from the object.
   * @param object     The object to get the value from.
   * @param attributes The attributes for the expression. These attributes are only available if the user submitted a
   *                   form and a particular parameter had some attributes associated with it. Those attributes are
   *                   stored in the HttpServletRequest and can be fetched via the ParameterWorkflow. These are passed
   *                   to the Converter to assist in conversion from objects to Strings.
   * @return The String value from the object. This value has been converted.
   * @throws ExpressionException If the expression is invalid or there was an error during processing.
   */
  String getValue(String expression, Object object, Map<String, String> attributes) throws ExpressionException;

  /**
   * Sets the given value into the given object using the given expression.
   *
   * @param expression The expression.
   * @param object     The object.
   * @param value      The value to set into the object.
   * @throws ExpressionException If the expression is invalid or there was an error during processing.
   */
  void setValue(String expression, Object object, Object value) throws ExpressionException;

  /**
   * Sets the given values into the given object using the given expression. The values given are taken directly from
   * the HttpServletRequest parameters. This performs any necessary conversions from the String[] values to the type
   * required by the expression and object given. Conversions are done by the appropriate {@link
   * org.primeframework.mvc.parameter.convert.GlobalConverter}.
   *
   * @param expression The expression.
   * @param object     The object.
   * @param values     The HttpServletRequest parameter values.
   * @param attributes The attributes for the expression. These attributes are only available if the user submitted a
   *                   form and a particular parameter had some attributes associated with it. Those attributes are
   *                   stored in the HttpServletRequest and can be fetched via the ParameterWorkflow. These are passed
   *                   to the Converter to assist in conversion from objects to Strings.
   * @throws ConversionException     If the conversion failed.
   * @throws ConverterStateException If there isn't a converter or the converter could not run because it was missing a
   *                                 required attribute.
   * @throws ExpressionException     If the expression is invalid or there was an error during processing.
   */
  void setValue(String expression, Object object, String[] values, Map<String, String> attributes)
    throws ConversionException, ConverterStateException, ExpressionException;

  /**
   * Expands variables in the given String. The variables must be in the form <code>${foo}</code>.
   *
   * @param str    The String to expand.
   * @param object The Root object where the variables are expanded from.
   * @param encode Whether or not values are URL encoded.
   * @return The expanded String and never null.
   * @throws ExpressionException If the expansion failed.
   */
  String expand(String str, Object object, boolean encode) throws ExpressionException;

  /**
   * Retrieves all of the names of the members from the given class that can be accessed by the expression evaluator.
   *
   * @param type The class to retrieve the members from.
   * @return The list of member names.
   */
  Set<String> getAllMembers(Class<?> type);

  /**
   * Retrieves all of the values of the members from the given Object that can be accessed by the expression evaluator.
   *
   * @param obj The Object to retrieve the values from.
   * @return The list of member values.
   */
  Collection<Object> getAllMemberValues(Object obj);

  /**
   * Returns the annotation on the given expression end-point. The end point might be a method or a field.
   *
   * @param type       The type of the annotation.
   * @param expression The expression.
   * @param action     The action.
   * @return The annotation or null.
   */
  <T extends Annotation> T getAnnotation(Class<T> type, String expression, Object action);
}
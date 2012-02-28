/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.mvc.parameter.convert;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import org.jcatapult.mvc.parameter.el.TypeTools;

import net.java.lang.StringTools;

/**
 * <p>
 * This class is the base type converter for all the type converters
 * that handle Object types. If you are writing a converter for
 * primitive types, use the {@link org.jcatapult.mvc.parameter.convert.converters.AbstractPrimitiveConverter}
 * class.
 * </p>
 *
 * <p>
 * This class mostly delegates between the various method calls by
 * passing in default values or performing casting. Each method
 * describes how it functions.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public abstract class AbstractGlobalConverter implements GlobalConverter {
    /**
     * Handles the following cases:
     *
     * <ul>
     * <li>Null - returns null</li>
     * <li>Type is an array - calls stringToArray</li>
     * <li>Type is not an array and values length is 1 or 0 - calls stringToObject</li>
     * <li>Type is an array and values length is > 1 - calls stringsToArray</li>
     * <li>Type is not an array and values length is > 1 - calls stringsToObject</li>
     * </ul>
     *
     * @param   convertTo The type to convert to.
     * @param   dynamicAttributes The dynamic attributes used to assist in conversion.
     * @param   expression The full path to the expression that is causing the conversion.
     * @param   values The values to convert.
     * @return  The converted value.
     * @throws  ConversionException If the conversion failed.
     * @throws  ConverterStateException if the converter didn't have all of the information it needed
     *          to perform the conversion.
     */
    public Object convertFromStrings(Type convertTo, Map<String, String> dynamicAttributes, String expression, String... values)
    throws ConversionException, ConverterStateException {
        // Handle a zero or one String
        Class<?> rawType = TypeTools.rawType(convertTo);
        if (values == null || values.length <= 1) {
            String value = (values != null && values.length == 1) ? values[0] : null;

            if (rawType.isArray()) {
                // Punt on multi-dimensional arrays
                if (rawType.getComponentType().isArray()) {
                    throw new ConverterStateException("Converter [" + getClass() + "] does not support" +
                        " conversion to multi-dimensional arrays of type [" + convertTo + "]");
                }

                return stringToArray(value, convertTo, dynamicAttributes, expression);
            }

            return stringToObject(value, convertTo, dynamicAttributes, expression);
        }

        // Handle multiple strings
        if (rawType.isArray()) {
            // Punt on multi-dimensional arrays
            if (rawType.getComponentType().isArray()) {
                throw new ConverterStateException("Converter [" + getClass() + "] does not support" +
                    " conversion to multi-dimensional arrays of type [" + convertTo + "]");
            }

            return stringsToArray(values, convertTo, dynamicAttributes, expression);
        }

        return stringsToObject(values, convertTo, dynamicAttributes, expression);
    }

    /**
     * Gets the first parameter type is the given type is a parameterized type. If it isn't, this
     * returns null. If the type has multiple parameters, only the first is returned.
     *
     * @param   type The type.
     * @return  The first parameter type.
     */
    protected Class<?> parameterType(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        return null;
    }

    /**
     * Converts the value to a String.
     *
     * @param   convertFrom The original Type of the value.
     * @param   dynamicAttributes The dynamic attributes used to assist in conversion.
     * @param   expression The full path to the expression that is causing the conversion.
     * @param   value The value to convert.
     * @return  The converted value.
     * @throws  ConversionException If the conversion failed.
     * @throws  ConverterStateException if the converter didn't have all of the information it needed
     *          to perform the conversion.
     */
    public String convertToString(Type convertFrom, Map<String, String> dynamicAttributes, String expression, Object value)
    throws ConversionException {
        // Handle null
        if (value == null) {
            return null;
        }

        // Check simple conversion
        if (value instanceof String) {
            return (String) value;
        }

        // Handle arrays
        Class<?> rawType = TypeTools.rawType(convertFrom);
        if (rawType.isArray()) {
            return arrayToString(value, convertFrom, dynamicAttributes, expression);
        }

        return objectToString(value, convertFrom, dynamicAttributes, expression);
    }

    /**
     * This performs the conversion from a single String value to an array of the given type.
     *
     * @param   value The value to convert to an array.
     * @param   convertTo The array type to convert to.
     * @param   dynamicAttributes The dynamic attributes used to assist in conversion.
     * @param   expression The full path to the expression that is causing the conversion.
     * @return  The converted value.
     * @throws  ConversionException If the conversion failed.
     * @throws  ConverterStateException if the converter didn't have all of the information it needed
     *          to perform the conversion.
     */
    protected Object stringToArray(String value, Type convertTo, Map<String, String> dynamicAttributes, String expression)
    throws ConversionException {
        if (value == null) {
            return null;
        }

        Object finalArray;
        Class<?> rawType = TypeTools.rawType(convertTo);
        if (StringTools.isTrimmedEmpty(value)) {
            finalArray = Array.newInstance(rawType.getComponentType(), 0);
        } else {
            String[] parts = value.split(",");
            finalArray = Array.newInstance(rawType.getComponentType(), parts.length);
            for (int i = 0; i < parts.length; i++) {
                Object singleValue = stringToObject(parts[i], rawType.getComponentType(), dynamicAttributes, expression);
                Array.set(finalArray, i, singleValue);
            }
        }

        return finalArray;
    }

    /**
     * This performs the conversion from an array of String values to an array of the given type.
     *
     * @param   values The values to convert to an array.
     * @param   convertTo The array type to convert to.
     * @param   dynamicAttributes The dynamic attributes to assist in the conversion.
     * @param   expression The full path to the expression that is causing the conversion.
     * @return  The converted value.
     * @throws  ConversionException If the conversion failed.
     * @throws  ConverterStateException if the converter didn't have all of the information it needed
     *          to perform the conversion.
     */
    protected Object stringsToArray(String[] values, Type convertTo, Map<String, String> dynamicAttributes, String expression)
    throws ConversionException {
        if (values == null) {
            return null;
        }

        Object finalArray;
        Class<?> rawType = TypeTools.rawType(convertTo);
        if (values.length == 0) {
            finalArray = Array.newInstance(rawType.getComponentType(), 0);
        } else {
            finalArray = Array.newInstance(rawType.getComponentType(), values.length);
            for (int i = 0; i < values.length; i++) {
                Object singleValue = stringToObject(values[i], rawType.getComponentType(), dynamicAttributes, expression);
                Array.set(finalArray, i, singleValue);
            }
        }

        return finalArray;
    }

    /**
     * This performs the conversion from an array to a single String value.
     *
     * @param   value The array value to convert to a String.
     * @param   convertFrom The array type to convert from.
     * @param   dynamicAttributes The dynamic attributes to assist in the conversion.
     * @param   expression The full path to the expression that is causing the conversion.
     * @return  The converted value.
     * @throws  ConversionException If the conversion failed.
     * @throws  ConverterStateException if the converter didn't have all of the information it needed
     *          to perform the conversion.
     */
    protected String arrayToString(Object value, Type convertFrom, Map<String, String> dynamicAttributes, String expression)
    throws ConversionException {
        Class<?> rawType = TypeTools.rawType(convertFrom);
        if (!rawType.isArray()) {
            throw new ConversionException("The convertFrom parameter must be an array type");
        }

//        if (value == null) {
//            return null;
//        }

        if (!value.getClass().isArray()) {
            throw new ConversionException("The value is not an array");
        }

        if (value.getClass().getComponentType().isArray()) {
            throw new ConversionException("The value is a multi-dimensional array, which is not" +
                " supported by the AbstractConverter");
        }

        int length = Array.getLength(value);
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < length; i++) {
            Object o = Array.get(value, i);
            str.append(convertToString(value.getClass().getComponentType(), dynamicAttributes, expression, o));
            if (i + 1 < length) {
                str.append(",");
            }
        }

        return str.toString();
    }

    /**
     * Converts the single String value to an Object.
     *
     * @param   value The String value to convert.
     * @param   convertTo The type to convert to.
     * @param   dynamicAttributes The dynamic attributes to assist in the conversion.
     * @param   expression The full path to the expression that is causing the conversion.
     * @return  The converted value.
     * @throws  ConversionException If the conversion failed.
     * @throws  ConverterStateException if the converter didn't have all of the information it needed
     *          to perform the conversion.
     */
    protected abstract Object stringToObject(String value, Type convertTo, Map<String, String> dynamicAttributes, String expression)
    throws ConversionException, ConverterStateException;

    /**
     * Converts a String array to a single Object (not an array of Objects). Support for this method
     * is uncommon.
     *
     * @param   values The String values to convert.
     * @param   convertTo The type to convert to.
     * @param   dynamicAttributes The dynamic attributes to assist in the conversion.
     * @param   expression The full path to the expression that is causing the conversion.
     * @return  The converted value.
     * @throws  ConversionException If the conversion failed.
     * @throws  ConverterStateException if the converter didn't have all of the information it needed
     *          to perform the conversion.
     */
    protected abstract Object stringsToObject(String[] values, Type convertTo, Map<String, String> dynamicAttributes, String expression)
    throws ConversionException, ConverterStateException;

    /**
     * Converts the Object value to a String.
     *
     * @param   value The Object value to convert.
     * @param   convertFrom The type to convert from.
     * @param   dynamicAttributes The dynamic attributes to assist in the conversion.
     * @param   expression The full path to the expression that is causing the conversion.
     * @return  The converted value.
     * @throws  ConversionException If the conversion failed.
     * @throws  ConverterStateException if the converter didn't have all of the information it needed
     *          to perform the conversion.
     */
    protected abstract String objectToString(Object value, Type convertFrom, Map<String, String> dynamicAttributes, String expression)
    throws ConversionException, ConverterStateException;
}

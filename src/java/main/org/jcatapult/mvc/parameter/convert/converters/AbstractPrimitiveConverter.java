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
package org.jcatapult.mvc.parameter.convert.converters;

import java.lang.reflect.Type;
import java.util.Map;

import org.jcatapult.mvc.parameter.convert.ConversionException;
import org.jcatapult.mvc.parameter.convert.ConverterStateException;
import org.jcatapult.mvc.parameter.convert.AbstractGlobalConverter;
import org.jcatapult.mvc.parameter.el.TypeTools;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.java.lang.StringTools;

/**
 * <p>
 * Overrides the abstract type converter to add abstract methods
 * for handling primitives.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public abstract class AbstractPrimitiveConverter extends AbstractGlobalConverter {
    private boolean emptyIsNull = true;

    @Inject(optional = true)
    public void setEmptyStringIsNull(@Named("jcatapult.mvc.emptyStringIsNull") boolean emptyIsNull) {
        this.emptyIsNull = emptyIsNull;
    }

    protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
        if (emptyIsNull && StringTools.isTrimmedEmpty(value)) {
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
     * Returns the default primitive value for the given primitive type. This must use the wrapper
     * classes as return types.
     *
     * @param   convertTo The type of primitive to return the default value for.
     * @return  The wrapper that contains the default value for the primitive.
     * @throws  ConversionException If the default value could not be determined.
     */
    protected abstract Object defaultPrimitive(Class convertTo, Map<String, String> attributes)
    throws ConversionException, ConverterStateException;

    /**
     * Converts the given String (always non-null) to a primitive denoted by the convertTo parameter.
     *
     * @param   value The value to convert.
     * @param   convertTo The type to convert the value to.
     * @param   attributes Any attributes associated with the parameter being converted. Parameter
     *          attributes are described in the {@link org.jcatapult.mvc.parameter.ParameterWorkflow}
     *          class comment.
     * @return  The converted value.
     * @throws  ConversionException If there was a problem converting the given value to the
     *          given type.
     * @throws  ConverterStateException If the state of the request, response, locale or attributes
     *          was such that conversion could not occur. This is normally a fatal exception that is
     *          fixable during development but not in production.
     */
    protected abstract Object stringToPrimitive(String value, Class convertTo, Map<String, String> attributes)
    throws ConversionException, ConverterStateException;

    /**
     * Converts the given String (always non-null) to a primitive denoted by the convertTo parameter.
     *
     * @param   value The Object value to convert.
     * @param   convertFrom The type to convert the value from.
     * @param   attributes Any attributes associated with the parameter being converted. Parameter
     *          attributes are described in the {@link org.jcatapult.mvc.parameter.ParameterWorkflow}
     *          class comment.
     * @return  The converted value.
     * @throws  ConversionException If there was a problem converting the given value to the
     *          given type.
     * @throws  ConverterStateException If the state of the request, response, locale or attributes
     *          was such that conversion could not occur. This is normally a fatal exception that is
     *          fixable during development but not in production.
     */
    protected abstract String primitiveToString(Object value, Class convertFrom, Map<String, String> attributes)
    throws ConversionException, ConverterStateException;
}
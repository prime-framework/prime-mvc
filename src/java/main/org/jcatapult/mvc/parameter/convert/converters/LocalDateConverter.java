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

import org.jcatapult.mvc.parameter.convert.AbstractGlobalConverter;
import org.jcatapult.mvc.parameter.convert.ConversionException;
import org.jcatapult.mvc.parameter.convert.ConverterStateException;
import org.jcatapult.mvc.parameter.convert.annotation.GlobalConverter;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.java.lang.StringTools;

/**
 * <p>
 * This converts to and from LocalDate.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@GlobalConverter(forTypes = {LocalDate.class})
@SuppressWarnings("unchecked")
public class LocalDateConverter extends AbstractGlobalConverter {
    private boolean emptyIsNull = true;

    @Inject(optional = true)
    public void setEmptyStringIsNull(@Named("jcatapult.mvc.emptyStringIsNull") boolean emptyIsNull) {
        this.emptyIsNull = emptyIsNull;
    }

    protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
        if (emptyIsNull && StringTools.isTrimmedEmpty(value)) {
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

        return DateTimeFormat.forPattern(format).print((ReadablePartial) value);
    }

    private LocalDate toLocalDate(String value, String format) {
        try {
            return DateTimeFormat.forPattern(format).parseDateTime(value).toLocalDate();
        } catch (IllegalArgumentException e) {
            throw new ConversionException("Invalid date [" + value + "] for format [" + format + "]", e);
        }
    }
}
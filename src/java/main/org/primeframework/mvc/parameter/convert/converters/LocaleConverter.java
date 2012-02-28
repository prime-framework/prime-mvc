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
package org.primeframework.mvc.parameter.convert.converters;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;

import org.primeframework.mvc.parameter.convert.AbstractGlobalConverter;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.annotation.GlobalConverter;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.java.lang.StringTools;

/**
 * <p>
 * This converts to and from Locales.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@GlobalConverter(forTypes = {Locale.class})
@SuppressWarnings("unchecked")
public class LocaleConverter extends AbstractGlobalConverter {
    private boolean emptyIsNull = true;

    @Inject(optional = true)
    public void setEmptyStringIsNull(@Named("jcatapult.mvc.emptyStringIsNull") boolean emptyIsNull) {
        this.emptyIsNull = emptyIsNull;
    }

    protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
    throws org.primeframework.mvc.parameter.convert.ConversionException, ConverterStateException {
        if (emptyIsNull && StringTools.isTrimmedEmpty(value)) {
            return null;
        }

        String[] parts = value.split("_");
        return toLocale(parts);
    }

    protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> attributes, String expression)
    throws org.primeframework.mvc.parameter.convert.ConversionException, ConverterStateException {
        return toLocale(values);
    }

    protected String objectToString(Object value, Type convertFrom, Map<String, String> attributes, String expression)
    throws org.primeframework.mvc.parameter.convert.ConversionException, ConverterStateException {
        return value.toString();
    }

    private Locale toLocale(String[] parts) {
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        }

        return new Locale(parts[0], parts[1], parts[2]);
    }
}
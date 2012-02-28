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

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;

import org.jcatapult.mvc.parameter.convert.ConversionException;
import org.jcatapult.mvc.parameter.convert.ConverterStateException;
import org.jcatapult.mvc.parameter.convert.AbstractGlobalConverter;
import org.jcatapult.mvc.parameter.convert.annotation.GlobalConverter;

import static net.java.lang.ObjectTools.*;
import net.java.lang.StringTools;

/**
 * <p>
 * This class converts to and from the java.io.File class.
 * </p>
 *
 * @author Brian Pontarelli
 */
@GlobalConverter(forTypes = {File.class})
@SuppressWarnings("unchecked")
public class FileConverter extends AbstractGlobalConverter {
    /**
     * Returns null if the value is null, otherwise this returns a new File of the value.
     *
     * @param   attributes Can contain the parentDir attribute which if the String is relative will
     * @param expression
     */
    protected Object stringToObject(String value, Type convertTo, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
        if (StringTools.isTrimmedEmpty(value)) {
            return null;
        }

        if (value.charAt(0) == File.separatorChar || value.charAt(0) == '\\') {
            return new File(value);
        }

        if (attributes != null) {
            String parent = attributes.get("parentDir");
            if (parent != null) {
                return new File(parent, value);
            }
        }

        return new File(value);
    }

    /**
     * Joins the values and then sends the new joined String to the stringToObject method.
     */
    protected Object stringsToObject(String[] values, Type convertTo, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
        String joined = join(values, File.separator);
        return stringToObject(joined, convertTo, attributes, expression);
    }

    /**
     * Returns the absolute path of the file.
     */
    protected String objectToString(Object value, Type convertFrom, Map<String, String> attributes, String expression)
    throws ConversionException, ConverterStateException {
        File file = (File) value;
        return file.getAbsolutePath();
    }
}
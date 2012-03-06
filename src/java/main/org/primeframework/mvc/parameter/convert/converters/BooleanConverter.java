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

import java.util.Map;

import org.primeframework.mvc.config.PrimeMVCConfiguration;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.annotation.GlobalConverter;

import com.google.inject.Inject;

/**
 * This class is the type converter for booleans.
 *
 * @author Brian Pontarelli
 */
@GlobalConverter(forTypes = {Boolean.class, boolean.class})
public class BooleanConverter extends AbstractPrimitiveConverter {
  @Inject
  public BooleanConverter(PrimeMVCConfiguration configuration) {
    super(configuration);
  }

  /**
   * Returns false.
   */
  protected Object defaultPrimitive(Class convertTo, Map<String, String> attributes)
    throws ConversionException, ConverterStateException {
    return Boolean.FALSE;
  }

  /**
   * Uses Boolean.valueOf.
   */
  protected Object stringToPrimitive(String value, Class convertTo, Map<String, String> attributes)
    throws ConversionException, ConverterStateException {
    if (!value.equals("true") && !value.equals("false") && !value.equals("on") && !value.equals("off")
      && !value.equals("yes") && !value.equals("no")) {
      throw new ConversionException("Unable to convert invalid boolean String [" + value + "]");
    }

    return value.equals("true") || value.equals("on") || value.equals("yes");
  }

  /**
   * Returns value.toString().
   */
  protected String primitiveToString(Object value, Class convertFrom, Map<String, String> attributes)
    throws ConversionException, ConverterStateException {
    return value.toString();
  }
}
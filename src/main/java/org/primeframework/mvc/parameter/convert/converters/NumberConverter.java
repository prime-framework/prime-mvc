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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.annotation.GlobalConverter;

import com.google.inject.Inject;

/**
 * This is the type converter for primitives and wrapper classes of numbers.
 *
 * @author Brian Pontarelli
 */
@GlobalConverter
public class NumberConverter extends AbstractPrimitiveConverter {
  @Inject
  public NumberConverter(MVCConfiguration configuration) {
    super(configuration);
  }

  /**
   * Returns 0 for everything but in the correct wrapper classes.
   */
  protected Object defaultPrimitive(Class convertTo, Map<String, String> attributes)
    throws ConversionException, ConverterStateException {
    if (convertTo == Byte.TYPE || convertTo == Byte.class) {
      return (byte) 0;
    } else if (convertTo == Short.TYPE || convertTo == Short.class) {
      return (short) 0;
    } else if (convertTo == Integer.TYPE || convertTo == Integer.class) {
      return 0;
    } else if (convertTo == Long.TYPE || convertTo == Long.class) {
      return 0l;
    } else if (convertTo == Float.TYPE || convertTo == Float.class) {
      return 0.0f;
    } else if (convertTo == BigInteger.class) {
      return BigInteger.ZERO;
    } else if (convertTo == BigDecimal.class) {
      return new BigDecimal(0.0d);
    } else if (convertTo == Double.TYPE || convertTo == Double.class) {
      return 0.0d;
    }

    throw new ConverterStateException("Invalid type for NumberConverter [" + convertTo + "]");
  }

  /**
   * Uses the valueOf methods in the wrapper classes based on the convertTo type.
   */
  protected Object stringToPrimitive(String value, Class convertTo, Map<String, String> attributes)
    throws ConversionException, ConverterStateException {
    try {
      if (convertTo == Byte.TYPE || convertTo == Byte.class) {
        return Byte.valueOf(value);
      } else if (convertTo == Short.TYPE || convertTo == Short.class) {
        return Short.valueOf(value);
      } else if (convertTo == Integer.TYPE || convertTo == Integer.class) {
        return Integer.valueOf(value);
      } else if (convertTo == Long.TYPE || convertTo == Long.class) {
        return Long.valueOf(value);
      } else if (convertTo == Float.TYPE || convertTo == Float.class) {
        return Float.valueOf(value);
      } else if (convertTo == Double.TYPE || convertTo == Double.class) {
        return Double.valueOf(value);
      } else if (convertTo == BigInteger.class) {
        return new BigInteger(value);
      } else if (convertTo == BigDecimal.class) {
        return new BigDecimal(value);
      }

      throw new ConverterStateException("Invalid type for NumberConverter [" + convertTo + "]");
    } catch (NumberFormatException e) {
      throw new ConversionException(e);
    }
  }

  protected String primitiveToString(Object value, Class convertFrom, Map<String, String> attributes)
    throws ConversionException, ConverterStateException {
    return value.toString();

    // TODO add precision support
//        if (convertFrom == Byte.TYPE || convertFrom == Byte.class ||
//                convertFrom == Short.TYPE || convertFrom == Short.class ||
//                convertFrom == Integer.TYPE || convertFrom == Integer.class ||
//                convertFrom == Long.TYPE || convertFrom == Long.class) {
//            return value.toString();
//        } else if (convertFrom == Float.TYPE || convertFrom == Float.class ||
//                convertFrom == Double.TYPE || convertFrom == Double.class) {
//            return Double.valueOf(value);
//        } else if (convertFrom == BigInteger.class) {
//            return new BigInteger(value);
//        } else if (convertFrom == BigDecimal.class) {
//            return new BigDecimal(value);
//        }
  }
}
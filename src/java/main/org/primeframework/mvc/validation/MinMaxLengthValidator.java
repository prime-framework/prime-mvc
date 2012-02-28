/*
 * Copyright (c) 2001-2011, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.validation;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.primeframework.mvc.validation.annotation.MinMaxLength;

/**
 * <p> This class validates that the length of a value is within the bounds set in the annotation. This will work on
 * Strings and check the length of the String, Collections and check the size of the collection, Maps and check the size
 * of the Map, and arrays and check the length of the array. </p>
 *
 * @author Chadwick K. Boggs
 */
public class MinMaxLengthValidator implements Validator<MinMaxLength> {
  /**
   * @param annotation The MinMax annotation.
   * @param container  Not used.
   * @param value      The value to check.
   * @return True if the value is inclusively between min and max values, false otherwise.
   */
  public boolean validate(MinMaxLength annotation, Object container, Object value) {
    // Ignore validation if the value is null. This allows the Required annotation to be used
    // to ensure it is not null.
    if (value == null) {
      return true;
    }

    int length;
    if (value instanceof Collection) {
      length = ((Collection) value).size();
    } else if (value instanceof Map) {
      length = ((Map) value).size();
    } else if (value.getClass().isArray()) {
      length = Array.getLength(value);
    } else {
      length = value.toString().length();
    }

    return length >= annotation.min() && length <= annotation.max();
  }
}
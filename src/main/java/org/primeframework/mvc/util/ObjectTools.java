/*
 * Copyright (c) 2020, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Daniel DeGroff
 */
public class ObjectTools {

  /**
   * Helper method to get a value or a default. Both can be null so this is preferred when null values are okay.
   * Otherwise, use {@link Objects#requireNonNullElse(Object, Object)}
   *
   * @param value        The value.
   * @param defaultValue The default value.
   * @param <T>          The type (to prevent casting).
   * @return The value or the default.
   */
  public static <T> T defaultIfNull(T value, T defaultValue) {
    return value != null ? value : defaultValue;
  }

  /**
   * Compare the equality of a map of String arrays. This can perform an equality check on an HTTP request parameter
   * map.
   *
   * @param o1 object 1
   * @param o2 object 2
   * @return true if the objects are equal.
   */
  public static boolean equals(Map<String, String[]> o1, Map<String, String[]> o2) {
    if (o1 == null && o2 == null) {
      return true;
    }

    if (o1 == null || o2 == null) {
      return false;
    }

    if (o1.size() != o2.size()) {
      return false;
    }

    Set<String> keySet1 = o1.keySet();
    Set<String> keySet2 = o2.keySet();

    if (!keySet1.equals(keySet2)) {
      return false;
    }

    for (String key : keySet1) {
      String[] v1 = o1.get(key);
      String[] v2 = o2.get(key);
      if (!Arrays.equals(v1, v2)) {
        return false;
      }
    }

    return true;
  }
}

/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel DeGroff
 */
public class ParameterTools {
  /**
   * Combine original request parameters with additional values.
   *
   * @param original   the original request parameter map
   * @param parameters the new parameters to merge
   * @return a combined map that can be set into a wrapped HTTP servlet request.
   */
  public static Map<String, String[]> combine(Map<String, String[]> original, Map<String, List<String>> parameters) {
    Map<String, String[]> map = new HashMap<>();
    for (String key : original.keySet()) {
      String[] originalValues = original.get(key);
      List<String> parsedValues = parameters.remove(key);

      String[] newValues = new String[originalValues.length + (parsedValues == null ? 0 : parsedValues.size())];
      System.arraycopy(originalValues, 0, newValues, 0, originalValues.length);

      if (parsedValues != null && parsedValues.size() > 0) {
        int index = originalValues.length;
        for (String parsedValue : parsedValues) {
          newValues[index++] = parsedValue;
        }
      }

      map.put(key, newValues);
    }

    for (String key : parameters.keySet()) {
      List<String> parsedValues = parameters.get(key);
      map.put(key, parsedValues.toArray(new String[0]));
    }

    return map;
  }
}

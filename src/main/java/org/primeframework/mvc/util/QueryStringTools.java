/*
 * Copyright (c) 2019, Inversoft Inc., All Rights Reserved
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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helpful tools for Query String handling.
 *
 * @author Brian Pontarelli
 */
public class QueryStringTools {
  /**
   * Parses a query string with encoded parameters into a parameters Map (decoded).
   *
   * @param query The query string to parse.
   * @return The parameters in a Map.
   */
  public static Map<String, List<String>> parseQueryString(String query) {
    Map<String, List<String>> parameters = new HashMap<>();

    try {
      String[] parts = query.split("&");
      for (String part : parts) {
        String[] pieces = part.split("=");
        String name = URLDecoder.decode(pieces[0], StandardCharsets.UTF_8);
        String value = pieces.length > 1 ? URLDecoder.decode(pieces[1], StandardCharsets.UTF_8) : "";
        parameters.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
      }
    } catch (Exception e) {
      // Rethrow
      throw new IllegalArgumentException("Invalid query string [" + query + "]", e);
    }

    return parameters;
  }
}

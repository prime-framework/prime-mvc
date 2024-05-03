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

import java.util.Locale;

/**
 * Locale helper methods to roughly match Jackson's loose handling.
 *
 * @author Brian Pontarelli
 */
public class LocaleTools {
  public static Locale toLocale(String value) {
    if (value == null) {
      return null;
    }

    if (value.isBlank()) {
      return Locale.ROOT;
    }

    int ix = firstHyphenOrUnderscore(value);
    if (ix < 0) { // single argument
      return Locale.of(value);
    }

    String first = value.substring(0, ix);
    value = value.substring(ix + 1);
    ix = firstHyphenOrUnderscore(value);
    if (ix < 0) { // two pieces
      return Locale.of(first, value);
    }

    String second = value.substring(0, ix);
    return Locale.of(first, second, value.substring(ix + 1));
  }

  public static int firstHyphenOrUnderscore(String str) {
    for (int i = 0, end = str.length(); i < end; ++i) {
      char c = str.charAt(i);
      if (c == '_' || c == '-') {
        return i;
      }
    }
    return -1;
  }
}

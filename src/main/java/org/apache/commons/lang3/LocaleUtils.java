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
package org.apache.commons.lang3;

import java.util.Locale;

public class LocaleUtils {

  //-----------------------------------------------------------------------

  /**
   * <p>Converts a String to a Locale.</p>
   *
   * <p>This method takes the string format of a locale and creates the
   * locale object from it.</p>
   *
   * <pre>
   *   LocaleUtils.toLocale("en")         = new Locale("en", "")
   *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
   *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
   * </pre>
   *
   * <p>(#) The behaviour of the JDK variant constructor changed between JDK1.3 and JDK1.4.
   * In JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't. Thus, the result from getVariant() may
   * vary depending on your JDK.</p>
   *
   * <p>This method validates the input strictly.
   * The language code must be lowercase. The country code must be uppercase. The separator must be an underscore. The
   * length must be correct.
   * </p>
   *
   * @param str the locale String to convert, null returns null
   * @return a Locale, null if null input
   * @throws IllegalArgumentException if the string is an invalid format
   */
  public static Locale toLocale(String str) {
    if (str == null) {
      return null;
    }
    int len = str.length();
    if (len != 2 && len != 5 && len < 7) {
      throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    char ch0 = str.charAt(0);
    char ch1 = str.charAt(1);
    if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
      throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    if (len == 2) {
      return new Locale(str, "");
    } else {
      if (str.charAt(2) != '_') {
        throw new IllegalArgumentException("Invalid locale format: " + str);
      }
      char ch3 = str.charAt(3);
      if (ch3 == '_') {
        return new Locale(str.substring(0, 2), "", str.substring(4));
      }
      char ch4 = str.charAt(4);
      if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
        throw new IllegalArgumentException("Invalid locale format: " + str);
      }
      if (len == 5) {
        return new Locale(str.substring(0, 2), str.substring(3, 5));
      } else {
        if (str.charAt(5) != '_') {
          throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        return new Locale(str.substring(0, 2), str.substring(3, 5), str.substring(6));
      }
    }
  }

}

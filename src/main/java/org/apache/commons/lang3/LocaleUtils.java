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
   * <br>
   * <p>
   * <strong>Updated on 01/12/2021</strong>
   * See https://github.com/PascalSchumacher/commons-lang/blob/ce3e3e03e4d561dfae565186b431a879a9afa920/src/main/java/org/apache/commons/lang3/LocaleUtils.java
   * </p>
   * <br>
   * @param str the locale String to convert, null returns null
   * @return a Locale, null if null input
   * @throws IllegalArgumentException if the string is an invalid format
   */
  public static Locale toLocale(String str) {
    if (str == null) {
      return null;
    }
    if (str.isEmpty()) { // LANG-941 - JDK 8 introduced an empty locale where all fields are blank
      return new Locale(StringUtils.EMPTY, StringUtils.EMPTY);
    }
    if (str.contains("#")) { // LANG-879 - Cannot handle Java 7 script & extensions
      throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    final int len = str.length();
    if (len < 2) {
      throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    final char ch0 = str.charAt(0);
    if (ch0 == '_') {
      if (len < 3) {
        throw new IllegalArgumentException("Invalid locale format: " + str);
      }
      final char ch1 = str.charAt(1);
      final char ch2 = str.charAt(2);
      if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
        throw new IllegalArgumentException("Invalid locale format: " + str);
      }
      if (len == 3) {
        return new Locale(StringUtils.EMPTY, str.substring(1, 3));
      }
      if (len < 5) {
        throw new IllegalArgumentException("Invalid locale format: " + str);
      }
      if (str.charAt(3) != '_') {
        throw new IllegalArgumentException("Invalid locale format: " + str);
      }
      return new Locale(StringUtils.EMPTY, str.substring(1, 3), str.substring(4));
    }

    final String[] split = str.split("_", -1);
    final int occurrences = split.length - 1;
    switch (occurrences) {
      case 0:
        if (StringUtils.isAllLowerCase(str) && (len == 2 || len == 3)) {
          return new Locale(str);
        }
        throw new IllegalArgumentException("Invalid locale format: " + str);

      case 1:
        if (StringUtils.isAllLowerCase(split[0]) &&
            (split[0].length() == 2 || split[0].length() == 3) &&
            (split[1].length() == 2 && StringUtils.isAllUpperCase(split[1])) ||
            (split[1].length() == 3 && StringUtils.isNumeric(split[1]))) {
          return new Locale(split[0], split[1]);
        }
        throw new IllegalArgumentException("Invalid locale format: " + str);

      case 2:
        if (StringUtils.isAllLowerCase(split[0]) &&
            (split[0].length() == 2 || split[0].length() == 3) &&
            (split[1].length() == 0 || split[1].length() == 2 && StringUtils.isAllUpperCase(split[1])) &&
            split[2].length() > 0) {
          return new Locale(split[0], split[1], split[2]);
        }

        //$FALL-THROUGH$
      default:
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }
  }

}

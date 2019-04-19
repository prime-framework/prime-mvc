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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Daniel DeGroff
 */
public class EncodingUtils {

  /**
   * Basic escape of double quotes and back slash.
   *
   * @param s the string to escape
   * @return an escaped string.
   * @see <a href="https://tools.ietf.org/html/rfc2616#section-2.2">https://tools.ietf.org/html/rfc2616#section-2.2</a>
   */
  public static String escapedQuotedString(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  /**
   * Encode a string using <a href="http://tools.ietf.org/html/rfc5987">RFC 5987</a> standard.
   * <p>
   *
   * @param s the input string
   * @return an encoded string
   * @see <a href="https://stackoverflow.com/a/11307864">https://stackoverflow.com/a/11307864</a>
   */
  public static String rfc5987_encode(String s) {
    final byte[] s_bytes = s.getBytes(StandardCharsets.UTF_8);
    final int len = s_bytes.length;
    final StringBuilder sb = new StringBuilder(len << 1);
    final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    final byte[] attr_char = {'!', '#', '$', '&', '+', '-', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '|', '~'};
    for (int i = 0; i < len; ++i) {
      final byte b = s_bytes[i];
      if (Arrays.binarySearch(attr_char, b) >= 0) {
        sb.append((char) b);
      } else {
        sb.append('%');
        sb.append(digits[0x0f & (b >>> 4)]);
        sb.append(digits[b & 0x0f]);
      }
    }

    return sb.toString();
  }


}

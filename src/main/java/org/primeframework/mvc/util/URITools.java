/*
 * Copyright (c) 2012-2020, Inversoft Inc., All Rights Reserved
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

/**
 * URI tools.
 *
 * @author Brian Pontarelli
 */
public class URITools {
  private static final String URL_PATH_OTHER_SAFE_CHARS =
      // Unreserved characters
      "-._~"
          // The subdelim characters
          + "!$'()*,;&=+"
          // The gendelim characters permitted in paths
          + "@:"
          // A-Z
          + "ABCDEFGHIJKLMNOPQRSTVUWXYZ"
          // a-z
          + "abcdefghijklmnopqrstuvwxyz"
          // 0-9
          + "0123456789";

  private static final boolean[] UrlPathSafeCharacters = initializeURLPathSafeCharacters();

  public static String decodeURIPathSegment(Object value) {
    StringBuilder sb = new StringBuilder();
    char[] chars = value.toString().toCharArray();
    for (int i = 0; i < chars.length; ) {
      char c = chars[i];
      if (c == '%') {
        String hex = new String(new char[]{chars[i + 1], chars[i + 2]});
        sb.append(unHex(hex));
        i = i + 3;
      } else {
        sb.append(c);
        i++;
      }
    }

    return sb.toString();
  }

  /**
   * Determines the extension from the given URI.
   *
   * @param uri The URI.
   * @return The extension or null if there isn't one or the URI is something like /foo-1.0
   */
  public static String determineExtension(String uri) {
    String extension = null;
    int index = uri.lastIndexOf('.');
    if (index >= 0) {
      extension = uri.substring(index + 1);

      // Sanity check the extension to ensure it is NOT part of a version number like /foo-1.0
      boolean good = false;
      for (int i = 0; i < extension.length(); i++) {
        good = Character.isLetter(extension.charAt(i));
        if (!good) {
          break;
        }
      }

      if (!good || extension.contains("/")) {
        extension = null;
      }
    }

    return extension;
  }

  public static String encodeURIPathSegment(Object value) {
    StringBuilder sb = new StringBuilder();
    for (char ch : value.toString().toCharArray()) {
      if (UrlPathSafeCharacters[ch]) {
        sb.append(ch);
      } else {
        sb.append('%');
        sb.append(toHex(ch / 16));
        sb.append(toHex(ch % 16));
      }
    }

    return sb.toString();
  }

  private static boolean[] initializeURLPathSafeCharacters() {
    int maxChar = -1;
    char[] safeCharArray = URL_PATH_OTHER_SAFE_CHARS.toCharArray();
    for (char c : safeCharArray) {
      maxChar = Math.max(c, maxChar);
    }
    boolean[] octets = new boolean[maxChar + 1];
    for (char c : safeCharArray) {
      octets[c] = true;
    }

    return octets;
  }

  private static char toHex(int ch) {
    return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
  }

  private static String unHex(String s) {
    return String.valueOf((char) Integer.parseInt(s, 16));
  }
}

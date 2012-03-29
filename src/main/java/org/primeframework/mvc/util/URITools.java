/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
}

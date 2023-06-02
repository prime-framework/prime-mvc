/*
 * Copyright (c) 2021-2023, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.http;

import java.util.LinkedList;
import java.util.regex.Pattern;

import io.fusionauth.http.HTTPValues;
import io.fusionauth.http.server.HTTPRequest;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Toolkit for Servlet related tasks
 *
 * @author James Humphrey
 */
public class HTTPTools {
  private static final Pattern DoubleSlash = Pattern.compile("/{2,}");

  /**
   * Return the <code>Origin</code> header or as a fallback, the value of the <code>Referer</code> header will be returned if the <code>Origin</code>
   * header is not available.
   *
   * <p>
   * This handles a <code>"null"</code> Origin value and returns <code>null</code>.
   * </p>
   * <p>
   * See <a href="https://stackoverflow.com/a/42242802/3892636">https://stackoverflow.com/a/42242802/3892636</a> for more information.
   *
   * @param request the request.
   * @return null if no value can be found for the Origin or Referer header.
   */
  public static String getOriginHeader(HTTPRequest request) {
    String value = defaultIfNull(request.getHeader(HTTPValues.Headers.Origin), request.getHeader(HTTPValues.Headers.Referer));
    if (value == null || value.equals("null")) {
      return null;
    }

    return value;
  }

  /**
   * The request URI without the context path.
   *
   * @param request The request.
   * @return The uri minus the context path.
   */
  public static String getRequestURI(HTTPRequest request) {
    String uri = request.getPath();

    int semicolon = uri.indexOf(';');
    if (semicolon >= 0) {
      uri = uri.substring(0, semicolon);
    }

    String context = request.getContextPath();
    if (context.length() > 0) {
      return uri.substring(context.length());
    }

    return DoubleSlash.matcher(uri).replaceAll("/");
  }

  public static String sanitizeURI(String uri) {
    if (uri == null) {
      return null;
    }

    char[] ca = uri.replace("%2E", ".").toCharArray();
    int dots = 0;
    int slash = 0;
    boolean goodSegment = false;
    LinkedList<String> segments = new LinkedList<>();

    for (int i = 0; i < ca.length; i++) {
      char c = ca[i];

      if (c == '/') {
        // If this is a good segment, add it
        if (goodSegment || dots > 2) {
          segments.addLast(new String(ca, slash, i - slash));
        } else if (dots == 2) {
          if (segments.isEmpty()) {
            return null;
          }

          segments.removeLast();
        }

        slash = i;
        dots = 0;
        goodSegment = false;
      } else if (c == '.') {
        dots++;
      } else {
        goodSegment = true;
        dots = 0;
      }
    }

    if (goodSegment) {
      segments.addLast(new String(ca, slash, ca.length - slash));
    }

    return String.join("", segments);
  }
}

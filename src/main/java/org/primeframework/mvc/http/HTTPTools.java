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
package org.primeframework.mvc.http;

import java.net.URI;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Toolkit for Servlet related tasks
 *
 * @author James Humphrey
 */
public class HTTPTools {
  private static final Pattern DoubleSlash = Pattern.compile("/{2,}");

  /**
   * Returns the base URI in the following format:
   * <p/>
   * protocol://host[:port]
   * <p/>
   * This handles proxies, ports, schemes, everything.
   *
   * @param request the ServletRequest
   * @return A URI in the format of protocol://host[:port]
   */
  public static URI getBaseURI(HTTPRequest request) {
    return URI.create(toBaseURI(request));
  }

  /**
   * Returns the full URL based on the information in the request.
   *
   * @param request The request.
   * @return The full URL (as a URI).
   */
  public static URI getFullURI(HTTPRequest request) {
    String uri = toBaseURI(request);
    if (request.getContextPath() != null) {
      uri += request.getContextPath();
    }
    if (request.getPath() != null) {
      uri += request.getPath();
    }
    return URI.create(uri);
  }

  /**
   * Return the <code>Origin</code> header or as a fall back, the value of the <code>Referer</code> header will be
   * returned if the <code>Origin</code> header is not available.
   *
   * <p>
   * This handles a <code>"null"</code> Origin value and returns <code>null</code>.
   * </p>
   * <p>
   * See <a href="https://stackoverflow.com/a/42242802/3892636">https://stackoverflow.com/a/42242802/3892636</a> for
   * more information.
   *
   * @param request the request.
   * @return null if no value can be found for the Origin or Referer header.
   */
  public static String getOriginHeader(HTTPRequest request) {
    String value = defaultIfNull(request.getHeader(HTTPStrings.Headers.Origin), request.getHeader(HTTPStrings.Headers.Referer));
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

  private static String toBaseURI(HTTPRequest request) {
    // Setting the wrong value in the X-Forwarded-Proto header seems to be a common issue that causes an exception during URI.create. Assuming request.getScheme() is not the problem and it is related to the proxy configuration.
    String scheme = defaultIfNull(request.getHeader(HTTPStrings.Headers.XForwardedProto), request.getScheme()).toLowerCase();
    if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
      throw new IllegalArgumentException("The request scheme is invalid. Only http or https are valid schemes. The X-Forwarded-Proto header has a value of [" + request.getHeader("X-Forwarded-Proto") + "], this is likely an issue in your proxy configuration.");
    }

    String serverName = defaultIfNull(request.getHeader(HTTPStrings.Headers.XForwardedHost), request.getHost()).toLowerCase();
    int serverPort = request.getPort();
    // Ignore port 80 for http
    if (request.getScheme().equalsIgnoreCase("http") && serverPort == 80) {
      serverPort = -1;
    }

    String forwardedPort = request.getHeader(HTTPStrings.Headers.XForwardedPort);
    if (forwardedPort != null) {
      serverPort = Integer.parseInt(forwardedPort);
    }

    String uri = scheme + "://" + serverName;
    if (serverPort > 0) {
      if ((scheme.equalsIgnoreCase("http") && serverPort != 80) || (scheme.equalsIgnoreCase("https") && serverPort != 443)) {
        uri += ":" + serverPort;
      }
    }

    return uri;
  }
}

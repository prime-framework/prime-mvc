/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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
 *
 */
package org.primeframework.mvc.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Toolkit for Servlet related tasks
 *
 * @author James Humphrey
 */
public class ServletTools {
  private static final Pattern DoubleSlash = Pattern.compile("/{2,}");

  private static final Logger logger = LoggerFactory.getLogger(ServletTools.class);

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
  public static URI getBaseURI(HttpServletRequest request) {
    String scheme = defaultIfNull(request.getHeader("X-Forwarded-Proto"), request.getScheme()).toLowerCase();
    // Setting the wrong value in the X-Forwarded-Proto header seems to be a common issue that causes an exception during URI.create. Assuming request.getScheme() is not the problem and it is related to the proxy configuration.
    if (!scheme.equals("http") && !scheme.equals("https")) {
      throw new IllegalArgumentException("The request scheme is invalid. Only http or https are valid schemes. The X-Forwarded-Proto header has a value of [" + request.getHeader("X-Forwarded-Proto") + "], this is likely an issue in your proxy configuration.");
    }
    String serverName = defaultIfNull(request.getHeader("X-Forwarded-Host"), request.getServerName()).toLowerCase();
    int serverPort = request.getServerPort();
    // Ignore port 80 for http
    if (request.getScheme().equalsIgnoreCase("http") && serverPort == 80) {
      serverPort = -1;
    }

    String forwardedPort = request.getHeader("X-Forwarded-Port");
    if (forwardedPort != null) {
      serverPort = Integer.parseInt(forwardedPort);
    }

    String uri = scheme + "://" + serverName;
    if (serverPort > 0) {
      if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
        uri += ":" + serverPort;
      }
    }

    return URI.create(uri);
  }

  /**
   * Returns the base url in the following format:
   * <p/>
   * protocol://host[:port]/
   * <p/>
   * The protocol, host and port are extracted from the ServletRequest using the following methods:
   * <p/>
   * protocol = ServletRequest.getScheme() host = ServletRequest.getServerName() port = ServletRequest.getServerPost()
   * <p/>
   * Below are some examples of base URLs that can be returned:
   * <p/>
   * <strong>http://www.Inversoft Inc./</strong> <strong>https://www.Inversoft Inc.:8080/</strong>
   *
   * @param request the ServletRequest
   * @return a URL in the format of protocol://host[:port]/
   * @deprecated Use the URI method.
   */
  public static URL getBaseUrl(ServletRequest request) {
    String scheme = request.getScheme();
    String serverName = request.getServerName();
    int serverPort = request.getServerPort();

    URL baseUrl = null;
    try {
      if (serverPort > 0) {
        baseUrl = new URL(scheme, serverName, serverPort, "/");
      } else {
        baseUrl = new URL(scheme, serverName, "/");
      }
    } catch (MalformedURLException e) {
      logger.error(e.getMessage(), e);
    }

    return baseUrl;
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
  public static String getOriginHeader(HttpServletRequest request) {
    String value = defaultIfNull(request.getHeader("Origin"), request.getHeader("Referer"));
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
  public static String getRequestURI(HttpServletRequest request) {
    String uri = request.getRequestURI();
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

  /**
   * Returns the jsessionid part of the URI if it exists.
   *
   * @param request The request.
   * @return The jsessionid part or empty string.
   */
  public static String getSessionId(HttpServletRequest request) {
    String uri = request.getRequestURI();
    int semicolon = uri.indexOf(';');
    if (semicolon >= 0) {
      return uri.substring(semicolon);
    }

    return "";
  }
}

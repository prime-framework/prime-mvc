/*
 * Copyright (c) 2021-2022, Inversoft Inc., All Rights Reserved
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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.primeframework.mvc.parameter.fileupload.FileInfo;

/**
 * The HTTP request that is sent to the server (when acting as the client) or that was received from the client (when
 * acting as the server).
 * <p>
 * This is the read-only interface. If you need to mutate the request, use the {@link DefaultHTTPRequest} or
 * {@link MutableHTTPRequest} classes.
 *
 * @author Brian Pontarelli
 */
public interface HTTPRequest {
  /**
   * @param key the name of the attribute
   * @return the value of the attribute or <code>null</code> if no value exists by this name.
   */
  Object getAttribute(String key);

  /**
   * Return all the request attributes in a map keyed by name.
   *
   * @return a map of attributes
   */
  Map<String, Object> getAttributes();

  /**
   * The base URL of the server to which the request was sent.
   * <p>
   * The returned value will be in format: <code> protocol://host[:port]</code>.
   * <p>
   * The returned value should be equivalent to building the same value by combining the returned values of calling
   * {@link #getScheme()}, {@link #getHost()} and {@link #getPort()}.
   *
   * @return the URL used to make the request.
   */
  String getBaseURL();

  /**
   * @return The request body in a byte buffer where the position is the last byte read in and the start is 0.
   */
  ByteBuffer getBody();

  /**
   * @return the character encoding of this request.
   */
  Charset getCharacterEncoding();

  /**
   * @return The length of the content in bytes or null if the content-length header was not sent in the request.
   */
  Long getContentLength();

  /**
   * @return the value of the <code>Content-Type</code> HTTP request header.
   */
  String getContentType();

  /**
   * @return the context path if set, or an empty string if no context path has been set.
   */
  String getContextPath();

  /**
   * Return a cookie by name.
   *
   * @param name the name of the cookie
   * @return a cookie or <code>null</code> if no cookie exists by the requested name.
   */
  Cookie getCookie(String name);

  /**
   * @return all cookies on the request.
   */
  List<Cookie> getCookies();

  /**
   * @param key the name of the date header, if not found a default date will be returned, using the
   *            <code>RFC-1123</code> format.
   * @return an Instant representation of the date header.
   */
  Instant getDateHeader(String key);

  /**
   * @return the files from the request.
   */
  List<FileInfo> getFiles();

  /**
   * Returns the value of the HTTP request header by key.
   * <p>
   * If more than one value exists for this HTTP header, only the first value will be returned.
   *
   * @param key the HTTP header name.
   * @return the value for the HTTP header, or <code>null</code> if the HTTP header does not exist.
   */
  String getHeader(String key);

  /**
   * Return all values for the HTTP request header by key.
   *
   * @param key the HTTP header name.
   * @return a list of values for the HTTP header, or <code>null</code> if the HTTP header does not exist.
   */
  List<String> getHeaders(String key);

  /**
   * Return all available HTTP headers in a map keyed by HTTP header name.
   *
   * @return a map of HTTP headers.
   */
  Map<String, List<String>> getHeadersMap();

  /**
   * The host name of the server to which the request was sent.
   * <p>
   * If the HTTP request header <code>X-Forwarded-Host</code> is present, this value will be preferred to the value
   * found in the <code>HOST</code> header.
   *
   * <p>
   * If an IP address is used on the URL, this value will be an IP address in string form.
   * <p>
   *
   * @return the host value for this HTTP request.
   */
  String getHost();

  /**
   * The IP address of client where the request originated.
   * <p>
   * If the HTTP request header <code>X-Forwarded-For</code> is present, the left-most value found in this header value
   * will be preferred which should represent the original client address.
   *
   * @return an IP address.
   */
  String getIPAddress();

  /**
   * Return a single Locale if available. If more than one Locale are available, the first value will be returned.
   *
   * @return the locale if found or <code>null</code> if one is not found.
   */
  Locale getLocale();

  /**
   * @return all available locales or an empty list of no locales are available.
   */
  List<Locale> getLocales();

  /**
   * Return the HTTP method that was used to make this request.
   *
   * @return the HTTP method
   */
  HTTPMethod getMethod();

  /**
   * Return a single parameter value from the HTTP request. If more than one value exist for this key, only the first
   * value will be returned.
   * <p>
   * Use {@link #getParameterValues(String)} to retrieve all values associated with this key.
   *
   * @param key the name of the parameter.
   * @return the first value of the parameter by name or <code>null</code> if the parameter is not found.
   */
  String getParameterValue(String key);

  /**
   * Return all parameter values from the HTTP request associated with this key.
   *
   * @param key the name of the parameter.
   * @return the list of values of the parameter by name or <code>null</code> if the parameter is not found.
   */
  List<String> getParameterValues(String key);

  /**
   * @return a map of all the HTTP request parameters.
   */
  Map<String, List<String>> getParameters();

  /**
   * The HTTP request path is the part of the request URL from the protocol (scheme) to the query string.
   *
   * @return the request path.
   */
  String getPath();

  /**
   * The port of the server to which the request was sent.
   * <p>
   * If the HTTP request header <code>X-Forwarded-Port</code> is present, this value will be preferred over the port
   * identified by the remote socket.
   *
   * @return the port for this request or -1 if no port has been assigned.
   */
  int getPort();

  /**
   * The query string from the URI, this value will not have been URL decoded, but should be the exact value found on
   * the URI after the
   * <code>?</code> on the URL.
   *
   * @return the query string found in the HTTP request URL or <code>null</code> if no query string was provided.
   */
  String getQueryString();

  /**
   * Return the HTTP request schema (protocol) used when making this request.
   * <p>
   * If the HTTP request header <code>X-Forwarded-Proto</code> is present, this value will be preferred.
   *
   * @return the scheme (protocol) used on this HTTP request.
   */
  String getScheme();

  /**
   * The full URL of the server to which the request was sent.
   * <p>
   * The returned value will be in format: <code> protocol://host[:port]/contextPath/path</code>.
   * <p>
   * The returned value should be equivalent to building the same value by combining the returned values of calling
   * {@link #getScheme()}, {@link #getHost()}, {@link #getPort()}, {@link #getContextPath()} ()} and
   * {@link #getPath()}.
   *
   * @return the URL used to make the request.
   */
  String getURL();

  /**
   * @return true if this request is using a <code>Content-Type</code> of <code>multipart/form-data</code>.
   */
  boolean isMultipart();

  /**
   * Remove an HTTP request attribute that has previously been added to this request using the
   * {@link #setAttribute(String, Object)}.
   *
   * @param key the name of the key used to store the value you wish to remove.
   */
  void removeAttribute(String key);

  /**
   * Set an HTTP request attribute.
   *
   * @param key   the name of the key in which to store the value.
   * @param value the value of the attribute.
   */
  void setAttribute(String key, Object value);
}

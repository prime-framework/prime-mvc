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
 * This is the read-only interface. If you need to mutate the request, use the {@link DefaultHTTPRequest} or {@link
 * MutableHTTPRequest} classes.
 *
 * @author Brian Pontarelli
 */
public interface HTTPRequest {
  Object getAttribute(String key);

  Map<String, Object> getAttributes();

  byte[] getBody();

  Charset getCharacterEncoding();

  long getContentLength();

  String getContentType();

  String getContextPath();

  Cookie getCookie(String name);

  List<Cookie> getCookies();

  Instant getDateHeader(String key);

  List<FileInfo> getFiles();

  String getHeader(String key);

  List<String> getHeaders(String key);

  Map<String, List<String>> getHeadersMap();

  String getHost();

  Locale getLocale();

  List<Locale> getLocales();

  HTTPMethod getMethod();

  String getParameterValue(String key);

  List<String> getParameterValues(String key);

  Map<String, List<String>> getParameters();

  String getPath();

  int getPort();

  String getRemoteAddress();

  String getRemoteHost();

  /**
   * @return The remote IP address that might be from the network layer or from an HTTP X-Forwarded-For header.
   */
  String getRemoteIPAddress();

  String getScheme();

  boolean isMultipart();

  void removeAttribute(String key);

  void setAttribute(String key, Object value);
}

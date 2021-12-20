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

import java.io.OutputStream;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * The HTTP response that is sent back to the client.
 *
 * @author Brian Pontarelli
 */
public interface HTTPResponse {
  void addCookie(Cookie cookie);

  /**
   * Add an HTTP header. Calling this method more than once will write the header with multiple values.
   *
   * @param name  the name of the header
   * @param value the value
   */
  void addHeader(String name, String value);

  /**
   * @param name the header name
   * @return true if the header name has already been set.
   */
  boolean containsHeader(String name);

  /**
   * @return True if the status code is not 2xx.
   */
  boolean failure();

  Long getContentLength();

  void setContentLength(Long length);

  String getContentType();

  void setContentType(String contentType);

  List<Cookie> getCookies();

  String getHeader(String name);

  List<String> getHeaders(String name);

  Map<String, List<String>> getHeadersMap();

  /**
   * <strong>NOTE:</strong> If you have already specified the Content-Length header, then this will return a direct
   * connection to the HTTP response and immediately flush the headers to the client. This means that the bytes written
   * out will not be cached in memory in order to count them. Also, if you write more bytes then the Content-Length
   * header, they will be ignored. Calling the close() or flush() methods on this OutputStream has no effect in order to
   * ensure that Keep-Alive headers work properly.
   * <p>
   * If you haven't written out a Content-Length header, then this will use a caching OutputStream in order to count the
   * number of bytes. You MUST call the send() method on the response in order for this OutputStream to be written to
   * the client. If you don't call that method, nothing will happen.
   *
   * @return The output stream.
   */
  OutputStream getOutputStream();

  String getRedirect();

  int getStatus();

  void setStatus(int status);

  /**
   * Wraps the OutputStream in a Writer regardless of the current state of the OutputStream. This means that the Writer
   * will happily use the OutputStream as is. See the JavaDoc for {@link #getOutputStream()} for more information.
   *
   * @return The Writer.
   */
  Writer getWriter();

  /**
   * Removes a cookie(s) from the response so it isn't sent back to the client.
   *
   * @param name The name of the cookie.
   */
  void removeCookie(String name);

  /**
   * Sends a redirect response and closes the response.
   *
   * @param uri The URI.
   */
  void sendRedirect(String uri);

  /**
   * Set a date header, overwrite the existing value if the header has already been added.
   *
   * @param name  The name of the header.
   * @param value The instant as a ZonedDateTime.
   */
  void setDateHeader(String name, ZonedDateTime value);

  void setHeader(String name, String value);
}

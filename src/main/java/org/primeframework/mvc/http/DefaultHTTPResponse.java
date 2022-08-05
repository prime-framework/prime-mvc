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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.netty.handler.codec.http.HttpUtil;

/**
 * Default class for HTTPResponse.
 *
 * @author Brian Pontarelli
 */
public class DefaultHTTPResponse implements HTTPResponse {
  private final Map<String, Map<String, Cookie>> cookies = new HashMap<>(); // <Path, <Name, Cookie>>

  private final Map<String, List<String>> headers = new HashMap<>();

  private final OutputStream outputStream;

  private Throwable exception;

  private int status;

  public DefaultHTTPResponse(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  @Override
  public void addCookie(Cookie cookie) {
    String path = cookie.path != null ? cookie.path : "/";
    cookies.computeIfAbsent(path, key -> new HashMap<>()).put(cookie.name, cookie);
  }

  @Override
  public void addHeader(String name, String value) {
    if (name != null && name.equalsIgnoreCase("Set-Cookie")) {
      addCookie(new Cookie(name, value));
    }

    headers.putIfAbsent(name, new ArrayList<>());
    headers.get(name).add(value);
  }

  @Override
  public boolean containsHeader(String name) {
    return headers.containsKey(name) && headers.get(name).size() > 0;
  }

  @Override
  public boolean failure() {
    return status < 200 || status > 299;
  }

  @Override
  public Long getContentLength() {
    if (containsHeader("Content-Length")) {
      return Long.parseLong(getHeader("Content-Length"));
    }

    return null;
  }

  @Override
  public void setContentLength(Long length) {
    setHeader("Content-Length", length.toString());
  }

  @Override
  public String getContentType() {
    if (containsHeader("Content-Type")) {
      return getHeader("Content-Type");
    }

    return null;
  }

  @Override
  public void setContentType(String contentType) {
    setHeader("Content-Type", contentType);
  }

  @Override
  public List<Cookie> getCookies() {
    return cookies.values()
                  .stream()
                  .flatMap(map -> map.values().stream())
                  .collect(Collectors.toList());
  }

  @Override
  public Throwable getException() {
    return exception;
  }

  @Override
  public void setException(Throwable exception) {
    this.exception = exception;
  }

  @Override
  public String getHeader(String name) {
    return headers.containsKey(name) && headers.get(name).size() > 0 ? headers.get(name).get(0) : null;
  }

  @Override
  public List<String> getHeaders(String key) {
    return headers.get(key);
  }

  @Override
  public Map<String, List<String>> getHeadersMap() {
    return headers;
  }

  @Override
  public OutputStream getOutputStream() {
    return outputStream;
  }

  @Override
  public String getRedirect() {
    return getHeader("Location");
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public void setStatus(int status) {
    this.status = status;
  }

  @Override
  public Writer getWriter() {
    Charset charset = StandardCharsets.UTF_8;
    String contentType = getContentType();
    if (contentType != null) {
      charset = HttpUtil.getCharset(contentType, StandardCharsets.UTF_8);
    }

    return new OutputStreamWriter(getOutputStream(), charset);
  }

  @Override
  public void removeCookie(String name) {
    cookies.values().forEach(map -> map.remove(name));
  }

  @Override
  public void sendRedirect(String uri) {
    setHeader("Location", uri);
    status = Status.MOVED_TEMPORARILY;
  }

  @Override
  public void setDateHeader(String name, ZonedDateTime value) {
    addHeader(name, DateTimeFormatter.RFC_1123_DATE_TIME.format(value));
  }

  @Override
  public void setHeader(String name, String value) {
    if (name == null || value == null) {
      return;
    }

    addHeader(name, value);
  }

  @Override
  public boolean wasOneByteWritten() {
    return !(outputStream instanceof HTTPOutputStream) || ((HTTPOutputStream) outputStream).wasOneByteWritten();
  }
}


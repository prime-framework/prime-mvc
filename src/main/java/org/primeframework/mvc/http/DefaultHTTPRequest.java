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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.primeframework.mvc.http.HTTPStrings.Headers;
import org.primeframework.mvc.parameter.fileupload.FileInfo;
import org.primeframework.mvc.util.Buildable;

@SuppressWarnings("unused")
public class DefaultHTTPRequest implements MutableHTTPRequest, Buildable<DefaultHTTPRequest> {
  public final Map<String, Object> attributes = new HashMap<>(0);

  public final Map<String, Cookie> cookies = new HashMap<>();

  public final List<FileInfo> files = new ArrayList<>();

  public final Map<String, List<String>> headers = new HashMap<>();

  public final List<Locale> locales = new ArrayList<>();

  public final Map<String, List<String>> parameters = new HashMap<>(0);

  public ByteBuffer body;

  public Long contentLength;

  public String contentType;

  public String contextPath = "";

  public Charset encoding = StandardCharsets.UTF_8;

  public String host;

  public String ipAddress;

  public HTTPMethod method;

  public boolean multipart;

  public String path = "/";

  public int port = -1;

  public String queryString;

  public String scheme;

  @Override
  public void addCookies(Cookie... cookies) {
    for (Cookie cookie : cookies) {
      this.cookies.put(cookie.name, cookie);
    }
  }

  @Override
  public void addCookies(Collection<Cookie> cookies) {
    if (cookies == null) {
      return;
    }

    for (Cookie cookie : cookies) {
      this.cookies.put(cookie.name, cookie);
    }
  }

  @Override
  public void addFile(FileInfo fileInfo) {
    this.files.add(fileInfo);
  }

  @Override
  public void addHeader(String name, String value) {
    headers.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
  }

  @Override
  public void addHeaders(String name, String... values) {
    headers.computeIfAbsent(name, key -> new ArrayList<>()).addAll(List.of(values));
  }

  @Override
  public void addHeaders(String name, Collection<String> values) {
    headers.computeIfAbsent(name, key -> new ArrayList<>()).addAll(values);
  }

  @Override
  public void addHeaders(Map<String, List<String>> params) {
    for (String name : params.keySet()) {
      headers.put(name, params.get(name));
    }
  }

  @Override
  public void addLocales(Locale... locales) {
    this.locales.addAll(Arrays.asList(locales));
  }

  @Override
  public void addLocales(Collection<Locale> locales) {
    this.locales.addAll(locales);
  }

  @Override
  public void addParameter(String name, String value) {
    parameters.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
  }

  @Override
  public void addParameters(String name, String... values) {
    parameters.computeIfAbsent(name, key -> new ArrayList<>()).addAll(List.of(values));
  }

  @Override
  public void addParameters(String name, Collection<String> values) {
    parameters.computeIfAbsent(name, key -> new ArrayList<>()).addAll(values);
  }

  @Override
  public void addParameters(Map<String, List<String>> params) {
    for (String name : params.keySet()) {
      parameters.put(name, params.get(name));
    }
  }

  @Override
  public void deleteCookie(String name) {
    cookies.remove(name);
  }

  @Override
  public Object getAttribute(String key) {
    return attributes.get(key);
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getBaseURL() {
    // Setting the wrong value in the X-Forwarded-Proto header seems to be a common issue that causes an exception during URI.create. Assuming request.getScheme() is not the problem and it is related to the proxy configuration.
    String scheme = getScheme().toLowerCase();
    if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
      throw new IllegalArgumentException("The request scheme is invalid. Only http or https are valid schemes. The X-Forwarded-Proto header has a value of [" + getHeader(Headers.XForwardedProto) + "], this is likely an issue in your proxy configuration.");
    }

    String serverName = getHost().toLowerCase();
    int serverPort = getPort();
    // Ignore port 80 for http
    if (getScheme().equalsIgnoreCase("http") && serverPort == 80) {
      serverPort = -1;
    }

    String uri = scheme + "://" + serverName;
    if (serverPort > 0) {
      if ((scheme.equalsIgnoreCase("http") && serverPort != 80) || (scheme.equalsIgnoreCase("https") && serverPort != 443)) {
        uri += ":" + serverPort;
      }
    }

    return uri;
  }

  @Override
  public ByteBuffer getBody() {
    return body;
  }

  @Override
  public void setBody(ByteBuffer body) {
    this.body = body;
    this.contentLength = (long) body.position();
  }

  @Override
  public Charset getCharacterEncoding() {
    return encoding;
  }

  @Override
  public void setCharacterEncoding(Charset encoding) {
    this.encoding = encoding;
  }

  @Override
  public Long getContentLength() {
    return contentLength;
  }

  @Override
  public void setContentLength(Long contentLength) {
    this.contentLength = contentLength;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  @Override
  public String getContextPath() {
    return contextPath;
  }

  @Override
  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  @Override
  public Cookie getCookie(String name) {
    return cookies.get(name);
  }

  @Override
  public List<Cookie> getCookies() {
    return new ArrayList<>(cookies.values());
  }

  @Override
  public Instant getDateHeader(String key) {
    String header = getHeader(key);
    return header != null ? ZonedDateTime.parse(header, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant() : null;
  }

  @Override
  public List<FileInfo> getFiles() {
    return files;
  }

  @Override
  public String getHeader(String key) {
    List<String> values = getHeaders(key);
    return values != null && values.size() > 0 ? values.get(0) : null;
  }

  @Override
  public List<String> getHeaders(String key) {
    return headers.entrySet()
                  .stream()
                  .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                  .map(Entry::getValue)
                  .findFirst()
                  .orElse(null);
  }

  @Override
  public Map<String, List<String>> getHeadersMap() {
    return headers;
  }

  @Override
  public String getHost() {
    String xHost = getHeader(Headers.XForwardedHost);
    return xHost == null ? host : xHost;
  }

  @Override
  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public String getIPAddress() {
    String xIPAddress = getHeader(Headers.XForwardedFor);
    if (xIPAddress == null || xIPAddress.trim().length() == 0) {
      return ipAddress;
    }

    String[] ips = xIPAddress.split(",");
    if (ips.length < 1) {
      return xIPAddress.trim();
    }

    return ips[0].trim();
  }

  @Override
  public void setIPAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  @Override
  public Locale getLocale() {
    return locales.size() > 0 ? locales.get(0) : Locale.getDefault();
  }

  @Override
  public List<Locale> getLocales() {
    return locales;
  }

  @Override
  public HTTPMethod getMethod() {
    return method;
  }

  @Override
  public void setMethod(HTTPMethod method) {
    this.method = method;
  }

  @Override
  public String getParameterValue(String key) {
    List<String> values = parameters.get(key);
    return (values != null && values.size() > 0) ? values.get(0) : null;
  }

  @Override
  public List<String> getParameterValues(String key) {
    return parameters.get(key);
  }

  @Override
  public Map<String, List<String>> getParameters() {
    return parameters;
  }

  @Override
  public void setParameters(Map<String, List<String>> parameters) {
    this.parameters.clear();
    this.parameters.putAll(parameters);
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public int getPort() {
    String xPort = getHeader(Headers.XForwardedPort);
    return xPort == null ? port : Integer.parseInt(xPort);
  }

  @Override
  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public String getQueryString() {
    return queryString;
  }

  @Override
  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

  @Override
  public String getScheme() {
    String xScheme = getHeader(Headers.XForwardedProto);
    return xScheme == null ? scheme : xScheme;
  }

  @Override
  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  @Override
  public String getURL() {
    String url = getBaseURL();
    if (contextPath != null) {
      url += contextPath;
    }

    if (path != null) {
      url += path;
    }

    return url;
  }

  @Override
  public boolean isMultipart() {
    return multipart;
  }

  @Override
  public void setMultipart(boolean multipart) {
    this.multipart = multipart;
  }

  @Override
  public void removeAttribute(String key) {
    attributes.remove(key);
  }

  @Override
  public void removeHeader(String name) {
    headers.remove(name);
  }

  @Override
  public void removeHeader(String name, String... values) {
    List<String> actual = headers.get(name);
    if (actual != null) {
      actual.removeAll(List.of(values));
    }
  }

  @Override
  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  @Override
  public void setHeader(String name, String value) {
    this.headers.put(name, new ArrayList<>(List.of(value)));
  }

  @Override
  public void setHeaders(String name, String... values) {
    this.headers.put(name, new ArrayList<>(List.of(values)));
  }

  @Override
  public void setHeaders(String name, Collection<String> values) {
    this.headers.put(name, new ArrayList<>(values));
  }

  @Override
  public void setHeaders(Map<String, List<String>> parameters) {
    this.headers.clear();
    this.headers.putAll(parameters);
  }

  @Override
  public void setParameter(String name, String value) {
    setParameters(name, value);
  }

  @Override
  public void setParameters(String name, String... values) {
    setParameters(name, Arrays.asList(values));
  }

  @Override
  public void setParameters(String name, Collection<String> values) {
    List<String> list = new ArrayList<>();
    this.parameters.put(name, list);

    values.stream()
          .filter(Objects::nonNull)
          .forEach(list::add);
  }
}

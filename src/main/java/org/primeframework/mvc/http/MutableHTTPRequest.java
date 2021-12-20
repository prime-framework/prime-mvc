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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.primeframework.mvc.parameter.fileupload.FileInfo;

/**
 * The HTTP request that is sent to the server (when acting as the client) or that was received from the client (when
 * acting as the server). This interface is dramatically different than the Servlet interface because it is mutable.
 * This allows the MVC and also the Actions to mutate the HTTP request in order to simulate different requests without
 * using any wrappers.
 *
 * @author Brian Pontarelli
 */
public interface MutableHTTPRequest extends HTTPRequest {
  void addCookies(Cookie... cookies);

  void addCookies(Collection<Cookie> cookies);

  void addFile(FileInfo fileInfo);

  void addHeader(String name, String value);

  void addHeaders(String name, String... values);

  void addHeaders(String name, Collection<String> values);

  void addHeaders(Map<String, List<String>> params);

  void addLocales(Locale... locales);

  void addLocales(Collection<Locale> locales);

  void addParameter(String name, String value);

  void addParameters(String name, String... values);

  void addParameters(String name, Collection<String> values);

  void addParameters(Map<String, List<String>> params);

  void deleteCookie(String name);

  void setBody(byte[] body);

  void setCharacterEncoding(Charset encoding);

  void setRemoteAddress(String remoteAddress);

  void setContentLength(long contentLength);

  void setContentType(String contentType);

  void setContextPath(String contextPath);

  void setHeader(String name, String value);

  void setHeaders(String name, String... values);

  void setHeaders(String name, Collection<String> values);

  void setHeaders(Map<String, List<String>> parameters);

  void setHost(String host);

  void setMethod(HTTPMethod method);

  void setMultipart(boolean multipart);

  void setParameter(String name, String value);

  void setParameters(Map<String, List<String>> parameters);

  void setParameters(String name, String... values);

  void setParameters(String name, Collection<String> values);

  void setPath(String path);

  void setPort(int port);

  void setScheme(String scheme);
}

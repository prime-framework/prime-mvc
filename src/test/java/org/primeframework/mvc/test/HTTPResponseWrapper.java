/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.test;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.inversoft.http.HTTPStrings;
import io.fusionauth.http.Cookie;

/**
 * Test container for the HTTP Response and exception.
 *
 * @author Daniel DeGroff
 */
public class HTTPResponseWrapper {
  public Throwable exception;

  public Map<String, List<String>> headers;

  public HttpResponse<byte[]> response;

  public int status;

  public byte[] getBody() {
    return response != null
        ? response.body()
        : null;
  }

  public List<Cookie> getCookies(String name) {
    return getCookies()
        .stream()
        .filter(c -> c.name.equals(name))
        .toList();
  }

  public List<Cookie> getCookies() {
    if (response == null) {
      return List.of();
    }

    List<String> cookies = response.headers().allValues(HTTPStrings.Headers.SetCookie.toLowerCase());
    if (cookies != null && cookies.size() > 0) {
      return cookies.stream()
                    .map(Cookie::fromResponseHeader)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
    }

    return List.of();
  }

  public String getHeader(String name) {
    return response != null
        ? response.headers().firstValue(name.toLowerCase()).orElse(null)
        : null;
  }

  public int getStatus() {
    return status;
  }

  public void init() {
    status = response != null ? response.statusCode() : -1;
    headers = response != null ? response.headers().map() : Map.of();
  }
}

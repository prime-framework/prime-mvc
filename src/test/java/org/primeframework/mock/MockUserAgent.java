/*
 * Copyright (c) 2001-2020, Inversoft, All Rights Reserved
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
package org.primeframework.mock;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.primeframework.mvc.http.Cookie;
import org.primeframework.mvc.http.HTTPRequest;

/**
 * @author Daniel DeGroff
 */
public class MockUserAgent {
  // Cookies keyed by path.
  // Note: We may run some tests multi-threaded, use a concurrent map to protect from ConcurrentModificationException.
  public Map<String, Map<String, Cookie>> cookies = new ConcurrentHashMap<>();

  public void addCookie(Cookie cookie) {
    String cookiePath = cookie.path != null ? cookie.path : "/"; // Browsers default to / for path
    Map<String, Cookie> existing = this.cookies.computeIfAbsent(cookiePath, k -> new ConcurrentHashMap<>());
    existing.remove(cookie.name);

    // A Max-Age of 0 is a delete
    if ((cookie.maxAge == null || cookie.maxAge > 0) && (cookie.expires == null || cookie.expires.isBefore(ZonedDateTime.now(ZoneOffset.UTC)))) {
      existing.put(cookie.name, cookie);
    }
  }

  public void addCookies(Collection<Cookie> cookies) {
    for (Cookie cookie : cookies) {
      addCookie(cookie);
    }
  }

  public void clearCookies(String path) {
    cookies.remove(path);
  }

  public List<Cookie> getCookies(HTTPRequest request) {
    List<Cookie> cookies = new ArrayList<>();
    this.cookies.forEach((path, map) -> {
      if (request.getPath().startsWith(path)) {
        cookies.addAll(map.values());
      }
    });

    return cookies;
  }

  public void reset() {
    cookies.clear();
  }
}
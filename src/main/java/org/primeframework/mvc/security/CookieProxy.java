/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security;

import org.primeframework.mvc.http.Cookie;
import org.primeframework.mvc.http.Cookie.SameSite;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import static org.primeframework.mvc.util.ObjectTools.defaultIfNull;

/**
 * Something that can get a cookie...nom nom nom...
 *
 * @author Daniel DeGroff
 */
public class CookieProxy {
  private final Long maxAge;

  private final String name;

  private final SameSite sameSite;

  public CookieProxy(String name, Long maxAge, SameSite sameSite) {
    this.name = name;
    this.maxAge = maxAge;
    this.sameSite = sameSite;
  }

  public void add(HTTPRequest request, HTTPResponse response, String value) {
    Cookie cookie = new Cookie(name, value);
    cookie.httpOnly = true;
    cookie.maxAge = maxAge;
    cookie.path = "/";
    cookie.sameSite = sameSite;
    cookie.secure = "https".equalsIgnoreCase(defaultIfNull(request.getHeader("X-Forwarded-Proto"), request.getScheme()));
    response.addCookie(cookie);
  }

  public void delete(HTTPRequest request, HTTPResponse response) {
    Cookie cookie = new Cookie(name, null);
    cookie.httpOnly = true;
    cookie.maxAge = 0L;
    cookie.path = "/";
    cookie.sameSite = sameSite;
    cookie.secure = "https".equalsIgnoreCase(defaultIfNull(request.getHeader("X-Forwarded-Proto"), request.getScheme()));
    response.addCookie(cookie);

    // Ensure we do not return the cookie again after it has been deleted within the same request.
    request.setAttribute(name + "Deleted", true);
  }

  public String get(HTTPRequest request) {
    // Ensure we do not return the cookie again after it has been deleted within the same request.
    if (request.getAttribute(name + "Deleted") != null) {
      return null;
    }

    Cookie cookie = request.getCookie(name);
    return cookie != null ? cookie.value : null;
  }
}

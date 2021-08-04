/*
 * Copyright (c) 2020, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Daniel DeGroff
 */
public abstract class AbstractCookie {
  protected final HttpServletRequest request;

  protected final HttpServletResponse response;

  protected AbstractCookie(HttpServletRequest request, HttpServletResponse response) {
    this.request = request;
    this.response = response;
  }

  protected void addSecureHttpOnlyCookie(String name, String value, int maxAge) {
    Cookie cookie = buildSecureHttpOnlyCookie(name, value, maxAge);
    response.addCookie(cookie);
  }

  protected void addSecureHttpOnlySessionCookie(String name, String value) {
    Cookie cookie = buildSecureHttpOnlyCookie(name, value, -1);
    response.addCookie(cookie);
  }

  protected Cookie buildSecureHttpOnlyCookie(String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setSecure("https".equalsIgnoreCase(defaultIfNull(request.getHeader("X-Forwarded-Proto"), request.getScheme())));
    cookie.setHttpOnly(true);
    cookie.setMaxAge(maxAge);
    cookie.setPath("/");
    return cookie;
  }

  protected String defaultIfNull(String string, String defaultString) {
    return string == null ? defaultString : string;
  }

  protected void deleteCookie(String name) {
    Cookie cookie = new Cookie(name, null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  protected Cookie getCookie(String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(cookieName)) {
          return cookie;
        }
      }
    }

    return null;
  }
}

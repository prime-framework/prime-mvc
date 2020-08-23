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
package org.primeframework.mvc.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Daniel DeGroff
 */
public class DefaultLoginSecurityContextCookieConfig implements CookieConfig {
  @Override
  public void add(HttpServletRequest request, HttpServletResponse response, String value) {
    Cookie cookie = new Cookie(name(), value);
    cookie.setSecure("https".equals(defaultIfNull(request.getHeader("X-Forwarded-Proto"), request.getScheme()).toLowerCase()));
    cookie.setHttpOnly(true);
    // Add persistent cookie
    cookie.setMaxAge(Integer.MAX_VALUE);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  @Override
  public void delete(HttpServletRequest request, HttpServletResponse response) {
    Cookie cookie = new Cookie(name(), null);
    cookie.setSecure("https".equals(defaultIfNull(request.getHeader("X-Forwarded-Proto"), request.getScheme()).toLowerCase()));
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
    // Ensure we do not return the cookie again after it has been deleted within the same request.
    request.setAttribute(name() + "Deleted", true);
  }

  @Override
  public String get(HttpServletRequest request) {
    // Ensure we do not return the cookie again after it has been deleted within the same request.
    if (request.getAttribute(name() + "Deleted") != null) {
      return null;
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(name())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }

  @Override
  public String name() {
    return "prime-session";
  }

  private String defaultIfNull(String string, String defaultString) {
    return string == null ? defaultString : string;
  }
}

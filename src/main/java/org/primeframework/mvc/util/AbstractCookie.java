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

import org.primeframework.mvc.http.Cookie;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;

/**
 * @author Daniel DeGroff
 */
public abstract class AbstractCookie {
  protected final HTTPRequest request;

  protected final HTTPResponse response;

  protected AbstractCookie(HTTPRequest request, HTTPResponse response) {
    this.request = request;
    this.response = response;
  }

  protected void addSecureHTTPOnlyCookie(String name, String value, Long maxAge) {
    // Delete any existing version of the cookie and add the new version
    response.removeCookie(name);

    Cookie cookie = buildSecureHTTPOnlyCookie(name, value, maxAge);
    response.addCookie(cookie);
  }

  protected void addSecureHTTPOnlySessionCookie(String name, String value) {
    // Delete any existing version of the cookie and add the new version
    response.removeCookie(name);

    Cookie cookie = buildSecureHTTPOnlyCookie(name, value, null);
    response.addCookie(cookie);
  }

  protected void deleteCookie(String name) {
    // Delete any existing version of the cookie and add the new version with a 0 MaxAge
    response.removeCookie(name);

    Cookie cookie = new Cookie(name, null);
    cookie.maxAge = 0L;
    cookie.path = "/";
    response.addCookie(cookie);
  }

  protected Cookie getCookie(String cookieName) {
    return request.getCookie(cookieName);
  }

  private Cookie buildSecureHTTPOnlyCookie(String name, String value, Long maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.secure = "https".equalsIgnoreCase(defaultIfNull(request.getHeader("X-Forwarded-Proto"), request.getScheme()));
    cookie.httpOnly = true;
    cookie.maxAge = maxAge;
    cookie.path = "/";
    return cookie;
  }

  private String defaultIfNull(String string, String defaultString) {
    return string == null ? defaultString : string;
  }
}

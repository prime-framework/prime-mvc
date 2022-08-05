/*
 * Copyright (c) 2020-2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.scope;

import java.lang.annotation.Annotation;

import org.primeframework.mvc.http.Cookie;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;

/**
 * @author Daniel DeGroff
 */
public abstract class AbstractCookieScope<T extends Annotation> implements Scope<T> {
  protected final HTTPRequest request;

  protected final HTTPResponse response;

  protected AbstractCookieScope(HTTPRequest request, HTTPResponse response) {
    this.request = request;
    this.response = response;
  }

  @Override
  public Object get(String fieldName, Class<?> type, T scope) {
    String cookieName = getCookieName(fieldName, scope);
    Cookie cookie = request.getCookie(cookieName);
    return processCookie(cookie, fieldName, type, scope);
  }

  @Override
  public void set(String fieldName, Object value, T scope) {
    Cookie cookie = buildCookie(fieldName, value, scope);
    if (cookie != null) {
      cookie.httpOnly = true;
      cookie.path = "/";
      cookie.secure = "https".equalsIgnoreCase(request.getScheme());
      response.addCookie(cookie);
    } else {
      // If it wasn't in the request, then we don't need to delete it
      String cookieName = getCookieName(fieldName, scope);
      if (request.getCookie(cookieName) == null) {
        return;
      }

      // Delete any existing version of the cookie and add the new version with a 0 MaxAge
      response.removeCookie(cookieName);

      cookie = new Cookie(cookieName, null);
      cookie.maxAge = 0L;
      cookie.path = "/";
      response.addCookie(cookie);
    }
  }

  protected abstract Cookie buildCookie(String fieldName, Object value, T scope);

  protected abstract String getCookieName(String fieldName, T scope);

  protected abstract Object processCookie(Cookie cookie, String fieldName, Class<?> type, T scope);
}

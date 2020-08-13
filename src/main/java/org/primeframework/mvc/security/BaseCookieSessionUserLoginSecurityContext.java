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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.primeframework.mvc.config.MVCConfiguration;

/**
 * Uses a cookie to manage the user session.
 *
 * @author Daniel DeGroff
 */
@SuppressWarnings("unused")
public abstract class BaseCookieSessionUserLoginSecurityContext implements UserLoginSecurityContext {
  private final MVCConfiguration configuration;

  private final HttpServletRequest request;

  private final HttpServletResponse response;

  protected BaseCookieSessionUserLoginSecurityContext(MVCConfiguration configuration, HttpServletRequest request,
                                                      HttpServletResponse response) {
    this.configuration = configuration;
    this.request = request;
    this.response = response;
  }

  @Override
  public Object getCurrentUser() {
    String sessionId = getSessionId();
    return getUserFromSessionId(sessionId);
  }

  @Override
  public String getSessionId() {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(configuration.userLoginSecurityContextCookieName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }

  @Override
  public boolean isLoggedIn() {
    return getCurrentUser() != null;
  }

  @Override
  public void login(Object user) {
    Object sessionId = getSessionIdFromUser(user);
    if (sessionId == null) {
      return;
    }

    Cookie cookie = new Cookie(configuration.userLoginSecurityContextCookieName(), (String) sessionId);
    cookie.setSecure("https".equals(defaultIfNull(request.getHeader("X-Forwarded-Proto"), request.getScheme()).toLowerCase()));
    cookie.setHttpOnly(true);
    cookie.setMaxAge(-1);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  @Override
  public void logout() {
    Cookie cookie = new Cookie(configuration.userLoginSecurityContextCookieName(), null);
    cookie.setSecure("https".equals(defaultIfNull(request.getHeader("X-Forwarded-Proto"), request.getScheme()).toLowerCase()));
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  protected abstract Object getSessionIdFromUser(Object user);

  protected abstract Object getUserFromSessionId(Object id);

  private String defaultIfNull(String string, String defaultString) {
    return string == null ? defaultString : string;
  }
}

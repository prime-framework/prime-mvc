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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Uses a cookie to manage the user session.
 *
 * @author Daniel DeGroff
 */
@SuppressWarnings("unused")
public abstract class BaseCookieSessionUserLoginSecurityContext implements UserLoginSecurityContext {
  protected final HttpServletRequest request;

  protected final HttpServletResponse response;

  private final CookieConfig cookie;

  protected BaseCookieSessionUserLoginSecurityContext(CookieConfig cookie, HttpServletRequest request,
                                                      HttpServletResponse response) {
    this.cookie = cookie;
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
    return cookie.get(request);
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

    cookie.add(request, response, (String) sessionId);
  }

  @Override
  public void logout() {
    cookie.delete(request, response);
  }

  /**
   * Return the sessionId for the provider user.
   *
   * @param user the user
   * @return the session Id or null if no session is found for this user.
   */
  protected abstract Object getSessionIdFromUser(Object user);

  /**
   * Return the User for the given sessionId
   *
   * @param sessionId the sessionId
   * @return the user or null if no user was found.
   */
  protected abstract Object getUserFromSessionId(Object sessionId);

  private String defaultIfNull(String string, String defaultString) {
    return string == null ? defaultString : string;
  }
}

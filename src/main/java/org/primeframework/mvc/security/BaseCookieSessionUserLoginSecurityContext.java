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

import org.primeframework.mvc.http.Cookie.SameSite;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;

/**
 * Uses a cookie to manage the user session.
 *
 * @author Daniel DeGroff
 */
public abstract class BaseCookieSessionUserLoginSecurityContext implements UserLoginSecurityContext {
  protected final CookieProxy cookie;

  protected final HTTPRequest request;

  protected final HTTPResponse response;

  protected BaseCookieSessionUserLoginSecurityContext(HTTPRequest request, HTTPResponse response) {
    this.request = request;
    this.response = response;
    this.cookie = new CookieProxy(cookieName(), cookieDuration(), SameSite.Strict);
  }

  @Override
  public Object getCurrentUser() {
    String sessionId = getSessionId();
    if (sessionId == null) {
      return null;
    }

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
    String sessionId = getSessionIdFromUser(user);
    if (sessionId == null) {
      return;
    }

    cookie.add(request, response, sessionId);
  }

  @Override
  public void logout() {
    cookie.delete(request, response);
  }

  /**
   * Allows subclasses to specify a cookie duration. If null is returned, then the cookie is a session cookie.
   *
   * @return The duration of the cookie.
   */
  protected abstract Long cookieDuration();

  /**
   * Allows subclasses to specify the name of the cookie used by this security context to store the session id.
   *
   * @return The cookie name.
   */
  protected abstract String cookieName();

  /**
   * Return the session id for the provider user.
   *
   * @param user The application specific user object.
   * @return The session id or null if no session is found for this user.
   */
  protected abstract String getSessionIdFromUser(Object user);

  /**
   * Return the User for the given session id.
   *
   * @param sessionId The session id that was previously generated for a user.
   * @return The application specific user object based on the session id or null if no user was found.
   */
  protected abstract Object getUserFromSessionId(String sessionId);
}

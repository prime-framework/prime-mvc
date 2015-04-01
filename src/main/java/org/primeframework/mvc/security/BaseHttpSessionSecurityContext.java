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
import javax.servlet.http.HttpSession;

import com.google.inject.Inject;

/**
 * Uses the HttpSession object to store the user.
 *
 * @author Brian Pontarelli
 */
public abstract class BaseHttpSessionSecurityContext implements SecurityContext {
  public static final String USER_SESSION_KEY = "prime-mvc-security-user";

  private final HttpServletRequest request;

  @Inject
  public BaseHttpSessionSecurityContext(HttpServletRequest request) {
    this.request = request;
  }

  @Override
  public Object getCurrentUser() {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }

    return session.getAttribute(USER_SESSION_KEY);
  }

  @Override
  public boolean isLoggedIn() {
    return getCurrentUser() != null;
  }

  @Override
  public void login(Object user) {
    HttpSession session = request.getSession();
    if (session == null) {
      throw new IllegalStateException("Unable to create session");
    }

    session.setAttribute(USER_SESSION_KEY, user);
  }
}

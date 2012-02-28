/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.primeframework.mvc.scope.annotation.Session;

import com.google.inject.Inject;

/**
 * <p> This is the request scope which fetches and stores values in the HttpSession. </p>
 *
 * @author Brian Pontarelli
 */
public class SessionScope implements Scope<Session> {
  private final HttpServletRequest request;

  @Inject
  public SessionScope(HttpServletRequest request) {
    this.request = request;
  }

  /**
   * {@inheritDoc}
   */
  public Object get(String fieldName, Session scope) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      String key = scope.value().equals("##field-name##") ? fieldName : scope.value();
      return session.getAttribute(key);
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void set(String fieldName, Object value, Session scope) {
    HttpSession session;
    if (value != null) {
      session = request.getSession(true);
    } else {
      session = request.getSession(false);
    }

    if (session == null) {
      return;
    }

    String key = scope.value().equals("##field-name##") ? fieldName : scope.value();
    if (value != null) {
      session.setAttribute(key, value);
    } else {
      session.removeAttribute(key);
    }
  }
}

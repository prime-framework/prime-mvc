/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.AbstractCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the request scope which fetches and stores values in a cookie.
 *
 * @author Daniel DeGroff
 */
public class ManagedCookieScope extends AbstractCookie implements Scope<ManagedCookie> {
  private static final Logger logger = LoggerFactory.getLogger(ManagedCookieScope.class);

  private final Encryptor encryptor;

  @Inject
  public ManagedCookieScope(HttpServletRequest request, HttpServletResponse response, Encryptor encryptor) {
    super(request, response);
    this.encryptor = encryptor;
  }

  /**
   * {@inheritDoc}
   */
  public Object get(String fieldName, ManagedCookie scope) {
    throw new UnsupportedOperationException("The ManagedCookieScope cannot be called with this method.");
  }

  @Override
  public Cookie get(String fieldName, Class<?> type, ManagedCookie scope) {
    String cookieName = getCookieName(fieldName, scope);

    javax.servlet.http.Cookie cookie = getCookie(cookieName);
    String value = cookie != null ? cookie.getValue() : null;
    if (value == null || "".equals(value)) {
      return new Cookie(cookieName, null);
    }

    try {
      String cookieValue = scope.encrypt()
          ? encryptor.decrypt(String.class, value)
          : value;
      return new Cookie(cookieName, cookieValue);
    } catch (Exception e) {
      String message = e.getClass().getCanonicalName() + " " + e.getMessage();
      if (scope.encrypt()) {
        logger.warn("Failed to decrypt cookie. This may be expected if the cookie was encrypted using a different key.\n\tCause: " + message);
      } else {
        logger.warn("Failed to decode cookie. This is not expected.\n\tCause: " + message);
      }
    }

    return new Cookie(cookieName, null);
  }

  /**
   * {@inheritDoc}
   */
  public void set(String fieldName, Object value, ManagedCookie scope) {
    String cookieName = getCookieName(fieldName, scope);
    javax.servlet.http.Cookie existing = getCookie(cookieName);

    Cookie actual = (Cookie) value;

    // If the value is null, delete it if it was previously non-null
    if (actual == null || actual.getValue() == null) {
      if (existing != null) {
        // Delete it if we had it.
        deleteCookie(cookieName);
      }

      return;
    }

    String encoded;
    try {
      encoded = scope.encrypt()
          ? encryptor.encrypt(actual.getValue())
          : actual.getValue();
    } catch (Exception e) {
      throw new ErrorException("error", e);
    }

    addSecureHttpOnlyCookie(cookieName, encoded, scope.maxAge());
  }

  /**
   * Using the annotation or the current action invocation, this determines the name of the action used to get the
   * action session.
   *
   * @param fieldName the field name
   * @param scope     The scope annotation.
   * @return The action class name.
   */
  protected String getCookieName(String fieldName, ManagedCookie scope) {
    return "##field-name##".equals(scope.value()) ? fieldName : scope.value();
  }
}

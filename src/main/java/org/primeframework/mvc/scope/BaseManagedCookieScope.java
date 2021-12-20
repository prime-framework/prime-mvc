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

import java.lang.annotation.Annotation;

import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.http.Cookie;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.AbstractCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the request scope which fetches and stores values in a cookie.
 *
 * @author Daniel DeGroff
 */
public abstract class BaseManagedCookieScope<T extends Annotation> extends AbstractCookie implements Scope<T> {
  private static final Logger logger = LoggerFactory.getLogger(BaseManagedCookieScope.class);

  protected final Encryptor encryptor;

  protected BaseManagedCookieScope(HTTPRequest request, HTTPResponse response, Encryptor encryptor) {
    super(request, response);
    this.encryptor = encryptor;
  }

  /**
   * {@inheritDoc}
   */
  public Object get(String fieldName, T scope) {
    throw new UnsupportedOperationException("The ManagedCookieScope cannot be called with this method.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Cookie get(String fieldName, Class<?> type, T scope) {
    boolean neverNull = neverNull(scope);
    boolean encrypt = encrypt(scope);

    String cookieName = getCookieName(fieldName, scope);
    Cookie cookie = getCookie(cookieName);
    String value = cookie != null ? cookie.value : null;
    if (value == null || "".equals(value)) {
      return cookie != null ? cookie : (neverNull ? new Cookie(cookieName, null) : null);
    }

    try {
      cookie.value = encrypt ? encryptor.decrypt(String.class, value) : value;
      return cookie;
    } catch (Exception e) {
      String message = e.getClass().getCanonicalName() + " " + e.getMessage();
      if (encrypt) {
        logger.warn("Failed to decrypt cookie. This may be expected if the cookie was encrypted using a different key.\n\tCause: " + message);
      } else {
        logger.warn("Failed to decode cookie. This is not expected.\n\tCause: " + message);
      }
    }

    return neverNull ? cookie : null;
  }

  /**
   * {@inheritDoc}
   */
  public void set(String fieldName, Object value, T scope) {
    boolean encrypt = encrypt(scope);

    String cookieName = getCookieName(fieldName, scope);
    Cookie existing = getCookie(cookieName);
    Cookie actual = (Cookie) value;

    // If the value is null, delete it if it was previously non-null
    if (actual == null || actual.value == null) {
      if (existing != null) {
        // Delete it if we had it.
        deleteCookie(cookieName);
      }

      return;
    }

    String encoded;
    try {
      encoded = encrypt ? encryptor.encrypt(actual.value) : actual.value;
    } catch (Exception e) {
      throw new ErrorException("error", e);
    }

    addCookie(cookieName, encoded, scope);
  }

  /**
   * Adds the cookie in a scope specific way.
   *
   * @param name  The name of the cookie.
   * @param value The value of the cookie.
   * @param scope The scope annotation.
   */
  protected abstract void addCookie(String name, String value, T scope);

  /**
   * Using the annotation, determines if the cookie should be encrypted.
   *
   * @param scope The scope annotation.
   * @return True or false based on the annotation.
   */
  protected abstract boolean encrypt(T scope);

  /**
   * Using the annotation or the current action invocation, this determines the name of the action used to get the
   * action session.
   *
   * @param fieldName the field name
   * @param scope     The scope annotation.
   * @return The action class name.
   */
  protected abstract String getCookieName(String fieldName, T scope);

  /**
   * Using the annotation, determines if the cookie is allowed to be null.
   *
   * @param scope The scope annotation.
   * @return True or false based on the annotation.
   */
  protected abstract boolean neverNull(T scope);
}

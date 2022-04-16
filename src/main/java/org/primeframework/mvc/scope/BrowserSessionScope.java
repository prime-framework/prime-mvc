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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.http.Cookie;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.scope.annotation.BrowserSession;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.AbstractCookie;
import org.primeframework.mvc.util.CookieTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the session scope which fetches and stores objects as JSON in a cookie.
 *
 * @author Brian Pontarelli
 */
public class BrowserSessionScope extends AbstractCookie implements Scope<BrowserSession> {
  private static final Logger logger = LoggerFactory.getLogger(BrowserSessionScope.class);

  private final Encryptor encryptor;

  private final ObjectMapper objectMapper;

  @Inject
  public BrowserSessionScope(HTTPRequest request, HTTPResponse response, Encryptor encryptor,
                             ObjectMapper objectMapper) {
    super(request, response);
    this.encryptor = encryptor;
    this.objectMapper = objectMapper;
  }

  /**
   * {@inheritDoc}
   */
  public Object get(String fieldName, BrowserSession scope) {
    throw new UnsupportedOperationException("The ManagedCookieScope cannot be called with this method.");
  }

  @Override
  public Object get(String fieldName, Class<?> type, BrowserSession scope) {
    String cookieName = getCookieName(fieldName, scope);
    Cookie cookie = getCookie(cookieName);
    String value = cookie != null ? cookie.value : null;
    if (value == null || "".equals(value)) {
      return null;
    }

    try {
      return CookieTools.fromJSONCookie(value, type, scope.encrypt(), encryptor, objectMapper);
    } catch (Exception e) {
      String message = e.getClass().getCanonicalName() + " " + e.getMessage();
      if (scope.encrypt()) {
        logger.warn("Failed to decrypt cookie. This may be expected if the cookie was encrypted using a different key.\n\tCause: " + message);
      } else {
        logger.warn("Failed to decode cookie. This is not expected.\n\tCause: " + message);
      }

      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void set(String fieldName, Object value, BrowserSession scope) {
    String cookieName = getCookieName(fieldName, scope);
    Cookie existing = getCookie(cookieName);

    // If the value is null, delete it if it was previously non-null
    if (value == null) {
      if (existing != null) {
        // Delete it if we had it.
        deleteCookie(cookieName);
      }

      return;
    }

    try {
      String cookieValue = CookieTools.toJSONCookie(value, scope.compress(), scope.encrypt(), encryptor, objectMapper);
      addSecureHTTPOnlyCookie(cookieName, cookieValue, scope.maxAge());
    } catch (Exception e) {
      throw new ErrorException("error", e);
    }
  }

  /**
   * Using the annotation or the current action invocation, this determines the name of the action used to get the
   * action session.
   *
   * @param fieldName the field name
   * @param scope     The scope annotation.
   * @return The action class name.
   */
  protected String getCookieName(String fieldName, BrowserSession scope) {
    return "##field-name##".equals(scope.value()) ? fieldName : scope.value();
  }
}

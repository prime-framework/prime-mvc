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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.CookieTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This scope is the base class for browser session cookies.
 *
 * @author Brian Pontarelli
 */
public abstract class BaseBrowserSessionScope<T extends Annotation> extends AbstractCookieScope<T> {
  private static final Logger logger = LoggerFactory.getLogger(BaseBrowserSessionScope.class);

  protected final Encryptor encryptor;

  protected final ObjectMapper objectMapper;

  protected BaseBrowserSessionScope(HTTPRequest request, HTTPResponse response, Encryptor encryptor,
                                    ObjectMapper objectMapper) {
    super(request, response);
    this.encryptor = encryptor;
    this.objectMapper = objectMapper;
  }

  @Override
  protected Cookie buildCookie(String fieldName, Object value, T scope) {
    // If the value is null, return null and AbstractCookieScope will determine if the cookie should be deleted
    if (value == null) {
      return null;
    }

    boolean compress = compress(scope);
    boolean encrypt = encrypt(scope);
    try {
      String cookieValue = CookieTools.toJSONCookie(value, compress, encrypt, encryptor, objectMapper);
      Cookie cookie = new Cookie(getCookieName(fieldName, scope), cookieValue);
      setCookieValues(cookie, scope);
      return cookie;
    } catch (Exception e) {
      throw new ErrorException("error", e);
    }
  }

  /**
   * User the annotation or the current action invocation, this determines if the cookie should be compressed or not.
   *
   * @param scope The scope annotation.
   * @return True or false based on the annotation.
   */
  protected abstract boolean compress(T scope);

  /**
   * Using the annotation, determines if the cookie should be encrypted.
   *
   * @param scope The scope annotation.
   * @return True or false based on the annotation.
   */
  protected abstract boolean encrypt(T scope);

  @Override
  protected Object processCookie(Cookie cookie, String fieldName, Class<?> type, T scope) {
    String value = cookie != null ? cookie.value : null;
    if (value == null || "".equals(value)) {
      return null;
    }

    boolean encrypt = encrypt(scope);
    try {
      return CookieTools.fromJSONCookie(value, type, encrypt, encryptor, objectMapper);
    } catch (Exception e) {
      String message = e.getClass().getCanonicalName() + " " + e.getMessage();
      if (encrypt) {
        logger.warn("Failed to decrypt cookie. This may be expected if the cookie was encrypted using a different key.\n\tCause: {}", message);
      } else {
        logger.warn("Failed to decode cookie. This is not expected.\n\tCause: {}", message);
      }
    }

    return null;
  }

  /**
   * Allows subclasses to set additional cookie values before it is sent back to the user-agent.
   *
   * @param cookie The cookie.
   * @param scope  The scope.
   */
  protected abstract void setCookieValues(Cookie cookie, T scope);
}

/*
 * Copyright (c) 2021-2025, Inversoft Inc., All Rights Reserved
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
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.CookieTools;
import org.primeframework.mvc.util.ThrowingFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the request scope which fetches and stores values in a cookie.
 *
 * @author Daniel DeGroff
 */
public abstract class BaseManagedCookieScope<T extends Annotation> extends AbstractCookieScope<T> {
  private static final Logger logger = LoggerFactory.getLogger(BaseManagedCookieScope.class);

  protected final Encryptor encryptor;

  protected final ObjectMapper objectMapper;

  protected BaseManagedCookieScope(HTTPRequest request, HTTPResponse response, Encryptor encryptor,
                                   ObjectMapper objectMapper) {
    super(request, response);
    this.encryptor = encryptor;
    this.objectMapper = objectMapper;
  }

  @Override
  protected Cookie buildCookie(String fieldName, Object value, T scope) {
    // Make a copy so that we aren't impacting the request cookie, but instead using a new response cookie object
    Cookie cookie = new Cookie((Cookie) value);
    boolean compress = compress(scope);
    boolean encrypt = encrypt(scope);
    String cookieValue = cookie.value;

    // If the cookie is empty or null, return null and AbstractCookieScope will determine if the cookie should be deleted
    if (value == null || cookie.value == null) {
      return null;
    }

    byte[] result = cookieValue.getBytes(StandardCharsets.UTF_8);
    try {
      cookie.value = CookieTools.toCookie(result, compress, encrypt, encryptor);
    } catch (Exception e) {
      throw new ErrorException("error", e);
    }

    setCookieValues(cookie, scope);
    return cookie;
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

  /**
   * Using the annotation, determines if the cookie is allowed to be null.
   *
   * @param scope The scope annotation.
   * @return True or false based on the annotation.
   */
  protected abstract boolean neverNull(T scope);

  @Override
  protected Cookie processCookie(Cookie cookie, String fieldName, Class<?> type, T scope) {
    boolean compress = compress(scope);
    boolean encrypt = encrypt(scope);
    boolean neverNull = neverNull(scope);

    String cookieName = getCookieName(fieldName, scope);
    String cookieValue = cookie != null ? cookie.value : null;
    if (cookieValue == null || "".equals(cookieValue)) {
      return cookie != null ? cookie : (neverNull ? new Cookie(cookieName, null) : null);
    }

    try {
      // Note: If the cookie value begins with valid JSON followed by a newline character (\n, 0x0a, 10), oldFunction will return the
      //  JSON string and truncate the newline onward. Starting in 4.30.0 all cookies are written with header bytes to avoid this issue.
      ThrowingFunction<byte[], String> oldFunction = r -> objectMapper.readerFor(String.class).readValue(r);
      ThrowingFunction<byte[], String> newFunction = r -> new String(r, StandardCharsets.UTF_8);
      if (compress || encrypt) {
        // If a cookie meant to be compressed or encrypted according to the annotation, the processing must succeed.
        cookie.value = CookieTools.fromCookie(cookieValue, encrypt, encrypt, encryptor, oldFunction, newFunction);
      } else {
        try {
          // If a managed cookie is not compressed or encrypted, attempt to parse. If parsing fails (Exception), assume a legacy cookie that had the proper value in it.
          cookie.value = CookieTools.fromCookie(cookieValue, false, false, encryptor, oldFunction, newFunction);
        } catch (Throwable t) {
          // Smother because the cookie already has the value in it
        }
      }

      return cookie;
    } catch (Exception e) {
      String message = e.getClass().getCanonicalName() + " " + e.getMessage();
      if (encrypt) {
        logger.debug("Failed to decrypt cookie. This may be expected if the cookie was encrypted using a different key.\n\tCause: " + message);
      } else {
        logger.debug("Failed to decode cookie. This is not expected.\n\tCause: " + message);
      }
      // if we had an encryption or decoding problem, we should not keep a cookie value around at all
      cookie.value = null;
    }

    return neverNull ? cookie : null;
  }

  /**
   * Allows subclasses to set additional cookie values before it is sent back to the user-agent.
   *
   * @param cookie The cookie.
   * @param scope  The scope.
   */
  protected abstract void setCookieValues(Cookie cookie, T scope);
}

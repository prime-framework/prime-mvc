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
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.scope.annotation.BasicCookie;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.AbstractCookie;
import org.primeframework.mvc.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.primeframework.mvc.servlet.PrimeServletContextListener.GUICE_INJECTOR_KEY;

/**
 * This is the request scope which fetches and stores values in a cookie.
 *
 * @author Daniel DeGroff
 */
public class BasicCookieScope extends AbstractCookie implements Scope<BasicCookie> {
  private static final Logger logger = LoggerFactory.getLogger(BasicCookieScope.class);

  private final Encryptor encryptor;

  private final ObjectMapper objectMapper;

  @Inject
  public BasicCookieScope(HttpServletRequest request, HttpServletResponse response, Encryptor encryptor,
                          ObjectMapper objectMapper) {
    super(request, response);
    this.encryptor = encryptor;
    this.objectMapper = objectMapper;
  }

  /**
   * Helper method to retrieve a value from an Basic Cookie prior to the normal MVC workflow.
   *
   * @param request   the HTTP servlet request
   * @param fieldName the field name
   * @param type      the type of the value to be returned
   * @param action    the action class
   * @param <T>       the type of the value to be returned
   * @return the value or null if the value is not found.
   */
  public static <T> T get(HttpServletRequest request, String fieldName, Class<T> type, Class<?> action) {

    List<Field> fields = ReflectionUtils.findAllFieldsWithAnnotation(action, BasicCookie.class);
    Field actual = null;
    for (Field field : fields) {
      if (field.getName().equals(fieldName)) {
        actual = field;
      }
    }

    if (actual == null) {
      return null;
    }

    BasicCookie scope = actual.getDeclaredAnnotation(BasicCookie.class);
    boolean encrypted = scope.encrypt();

    String cookieName = action.getName() + "$" + fieldName;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(cookieName)) {
          String value = cookie.getValue();
          try {
            Injector injector = (Injector) request.getServletContext().getAttribute(GUICE_INJECTOR_KEY);
            if (encrypted) {
              Encryptor encryptor = injector.getInstance(Encryptor.class);
              return encryptor.decrypt(type, value);
            } else {
              byte[] bytes = Base64.getUrlDecoder().decode(value);
              ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
              return objectMapper.readerFor(type).readValue(bytes);
            }

          } catch (Exception e) {
            String message = e.getClass().getCanonicalName() + " " + e.getMessage();
            if (scope.encrypt()) {
              logger.warn("Failed to decrypt cookie. This may be expected if the cookie was encrypted using a different key.\n\tCause: " + message);
            } else {
              logger.warn("Failed to decode cookie. This is not expected.\n\tCause: " + message);
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Object get(String fieldName, BasicCookie scope) {
    throw new UnsupportedOperationException("The BasicCookieScope cannot be called with this method.");
  }

  @Override
  public Object get(String fieldName, Class<?> type, BasicCookie scope) {
    String cookieName = getCookieName(fieldName, scope);
    String value = getCookieValue(cookieName);
    if (value == null) {
      return null;
    }

    try {
      return scope.encrypt()
          ? encryptor.decrypt(type, value)
          : decode(type, value, scope);
    } catch (Exception e) {
      String message = e.getClass().getCanonicalName() + " " + e.getMessage();
      if (scope.encrypt()) {
        logger.warn("Failed to decrypt cookie. This may be expected if the cookie was encrypted using a different key.\n\tCause: " + message);
      } else {
        logger.warn("Failed to decode cookie. This is not expected.\n\tCause: " + message);
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void set(String fieldName, Object value, BasicCookie scope) {
    String cookieName = getCookieName(fieldName, scope);
    String existingValue = getCookieValue(cookieName);

    // If the value is null, delete it if it was previously non-null
    if (value == null) {
      if (existingValue != null) {
        // Delete it if we had it.
        deleteCookie(cookieName);
      }

      return;
    }

    String encoded;
    try {
      encoded = scope.encrypt()
          ? encryptor.encrypt(value)
          : encode(value, scope);
    } catch (Exception e) {
      throw new ErrorException("error", e);
    }

    addSecureHttpOnlySessionCookie(cookieName, encoded);
  }

  /**
   * Using the annotation or the current action invocation, this determines the name of the action used to get the
   * action session.
   *
   * @param fieldName the field name
   * @param scope     The scope annotation.
   * @return The action class name.
   */
  protected String getCookieName(String fieldName, BasicCookie scope) {
    return "##field-name##" .equals(scope.value()) ? fieldName : scope.value();
  }

  private String decode(Class<?> type, String value, BasicCookie scope) throws IOException {
    if (type.equals(String.class)) {
      return scope.encodeStrings()
          ? objectMapper.readerFor(type).readValue(Base64.getUrlDecoder().decode(value))
          : value;
    }

    return objectMapper.readerFor(type).readValue(Base64.getUrlDecoder().decode(value));
  }

  private String encode(Object value, BasicCookie scope) throws JsonProcessingException {
    if (value instanceof String s) {
      return scope.encodeStrings()
          ? Base64.getUrlEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8))
          : s;
    }

    return Base64.getUrlEncoder().encodeToString(objectMapper.writeValueAsBytes(value));
  }

  private String getCookieValue(String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(cookieName)) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }
}

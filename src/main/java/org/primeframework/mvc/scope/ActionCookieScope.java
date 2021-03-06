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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.scope.annotation.ActionCookie;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.AbstractCookie;
import org.primeframework.mvc.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.primeframework.mvc.servlet.PrimeServletContextListener.GUICE_INJECTOR_KEY;

/**
 * This is the request scope which fetches and stores values in a cookie, but those values are associated with a
 * specific action.
 *
 * @author Daniel DeGroff
 */
public class ActionCookieScope extends AbstractCookie implements Scope<ActionCookie> {
  private static final Logger logger = LoggerFactory.getLogger(ActionCookieScope.class);

  private final ActionInvocationStore actionInvocationStore;

  private final Encryptor encryptor;

  private final ObjectMapper objectMapper;

  @Inject
  public ActionCookieScope(HttpServletRequest request, HttpServletResponse response,
                           ActionInvocationStore actionInvocationStore, Encryptor encryptor,
                           ObjectMapper objectMapper) {
    super(request, response);
    this.actionInvocationStore = actionInvocationStore;
    this.encryptor = encryptor;
    this.objectMapper = objectMapper;
  }

  /**
   * Helper method to retrieve a value from an Action Cookie prior to the normal MVC workflow.
   *
   * @param request   the HTTP servlet request
   * @param fieldName the field name
   * @param type      the type of the value to be returned
   * @param action    the action class
   * @param <T>       the type of the value to be returned
   * @return the value or null if the value is not found.
   */
  public static <T> T get(HttpServletRequest request, String fieldName, Class<T> type, Class<?> action) {

    List<Field> fields = ReflectionUtils.findAllFieldsWithAnnotation(action, ActionCookie.class);
    Field actual = null;
    for (Field field : fields) {
      if (field.getName().equals(fieldName)) {
        actual = field;
      }
    }

    if (actual == null) {
      return null;
    }

    ActionCookie scope = actual.getDeclaredAnnotation(ActionCookie.class);
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
  public Object get(String fieldName, ActionCookie scope) {
    throw new UnsupportedOperationException("The ActionCookieScope cannot be called with this method.");
  }

  @Override
  public Object get(String fieldName, Class<?> type, ActionCookie scope) {
    String cookieName = getCookieName(fieldName, scope);
    String value = getCookieValue(cookieName);
    if (value == null) {
      return null;
    }

    try {
      return scope.encrypt() ? encryptor.decrypt(type, value) : decode(type, value);
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
  public void set(String fieldName, Object value, ActionCookie scope) {
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
      encoded = scope.encrypt() ? encryptor.encrypt(value) : encode(value);
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
  protected String getCookieName(String fieldName, ActionCookie scope) {
    String className;
    if (scope.action() != ActionCookie.class) {
      className = scope.action().getName();
    } else {
      ActionInvocation ai = actionInvocationStore.getCurrent();
      if (ai.action == null) {
        throw new PrimeException("Attempting to store a value in the action session but the current request URL isn'" +
            "t associated with an action class");
      }
      className = ai.action.getClass().getName();
    }

    return className + "$" + ("##field-name##".equals(scope.value()) ? fieldName : scope.value());
  }

  private String decode(Class<?> type, String value) throws IOException {
    byte[] bytes = Base64.getUrlDecoder().decode(value);
    return objectMapper.readerFor(type).readValue(bytes);
  }

  private String encode(Object value) throws JsonProcessingException {
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

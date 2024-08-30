/*
 * Copyright (c) 2001-2024, Inversoft Inc., All Rights Reserved
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

import java.lang.reflect.Field;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.scope.annotation.BrowserActionSession;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.CookieTools;
import org.primeframework.mvc.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the action session scope which fetches and stores values in a cookie, but those values are associated with a specific action.
 *
 * @author Daniel DeGroff
 */
public class BrowserActionSessionScope extends BaseBrowserSessionScope<BrowserActionSession> {
  private static final Logger logger = LoggerFactory.getLogger(BrowserActionSessionScope.class);

  private final ActionInvocationStore actionInvocationStore;

  @Inject
  public BrowserActionSessionScope(HTTPRequest request, HTTPResponse response,
                                   ActionInvocationStore actionInvocationStore,
                                   Encryptor encryptor, ObjectMapper objectMapper) {
    super(request, response, encryptor, objectMapper);
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * Helper method to retrieve a value from an Action Cookie prior to the normal MVC workflow.
   *
   * @param injector  The injector to use to create the Encryptor for the cookie values.
   * @param request   the HTTP servlet request
   * @param fieldName the field name
   * @param type      the type of the value to be returned
   * @param action    the action class
   * @param <T>       the type of the value to be returned
   * @return the value or null if the value is not found.
   */
  public static <T> T get(Injector injector, HTTPRequest request, String fieldName, Class<T> type, Class<?> action) {
    List<Field> fields = ReflectionUtils.findAllFieldsWithAnnotation(action, BrowserActionSession.class);
    Field actual = null;
    for (Field field : fields) {
      if (field.getName().equals(fieldName)) {
        actual = field;
      }
    }

    if (actual == null) {
      return null;
    }

    BrowserActionSession scope = actual.getDeclaredAnnotation(BrowserActionSession.class);
    boolean encrypted = scope.encrypt();

    String cookieName = action.getName() + "$" + fieldName;
    Cookie cookie = request.getCookie(cookieName);
    if (cookie == null) {
      return null;
    }

    String value = cookie.value;
    try {
      Encryptor encryptor = injector.getInstance(Encryptor.class);
      ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
      return CookieTools.fromJSONCookie(value, type, encrypted, encryptor, objectMapper);
    } catch (Exception e) {
      String message = e.getClass().getCanonicalName() + " " + e.getMessage();
      if (scope.encrypt()) {
        logger.debug("Failed to decrypt cookie. This may be expected if the cookie was encrypted using a different key.\n\tCause: {}", message);
      } else {
        logger.debug("Failed to decode cookie. This is not expected.\n\tCause: {}", message);
      }
    }

    return null;
  }

  @Override
  protected boolean compress(BrowserActionSession scope) {
    return scope.compress();
  }

  @Override
  protected boolean encrypt(BrowserActionSession scope) {
    return scope.encrypt();
  }

  /**
   * Using the annotation or the current action invocation, this determines the name of the action used to get the action session.
   *
   * @param fieldName the field name
   * @param scope     The scope annotation.
   * @return The action class name.
   */
  @Override
  protected String getCookieName(String fieldName, BrowserActionSession scope) {
    String className;
    if (scope.action() != BrowserActionSession.class) {
      className = scope.action().getName();
    } else {
      ActionInvocation ai = actionInvocationStore.getCurrent();
      if (ai.action == null) {
        throw new PrimeException("Attempting to store a value in the action session but the current request URL isn'" +
                                 "t associated with an action class");
      }
      className = ai.action.getClass().getName();
    }

    return className + "$" + ("##field-name##".equals(scope.name()) ? fieldName : scope.name());
  }

  @Override
  protected void setCookieValues(Cookie cookie, BrowserActionSession scope) {
    cookie.sameSite = scope.sameSite();
  }
}

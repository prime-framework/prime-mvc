/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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

import javax.crypto.BadPaddingException;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.util.CookieTools;

/**
 * Logs in and out users based on storing their User ID in a cookie and also adds a sliding session
 * timeout with a maximum age
 *
 * @author Brady Wied
 */
public abstract class BaseUserIdCookieSecurityContext<TUserId> implements UserLoginSecurityContext {
  public static final String UserKey = "primeCurrentUser";

  private static final String ContextKey = "primeLoginContext";

  protected final CookieProxy sessionCookie;

  private final Clock clock;

  private final Encryptor encryptor;

  private final ObjectMapper objectMapper;

  private final HTTPRequest request;

  private final HTTPResponse response;

  private final Duration sessionMaxAge;

  private final Duration sessionTimeout;

  protected BaseUserIdCookieSecurityContext(HTTPRequest request, HTTPResponse response, Encryptor encryptor, ObjectMapper objectMapper,
                                            Clock clock, Duration sessionTimeout, Duration sessionMaxAge) {
    this.request = request;
    this.response = response;
    this.encryptor = encryptor;
    this.objectMapper = objectMapper;
    this.clock = clock;
    this.sessionMaxAge = sessionMaxAge;
    this.sessionTimeout = sessionTimeout;
    long timeoutInSeconds = sessionTimeout.toSeconds();
    this.sessionCookie = new CookieProxy(getCookieName(), timeoutInSeconds, Cookie.SameSite.Strict);
  }

  /**
   * Get the currently logged in user. Only calls retrieveUserById once per request cycle.
   *
   * @return null if no user is logged in. Otherwise the user object as retrieved by retrieveUserById using the ID from the session
   */
  @Override
  public Object getCurrentUser() {
    var user = request.getAttribute(UserKey);
    if (user != null) {
      return user;
    }
    UserIdSessionContext<TUserId> sessionContext = resolveContext();
    if (sessionContext == null) {
      return null;
    }
    user = retrieveUserById(sessionContext.getUserId());
    this.request.setAttribute(UserKey, user);
    return user;
  }

  /**
   * The current session ID
   *
   * @return the session ID, if a session exists, otherwise null
   */
  @Override
  public String getSessionId() {
    return Optional.ofNullable(resolveContext())
                   .map(UserIdSessionContext::getSessionId)
                   .orElse(null);
  }

  @Override
  public boolean isLoggedIn() {
    // We only need to check the context, not the current user, to avoid calling the
    // retrieveUserById method, which is often less performant (DB call)
    return resolveContext() != null;
  }

  /**
   * Sets a cookie to log the user in
   *
   * @param user The user
   */
  @Override
  public void login(Object user) {
    try {
      var id = getIdFromUser(user);
      ZonedDateTime now = ZonedDateTime.now(clock);
      var sessionContext = createUserIdSessionContext(id, now);
      if (sessionContext.getSessionId() == null) {
        throw new IllegalArgumentException("Received a null getSessionId from " + sessionContext.getClass());
      }
      var cookieValue = CookieTools.toJSONCookie(sessionContext, true, true, this.encryptor, this.objectMapper);
      this.sessionCookie.add(request, response, cookieValue);
    } catch (Exception e) {
      // no partial state
      deleteCookies();
      throw new ErrorException(e);
    }
  }

  @Override
  public void logout() {
    deleteCookies();
  }

  /**
   * Ensures any other usages of getCurrentUser in the current request cycle are updated
   *
   * @param user The user to update
   */
  @Override
  public void updateUser(Object user) {
    Object currentUser = request.getAttribute(UserKey);
    if (currentUser != null) {
      request.setAttribute(UserKey, user);
    }
  }

  /**
   * Creates a new context that can be persisted in the session
   *
   * @param userId       user ID the context is for
   * @param loginInstant the instant the user logged in
   * @return context, readily serializable/deserializable by Jackson
   */
  protected abstract UserIdSessionContext<TUserId> createUserIdSessionContext(TUserId userId, ZonedDateTime loginInstant);

  protected String getCookieName() {
    return UserKey;
  }

  /**
   * Get the ID from the supplied user object
   *
   * @param user user to retrieve the ID for
   * @return ID of the user
   */
  protected abstract TUserId getIdFromUser(Object user);

  /**
   * What is the session context class
   *
   * @return the class
   */
  protected abstract Class<? extends UserIdSessionContext<TUserId>> getUserIdSessionContextClass();

  /**
   * Hydrates/retrieves the user based on ID
   *
   * @param id unique ID for the user
   * @return the object representing the user
   */
  protected abstract Object retrieveUserById(TUserId id);

  /**
   * Figure out whether to extend cookie
   *
   * @param signInTime when user signed in originally
   * @return correct cookie behavior
   */
  CookieExtendResult shouldExtendCookie(ZonedDateTime signInTime) {
    var now = ZonedDateTime.now(clock);
    var maxSessionAgeTime = signInTime.plus(sessionMaxAge);
    if (now.isAfter(maxSessionAgeTime)) {
      return CookieExtendResult.Invalid;
    }
    var extensionTime = now.plus(sessionTimeout);
    if (extensionTime.isAfter(maxSessionAgeTime)) {
      return CookieExtendResult.Keep;
    }
    var halfWayPoint = signInTime.plusMinutes(sessionTimeout.toMinutes() / 2);
    return now.isAfter(halfWayPoint) ? CookieExtendResult.Extend : CookieExtendResult.Keep;
  }

  private void deleteCookies() {
    this.sessionCookie.delete(request, response);
  }

  /**
   * Get existing deserialized session context from cookie
   *
   * @return value from cookie, null if no existing session
   */
  private UserIdSessionContext<TUserId> resolveContext() {
    var context = (UserIdSessionContext<TUserId>) this.request.getAttribute(ContextKey);
    if (context != null) {
      return context;
    }
    var cookie = this.sessionCookie.get(this.request);
    if (cookie == null) {
      return null;
    }
    try {
      context = CookieTools.fromJSONCookie(cookie,
                                           getUserIdSessionContextClass(),
                                           // we always encrypt in toCookie call
                                           true,
                                           true,
                                           encryptor,
                                           objectMapper);
      var shouldExtend = shouldExtendCookie(context.getLoginInstant());
      switch (shouldExtend) {
        case Extend:
          // same cookie value, but with longer time (set on cookie proxy in constructor)
          this.sessionCookie.add(request, response, cookie);
          break;
        case Invalid:
          // past max age, don't trust it at all
          deleteCookies();
          return null;
      }
      this.request.setAttribute(ContextKey, context);
      return context;
    } catch (BadPaddingException e) {
      // if the cookie encryption key changes (DB change, etc.) then we need new cookies, otherwise we cannot
      // decrypt and a user will be stuck trying to get back in
      this.deleteCookies();
      return null;
    } catch (Exception e) {
      throw new ErrorException(e);
    }
  }

  enum CookieExtendResult {
    // no extension needed
    Keep,
    // extend the cookie with the same value, but longer expiration time
    Extend,
    // the cookie is now invalid
    Invalid
  }
}

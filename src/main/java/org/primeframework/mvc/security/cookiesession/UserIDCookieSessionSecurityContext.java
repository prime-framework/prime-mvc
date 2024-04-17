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
package org.primeframework.mvc.security.cookiesession;

import javax.crypto.BadPaddingException;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.security.CookieProxy;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.UnauthenticatedException;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.util.CookieTools;

/**
 * Logs in and out users based on storing their User ID in a cookie and also adds a sliding session
 * timeout with a maximum age
 */
public abstract class UserIDCookieSessionSecurityContext implements UserLoginSecurityContext {
  public static final String UserKey = "primeCurrentUser";

  private final HTTPRequest request;

  private final HTTPResponse response;

  private final Encryptor encryptor;

  private final ObjectMapper objectMapper;

  protected final CookieProxy sessionCookie;

  private final Clock clock;

  private final Duration sessionMaxAge;

  private final Duration sessionTimeout;

  protected UserIDCookieSessionSecurityContext(HTTPRequest request, HTTPResponse response, Encryptor encryptor, ObjectMapper objectMapper,
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

  protected String getCookieName() {
    return UserKey;
  }

  enum CookieExtendResult {
    // no extension needed
    Keep,
    // extend the cookie with the same value
    Extend,
    // the cookie is now invalid
    Invalid
  }

  /**
   * Figure out whether to extend cookie
   *
   * @param now            the time now
   * @param signInTime     when user signed in originally
   * @param maxSessionAge  max absolute session length
   * @param sessionTimeout max session timeout
   * @return correct cookie behavior
   */
  static CookieExtendResult shouldExtendCookie(ZonedDateTime now, ZonedDateTime signInTime, Duration maxSessionAge, Duration sessionTimeout) {
    var maxSessionAgeTime = signInTime.plus(maxSessionAge);
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

  /**
   * Get the currently logged in user.
   *
   * Only calls retrieveUserById once per request cycle.
   *
   * @return null if no user is logged in. Otherwise the user object as retrieved by retrieveUserById using the ID from the session
   */
  @Override
  public Object getCurrentUser() {
    SerializedSessionContainer sessionContainer = getSessionContainer();
    if (sessionContainer == null) {
      return null;
    }
    if (sessionContainer instanceof InMemorySessionContainer inMemory) {
      return inMemory.user;
    }
    var user = retrieveUserById(sessionContainer.userId);
    var newContainer = new InMemorySessionContainer(sessionContainer, user);
    this.request.setAttribute(UserKey, newContainer);
    return newContainer.user;
  }

  private SerializedSessionContainer getSessionContainer() {
    var container = (SerializedSessionContainer) this.request.getAttribute(UserKey);
    if (container != null) {
      return container;
    }
    var cookie = this.sessionCookie.get(this.request);
    if (cookie == null) {
      return null;
    }
    try {
      container = CookieTools.fromJSONCookie(cookie, SerializedSessionContainer.class, true, encryptor, objectMapper);
      var shouldExtend = shouldExtendCookie(ZonedDateTime.now(this.clock), container.signInInstant, sessionMaxAge, sessionTimeout);
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
      this.request.setAttribute(UserKey, container);
      return container;
    } catch (BadPaddingException e) {
      // if the cookie encryption key changes (DB change, etc.) then we need new cookies, otherwise we cannot
      // decrypt and a user will be stuck trying to get back in
      this.deleteCookies();
      return null;
    } catch (InvalidDefinitionException e) {
      checkForMissingLibrary(e);
      return null;
    } catch (Exception e) {
      throw new ErrorException(e);
    }
  }

  /**
   * Hydrates/retrieves the user based
   *
   * @param id unique ID for the user
   * @return the object representing the user
   */
  protected abstract IdentifiableUser retrieveUserById(UUID id);

  @Override
  public String getSessionId() {
    return Optional.ofNullable(getSessionContainer()).map(c -> c.sessionId).orElse(null);
  }

  @Override
  public boolean isLoggedIn() {
    // user MIGHT a DB query to hydrate the user so we can do a lighter weight check by just looking for the session ID
    return getSessionId() != null;
  }

  @Override
  public void login(Object context) {
    if (!(context instanceof IdentifiableUser user)) {
      throw new IllegalArgumentException("Expected a user object here of type IdentifiableUser!");
    }
    try {
      var id = user.getId();
      var newSessionId = UUID.randomUUID();
      ZonedDateTime now = ZonedDateTime.now(clock);
      var container = new SerializedSessionContainer(id, newSessionId.toString(), now);
      writeContainerToCookie(container);
    } catch (Exception e) {
      // no partial state
      deleteCookies();
      throw new ErrorException(e);
    }
  }

  private void checkForMissingLibrary(InvalidDefinitionException e) throws ErrorException {
    Exception cause = e;
    if (e.getMessage().contains("Java 8 date/time type `java.time.ZonedDateTime` not supported by default: add Module")) {
      cause = new IllegalStateException("You are missing a Jackson module that serializes ZonedDateTime. Adding com.inversoft:jackson5 to your dependencies and adding JacksonModule from that dependency to your MultiBinder is recommended.", e);
    }
    throw new ErrorException(cause);
  }

  private void writeContainerToCookie(SerializedSessionContainer container) throws Exception {
    try {
      var cookieValue = CookieTools.toJSONCookie(container, true, true, this.encryptor, this.objectMapper);
      this.sessionCookie.add(request, response, cookieValue);
    } catch (InvalidDefinitionException e) {
      checkForMissingLibrary(e);
    }
  }

  private void deleteCookies() {
    this.sessionCookie.delete(request, response);
  }

  @Override
  public void logout() {
    deleteCookies();
  }

  /**
   * Ensures any other usages of getCurrentUser in the current request cycle are updated
   *
   * @param context The user to update
   */
  @Override
  public void updateUser(Object context) {
    if (!(context instanceof IdentifiableUser user)) {
      throw new IllegalArgumentException("Expected a user object here of type IdentifiableUser!");
    }
    var currentContainer = getSessionContainer();
    if (currentContainer == null) {
      throw new UnauthenticatedException();
    }
    var newContainer = new InMemorySessionContainer(currentContainer.sessionId, currentContainer.signInInstant, user);
    request.setAttribute(UserKey, newContainer);
  }
}

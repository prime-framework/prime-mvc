/*
 * Copyright (c) 2015-2024, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fusionauth.http.Cookie.SameSite;
import io.fusionauth.http.HTTPValues.ContentTypes;
import io.fusionauth.http.HTTPValues.Headers;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import org.primeframework.mvc.http.FormDataBodyHandler;
import org.primeframework.mvc.http.JSONResponseHandler;
import org.primeframework.mvc.security.oauth.OAuthConfiguration;
import org.primeframework.mvc.security.oauth.RefreshResponse;
import org.primeframework.mvc.security.oauth.TokenAuthenticationMethod;
import org.primeframework.mvc.security.oauth.Tokens;
import static org.primeframework.mvc.util.ObjectTools.defaultIfNull;

/**
 * Uses cookies to manage the JWT and refresh tokens from an OAuth provider and also manages the refreshing of JWTs.
 *
 * @author Daniel DeGroff
 */
public abstract class BaseJWTRefreshTokenCookiesUserLoginSecurityContext implements UserLoginSecurityContext {
  private static final String ContextKey = "primeLoginContext";

  private static final String UserKey = "primeCurrentUser";

  protected final CookieProxy jwtCookie;

  protected final CookieProxy refreshTokenCookie;

  protected final HTTPRequest request;

  protected final HTTPResponse response;

  protected final VerifierProvider verifierProvider;

  protected BaseJWTRefreshTokenCookiesUserLoginSecurityContext(HTTPRequest request, HTTPResponse response,
                                                               VerifierProvider verifierProvider) {
    this.request = request;
    this.response = response;
    this.verifierProvider = verifierProvider;

    // The cookies for the tokens has an expiration of 70 years to allow it to work with Firefox
    this.jwtCookie = new CookieProxy(jwtCookieName(), (long) Integer.MAX_VALUE, cookieSameSite());
    this.refreshTokenCookie = new CookieProxy(refreshTokenCookieName(), (long) Integer.MAX_VALUE, cookieSameSite());
  }

  @Override
  public Object getCurrentUser() {
    // Cache in the request
    Object user = request.getAttribute(UserKey);
    if (user != null) {
      return user;
    }

    Tokens tokens = resolveContext();
    if (tokens.jwt == null) {
      return null;
    }

    user = retrieveUserForJWT(tokens.jwt);
    if (user == null) {
      jwtCookie.delete(request, response);
    } else {
      request.setAttribute(UserKey, user);
    }

    return user;
  }

  @Override
  public String getSessionId() {
    Tokens tokens = resolveContext();
    if (tokens.decodedJWT != null) {
      return tokens.decodedJWT.getString("sid");
    }

    return null;
  }

  @Override
  public boolean isLoggedIn() {
    return getCurrentUser() != null;
  }

  @Override
  public void login(Object context) {
    if (!(context instanceof Tokens tokens)) {
      throw new IllegalArgumentException("The login context for [BaseJWTRefreshTokenCookiesUserLoginSecurityContext] is expected to be of type [" + Tokens.class.getCanonicalName() + "] but an object of type [" + context.getClass().getCanonicalName() + "] was provided. This is a development time error.");
    }

    try {
      // Do not trust the input to this method.
      // - We expect the caller to provide a valid JWT, but let's ensure it is not expired and check for application specific claims.
      // - Note that it is allowed to omit the JWT on login which simply causes an immediate refresh.
      if (tokens.jwt != null) {
        Map<String, Verifier> verifiers = getVerifiersOrNull();
        if (verifiers == null) {
          return;
        }

        tokens.decodedJWT = JWT.getDecoder().decode(tokens.jwt, verifiers);
        if (!validateJWTClaims(tokens.decodedJWT)) {
          clearTokens(tokens);
          throw new InvalidLoginContext();
        }

        jwtCookie.add(request, response, tokens.jwt);
      }

      if (tokens.refreshToken != null) {
        refreshTokenCookie.add(request, response, tokens.refreshToken);
      }
    } catch (Exception e) {
      clearTokens(tokens);
      throw new InvalidLoginContext();
    }
  }

  @Override
  public void logout() {
    jwtCookie.delete(request, response);
    refreshTokenCookie.delete(request, response);
  }

  @Override
  public void updateUser(Object user) {
    Object currentUser = request.getAttribute(UserKey);
    if (currentUser != null) {
      request.setAttribute(UserKey, user);
    }
  }

  protected SameSite cookieSameSite() {
    return SameSite.Strict;
  }

  /**
   * The JWT that is passed to this method is known to be valid. The signature has been validated, and the JWT is not expired.
   * <p>
   * You may wish to perform an additional check to identify if this JWT has been revoked and no longer valid based upon external criteria.
   *
   * @param jwt the decoded JWT
   * @return true if this JWT has been revoked. False if the JWT has not been revoked and is ok to be used.
   */
  protected boolean isRevoked(@SuppressWarnings("unused") JWT jwt) {
    return false;
  }

  /**
   * Allows subclasses to specify the name of the JWT cookie.
   *
   * @return The cookie name.
   */
  protected abstract String jwtCookieName();

  /**
   * @return the oauth configuration required to connect and authenticate to the token endpoint, etc.
   */
  protected abstract OAuthConfiguration oauthConfiguration();

  /**
   * Allows subclasses to specify the name of the refresh token cookie.
   *
   * @return The cookie name.
   */
  protected abstract String refreshTokenCookieName();

  /**
   * Retrieve a user given an encoded JWT string.
   *
   * @param jwt the encoded JWT string
   * @return a user object.
   */
  protected abstract Object retrieveUserForJWT(String jwt);

  /**
   * The JWT that is passed to this method is known to be valid. The signature has been validated, and the JWT is not expired.
   * <p>
   * You may wish to perform application specific validation on the JWT claims.
   *
   * @param jwt the decoded JWT
   * @return true if the validation is ok and the JWT can be used. False if the JWT is not ok- and it should not be used. Returning true will still
   *     allow a refresh token to be used if available.
   */
  protected boolean validateJWTClaims(@SuppressWarnings("unused") JWT jwt) {
    return true;
  }

  private void clearTokens(Tokens tokens) {
    tokens.decodedJWT = null;
    tokens.jwt = null;
    tokens.refreshToken = null;
    jwtCookie.delete(request, response);
    refreshTokenCookie.delete(request, response);
  }

  private Map<String, Verifier> getVerifiersOrNull() {
    // If we do not have any verifiers, do not attempt to decode the JWT.
    // - This is a fail-safe against validating a JWT with an alg of 'none'.
    //   In practice, at least in FusionAuth, we'll always have at least one
    //   verifier. But to protect other users of this library, do not attempt
    //   a JWT unless we have a verifier.
    // - By returning null, we ensure that the JWT decoder will explode because the 'verifiers' is a non-null parameter.
    Map<String, Verifier> verifiers = verifierProvider.get();
    return verifiers.isEmpty() ? null : verifiers;
  }

  private Tokens refreshJWT(Tokens tokens) {
    tokens.jwt = null;
    tokens.decodedJWT = null;

    if (tokens.refreshToken == null) {
      jwtCookie.delete(request, response);
      return tokens;
    }

    Map<String, List<String>> body = new HashMap<>(2);
    body.put("grant_type", List.of("refresh_token"));
    body.put("refresh_token", List.of(tokens.refreshToken));

    OAuthConfiguration oauthConfiguration = oauthConfiguration();
    if (oauthConfiguration == null) {
      return tokens;
    }

    Builder clientBuilder = HttpClient.newBuilder();

    if (oauthConfiguration.authenticationMethod == TokenAuthenticationMethod.client_secret_basic) {
      clientBuilder.authenticator(new BasicAuthenticator(oauthConfiguration.clientId, oauthConfiguration.clientSecret));
    } else if (oauthConfiguration.authenticationMethod == TokenAuthenticationMethod.client_secret_post) {
      body.put("client_id", List.of(oauthConfiguration.clientId));
      body.put("client_secret", List.of(oauthConfiguration.clientSecret));
    }

    var handler = new FormDataBodyHandler(body);
    HttpRequest refreshRequest = HttpRequest.newBuilder(URI.create(oauthConfiguration.tokenEndpoint))
                                            .header(Headers.ContentType, ContentTypes.Form)
                                            .POST(BodyPublishers.ofByteArray(handler.getBody()))
                                            .build();

    HttpResponse<InputStream> resp = null;
    Exception endpointException = null;
    try {
      resp = clientBuilder.build().send(refreshRequest, BodyHandlers.ofInputStream());
    } catch (Exception e) {
      endpointException = e;
    }
    if (endpointException != null || resp.statusCode() < 200 || resp.statusCode() > 299) {
      tokens.refreshToken = null;
      jwtCookie.delete(request, response);
      refreshTokenCookie.delete(request, response);
      return tokens;
    }

    var responseHandler = new JSONResponseHandler<>(RefreshResponse.class);
    RefreshResponse rr;
    try {
      rr = responseHandler.apply(resp.body());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    tokens.jwt = rr.access_token;
    tokens.refreshToken = defaultIfNull(rr.refresh_token, tokens.refreshToken);

    Map<String, Verifier> verifiers = getVerifiersOrNull();
    if (verifiers != null) {
      tokens.decodedJWT = JWT.getDecoder().decode(tokens.jwt, verifiers);
      // The JWT was refreshed successfully, and signature verified. However, we still want to verify claims on each refresh.
      if (!validateJWTClaims(tokens.decodedJWT)) {
        clearTokens(tokens);
        return tokens;
      }
    }

    if (tokens.jwt != null) {
      jwtCookie.add(request, response, tokens.jwt);
    }

    if (tokens.refreshToken != null) {
      refreshTokenCookie.add(request, response, tokens.refreshToken);
    }

    return tokens;
  }

  private Tokens resolveContext() {
    Tokens tokens = (Tokens) request.getAttribute(ContextKey);
    if (tokens != null) {
      return tokens;
    }

    // Add the new tokens to the request, and then we fill them out below
    tokens = new Tokens();
    request.setAttribute(ContextKey, tokens);

    Map<String, Verifier> verifiers = getVerifiersOrNull();
    if (verifiers == null) {
      return tokens;
    }

    tokens.jwt = jwtCookie.get(request);
    tokens.refreshToken = refreshTokenCookie.get(request);
    if (tokens.jwt == null && tokens.refreshToken == null) {
      return tokens;
    }

    try {
      // We can optionally start with only a refresh token, no JWT.
      // - Treat this the same as an expired JWT - just refresh my JWT man!
      if (tokens.jwt == null) {
        return refreshJWT(tokens);
      }

      tokens.decodedJWT = JWT.getDecoder().decode(tokens.jwt, verifiers);
      if (!validateJWTClaims(tokens.decodedJWT) || isRevoked(tokens.decodedJWT)) {
        clearTokens(tokens);
        return tokens;
      }

      return tokens;
    } catch (JWTExpiredException e) {
      return refreshJWT(tokens);
    } catch (Exception e) {
      clearTokens(tokens);
      return tokens;
    }
  }
}

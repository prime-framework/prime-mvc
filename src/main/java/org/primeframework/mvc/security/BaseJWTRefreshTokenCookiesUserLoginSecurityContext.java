/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.RESTClient;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
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
    this.jwtCookie = new CookieProxy(jwtCookieName(), (long) Integer.MAX_VALUE);
    this.refreshTokenCookie = new CookieProxy(refreshTokenCookieName(), (long) Integer.MAX_VALUE);
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
    if (user != null) {
      request.setAttribute(UserKey, user);
    }

    return user;
  }

  @Override
  public String getSessionId() {
    Tokens tokens = resolveContext();
    return defaultIfNull(tokens.refreshToken, tokens.jwt);
  }

  @Override
  public boolean isLoggedIn() {
    return getCurrentUser() != null;
  }

  @Override
  public void login(Object context) {
    Tokens tokens = (Tokens) context;
    if (tokens.jwt != null) {
      jwtCookie.add(request, response, tokens.jwt);
    }

    if (tokens.refreshToken != null) {
      refreshTokenCookie.add(request, response, tokens.refreshToken);
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

  protected abstract Object retrieveUserForJWT(String jwt);

  /**
   * The JWT that is passed to this method is known to be valid. The signature has been validated, and the JWT is not
   * expired.
   * <p>
   * You may wish to perform application specific validation on the JWT claims.
   *
   * @param jwt the decoded JWT
   * @return true if the validation is ok and the JWT can be used. False if the JWT is not ok- and it should not be
   *     used. Returning true will still allow a refresh token to be used if available.
   */
  protected boolean validateJWTClaims(@SuppressWarnings("unused") JWT jwt) {
    return true;
  }

  private Tokens refreshJWT(Tokens tokens) {
    tokens.jwt = null;

    if (tokens.refreshToken == null) {
      jwtCookie.delete(request, response);
      return tokens;
    }

    Map<String, List<String>> body = new HashMap<>(2);
    body.put("grant_type", List.of("refresh_token"));
    body.put("refresh_token", List.of(tokens.refreshToken));

    OAuthConfiguration oauthConfiguration = oauthConfiguration();
    RESTClient<RefreshResponse, JsonNode> client = new RESTClient<>(RefreshResponse.class, JsonNode.class)
        .url(oauthConfiguration.tokenEndpoint)
        .successResponseHandler(new JSONResponseHandler<>(RefreshResponse.class))
        .errorResponseHandler(new JSONResponseHandler<>(JsonNode.class));

    if (oauthConfiguration.authenticationMethod == TokenAuthenticationMethod.client_secret_basic) {
      client.basicAuthorization(oauthConfiguration.clientId, oauthConfiguration.clientSecret);
    } else if (oauthConfiguration.authenticationMethod == TokenAuthenticationMethod.client_secret_post) {
      body.put("client_id", List.of(oauthConfiguration.clientId));
      body.put("client_secret", List.of(oauthConfiguration.clientSecret));
    }

    ClientResponse<RefreshResponse, JsonNode> resp = client
        .bodyHandler(new FormDataBodyHandler(body))
        .post()
        .go();

    if (!resp.wasSuccessful()) {
      tokens.refreshToken = null;
      jwtCookie.delete(request, response);
      refreshTokenCookie.delete(request, response);
      return tokens;
    }

    RefreshResponse rr = resp.getSuccessResponse();
    tokens.jwt = rr.access_token;
    tokens.refreshToken = defaultIfNull(rr.refresh_token, tokens.refreshToken);

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

    // Add the new tokens to the request and then we fill them out below
    tokens = new Tokens();
    request.setAttribute(ContextKey, tokens);

    // If we do not have any verifiers, do not attempt to decode the JWT.
    // - This is a fail-safe against validating a JWT with an alg of 'none'.
    //   In practice, at least in FusionAuth, we'll always have at least one
    //   verifier. But to protect other users of this library, do not attempt
    //   a JWT unless we have a verifier.
    Map<String, Verifier> verifiers = verifierProvider.get();
    if (verifiers.isEmpty()) {
      return tokens;
    }

    tokens.jwt = jwtCookie.get(request);
    tokens.refreshToken = refreshTokenCookie.get(request);
    if (tokens.jwt == null && tokens.refreshToken == null) {
      return tokens;
    }

    try {
      JWT jwt = JWT.getDecoder().decode(tokens.jwt, verifiers);
      if (!validateJWTClaims(jwt)) {
        return refreshJWT(tokens);
      }

      return tokens;
    } catch (JWTExpiredException e) {
      return refreshJWT(tokens);
    } catch (Exception e) {
      tokens.jwt = null;
      tokens.refreshToken = null;
      jwtCookie.delete(request, response);
      refreshTokenCookie.delete(request, response);
      return tokens;
    }
  }
}

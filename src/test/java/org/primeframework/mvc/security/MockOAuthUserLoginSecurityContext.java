/*
 * Copyright (c) 2016-2023, Inversoft Inc., All Rights Reserved
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

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.security.oauth.OAuthConfiguration;
import org.primeframework.mvc.security.oauth.TokenAuthenticationMethod;
import org.primeframework.mvc.security.oauth.Tokens;

/**
 * @author Brian Pontarelli
 */
public class MockOAuthUserLoginSecurityContext extends BaseJWTRefreshTokenCookiesUserLoginSecurityContext {
  public static Object CurrentUser;

  public static Set<String> Roles = new HashSet<>();

  public static String TokenEndpoint = "http://localhost:8000/oauth/token";

  public static boolean ValidateJWTOnLogin = true;

  public static String clientId;

  public static String clientSecret;

  public static TokenAuthenticationMethod tokenAuthenticationMethod;

  @Inject
  public MockOAuthUserLoginSecurityContext(HTTPRequest request, HTTPResponse response,
                                           VerifierProvider verifierProvider) {
    super(request, response, verifierProvider);
  }

  public static void reset() {
    TokenEndpoint = "http://localhost:8000/oauth/token";
    ValidateJWTOnLogin = true;
    tokenAuthenticationMethod = TokenAuthenticationMethod.none;
  }

  @Override
  public Set<String> getCurrentUsersRoles() {
    return Roles;
  }

  @Override
  public String getSessionId() {
    return CurrentUser != null ? Integer.toString(CurrentUser.hashCode()) : null;
  }

  @Override
  public void login(Object context) {
    if (ValidateJWTOnLogin) {
      super.login(context);
    }

    // This is a Mock version, it does not validate the JWT on login.
    if (!(context instanceof Tokens tokens)) {
      throw new IllegalArgumentException("The login context for [BaseJWTRefreshTokenCookiesUserLoginSecurityContext] is expected to be of type [" + Tokens.class.getCanonicalName() + "] but an object of type [" + context.getClass().getCanonicalName() + "] was provided. This is a development time error.");
    }

    if (tokens.jwt != null) {
      jwtCookie.add(request, response, tokens.jwt);
    }

    if (tokens.refreshToken != null) {
      refreshTokenCookie.add(request, response, tokens.refreshToken);
    }
  }

  @Override
  protected String jwtCookieName() {
    return "prime-jwt";
  }

  @Override
  protected OAuthConfiguration oauthConfiguration() {
    return new OAuthConfiguration().with(c -> c.authenticationMethod = tokenAuthenticationMethod)
                                   .with(c -> c.clientId = clientId)
                                   .with(c -> c.clientSecret = clientSecret)
                                   .with(c -> c.tokenEndpoint = TokenEndpoint);
  }

  @Override
  protected String refreshTokenCookieName() {
    return "prime-refresh-token";
  }

  @Override
  protected Object retrieveUserForJWT(String jwt) {
    return CurrentUser;
  }
}

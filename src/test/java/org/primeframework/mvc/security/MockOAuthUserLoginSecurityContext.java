/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.security.oauth.OAuthConfiguration;
import org.primeframework.mvc.security.oauth.TokenAuthenticationMethod;

/**
 * @author Brian Pontarelli
 */
public class MockOAuthUserLoginSecurityContext extends BaseJWTRefreshTokenCookiesUserLoginSecurityContext {
  public static Object CurrentUser;

  public static Set<String> Roles = new HashSet<>();

  public static String TokenEndpoint = "http://localhost:8000/oauth/token";

  public static String clientId;

  public static String clientSecret;

  @Inject
  public MockOAuthUserLoginSecurityContext(HTTPRequest request, HTTPResponse response,
                                           VerifierProvider verifierProvider) {
    super(request, response, verifierProvider);
  }

  @Override
  public Set<String> getCurrentUsersRoles() {
    return Roles;
  }

  @Override
  protected String jwtCookieName() {
    return "prime-jwt";
  }

  @Override
  protected OAuthConfiguration oauthConfiguration() {
    return new OAuthConfiguration().with(c -> c.authenticationMethod = TokenAuthenticationMethod.none)
                                   .with(c -> c.clientId = clientId)
                                   .with(c -> c.clientId = clientSecret)
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

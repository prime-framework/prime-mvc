/*
 * Copyright (c) 2015-2025, Inversoft Inc., All Rights Reserved
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.http.Cookie.SameSite;
import io.fusionauth.http.HTTPValues.ContentTypes;
import io.fusionauth.http.HTTPValues.Headers;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.json.JacksonModule;
import org.primeframework.mvc.http.FormBodyPublisher;
import org.primeframework.mvc.http.JSONResponseBodyHandler;
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

  // can run out of open files if we create too many of these
  private static final HttpClient httpClient = HttpClient.newHttpClient();

  protected final CookieProxy jwtCookie;

  private final static ObjectMapper objectMapper = new ObjectMapper().registerModule(new JacksonModule());

  protected final CookieProxy refreshTokenCookie;

  protected final HTTPRequest request;

  protected final HTTPResponse response;

  protected final VerifierProvider verifierProvider;

  protected BaseJWTRefreshTokenCookiesUserLoginSecurityContext(HTTPRequest request, HTTPResponse response, VerifierProvider verifierProvider) {
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
      //System.out.println("---returning attribute user " + user);
      return user;
    }

    Tokens tokens = resolveContext();
    if (tokens.jwt == null) {
      // System.out.println("---returning null because tokens.jwt is null");
      return null;
    }

    user = retrieveUserForJWT(tokens.jwt);
    if (user == null) {
      // System.out.println("---returning null because retrieveUserForJWT is null");
      jwtCookie.delete(request, response);
    } else {
      // System.out.println("---yay got user " + user);
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

    Builder requestBuilder = HttpRequest.newBuilder(URI.create(oauthConfiguration.tokenEndpoint));
    if (oauthConfiguration.authenticationMethod == TokenAuthenticationMethod.client_secret_basic) {
      // see https://www.rfc-editor.org/rfc/rfc2617#section-2
      if (oauthConfiguration.clientId.contains(":")) {
        tokens.refreshToken = null;
        jwtCookie.delete(request, response);
        refreshTokenCookie.delete(request, response);
        return tokens;
      }
      // not using the HttpClient authenticator/PasswordAuthentication support because
      // we want pre-emptive auth here
      String encoded = Base64.getEncoder()
                             .encodeToString((oauthConfiguration.clientId + ":" + oauthConfiguration.clientSecret)
                                                 .getBytes(StandardCharsets.UTF_8));
      requestBuilder.header("Authorization", "Basic " + encoded);
    } else if (oauthConfiguration.authenticationMethod == TokenAuthenticationMethod.client_secret_post) {
      body.put("client_id", List.of(oauthConfiguration.clientId));
      body.put("client_secret", List.of(oauthConfiguration.clientSecret));
    }

    if (System.getenv("curl_refresh_only") == null) {
      HttpRequest refreshRequest = requestBuilder.header(Headers.ContentType, ContentTypes.Form)
                                                 .POST(new FormBodyPublisher(body))
                                                 .build();

//      HttpResponse<RefreshResponse> resp = null;
      HttpResponse<String> resp = null;
      Exception endpointException = null;
      RefreshResponse rr = null;

      try {
//        resp = httpClient.send(refreshRequest, new JSONResponseBodyHandler<>(RefreshResponse.class));
        resp = httpClient.send(refreshRequest, BodyHandlers.ofString());
        rr = objectMapper.readValue(resp.body(), RefreshResponse.class);
        System.out.println("---refreshJWT - got back status code " + resp.statusCode());
      } catch (Exception e) {
        System.out.println("---refreshJWT - got back exception " + e.getMessage());
        e.printStackTrace();
        try {
          if (System.getenv("curl_refresh_retry") != null) {
            List<String> args = List.of("curl",
                                        "-v",
                                        "-H",
                                        "Content-Type: application/x-www-form-urlencoded",
                                        "-d",
                                        new String(new FormBodyPublisher(body).getBody()),
                                        oauthConfiguration.tokenEndpoint);
            System.out.println("---refreshJWT - sending another request with curl, command line " + args);
            Process process = new ProcessBuilder(args)
                .inheritIO()
                .start();
            process.waitFor();
            System.out.println("---refreshJWT - curl request complete");
          }
          if (System.getenv("java_net_http_refresh_retry") != null) {
            System.out.println("---refreshJWT trying again with java net http");
            try {
//              resp = httpClient.send(refreshRequest, new JSONResponseBodyHandler<>(RefreshResponse.class));
              resp = httpClient.send(refreshRequest, BodyHandlers.ofString());
              rr = objectMapper.readValue(resp.body(), RefreshResponse.class);
              System.out.println("---refreshJWT java net http attempt 2 - got back status code " + resp.statusCode());
            } catch (Exception attempt2) {
              System.out.println("---refreshJWT java net http attempt 2 - got back exception " + attempt2.getMessage());
              attempt2.printStackTrace();
              endpointException = attempt2;
            }
          }
        } catch (Exception curlE) {
          throw new RuntimeException(curlE);
        }
      }

      if (endpointException != null || resp.statusCode() < 200 || resp.statusCode() > 299) {
        tokens.refreshToken = null;
        jwtCookie.delete(request, response);
        refreshTokenCookie.delete(request, response);
        return tokens;
      }

//      RefreshResponse rr = resp.body();
      tokens.jwt = rr.access_token;
      tokens.refreshToken = defaultIfNull(rr.refresh_token, tokens.refreshToken);
    } else {
      List<String> args = List.of("curl",
                                  "-s",
                                  "-H",
                                  "Content-Type: application/x-www-form-urlencoded",
                                  "-d",
                                  new String(new FormBodyPublisher(body).getBody()),
                                  oauthConfiguration.tokenEndpoint);
      System.out.println("---refreshJWT - sending ONLY request with curl, command line " + args);
      try {
        Process process = new ProcessBuilder(args)
            .start();
        process.waitFor();
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          builder.append(line);
          builder.append(System.getProperty("line.separator"));
        }
        String result = builder.toString();
        System.out.println("---refreshJWT - sending ONLY request with curl, got result " + result);
        RefreshResponse rr = new ObjectMapper().readValue(result, RefreshResponse.class);
        tokens.jwt = rr.access_token;
        tokens.refreshToken = defaultIfNull(rr.refresh_token, tokens.refreshToken);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      System.out.println("---refreshJWT - curl request complete");
    }

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
      //System.out.println("---resolveContext returning tokens from attributes");
      return tokens;
    }

    // Add the new tokens to the request, and then we fill them out below
    tokens = new Tokens();
    request.setAttribute(ContextKey, tokens);

    Map<String, Verifier> verifiers = getVerifiersOrNull();
    if (verifiers == null) {
      //System.out.println("---resolveContext returning empty tokens because verifiers is null");
      return tokens;
    }

    tokens.jwt = jwtCookie.get(request);
    tokens.refreshToken = refreshTokenCookie.get(request);
    if (tokens.jwt == null && tokens.refreshToken == null) {
      //System.out.println("---resolveContext returning empty tokens because refresh and jwt are null");
      return tokens;
    }

    try {
      // We can optionally start with only a refresh token, no JWT.
      // - Treat this the same as an expired JWT - just refresh my JWT man!
      if (tokens.jwt == null) {
        System.out.println("---resolveContext refreshing and returning");
        return refreshJWT(tokens);
      }

      tokens.decodedJWT = JWT.getDecoder().decode(tokens.jwt, verifiers);
      if (!validateJWTClaims(tokens.decodedJWT) || isRevoked(tokens.decodedJWT)) {
        System.out.println("---resolveContext returning empty tokens because jWT claims invalid or revoked");
        clearTokens(tokens);
        return tokens;
      }

      return tokens;
    } catch (JWTExpiredException e) {
      System.out.println("---resolveContext refreshing due to JWTExpiredException and returning");
      return refreshJWT(tokens);
    } catch (Exception e) {
      System.out.println("---resolveContext returning empty tokens due to " + e.getMessage());
      clearTokens(tokens);
      return tokens;
    }
  }
}

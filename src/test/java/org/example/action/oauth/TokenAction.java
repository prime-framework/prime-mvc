/*
 * Copyright (c) 2021-2025, Inversoft Inc., All Rights Reserved
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
package org.example.action.oauth;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.JWTEncoder;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.FieldName;
import org.primeframework.mvc.parameter.annotation.UnknownParameters;
import org.primeframework.mvc.security.MockOAuthUserLoginSecurityContext;
import org.primeframework.mvc.security.oauth.RefreshResponse;
import static org.example.action.oauth.LoginAction.Subject;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Action
@JSON
public class TokenAction {
  @FieldName("client_id")
  public String clientId;

  @FieldName("client_secret")
  public String clientSecret;

  public String grant_type;

  public String refresh_token;

  @JSONResponse
  public RefreshResponse response = new RefreshResponse();

  @UnknownParameters
  public Map<String, String[]> unknownParameters = new HashMap<>();

  @Inject
  private HTTPRequest httpRequest;

  public String post() {
    assertEquals(grant_type, "refresh_token");
    assertEquals(refresh_token, "prime-refresh-token-value");
    switch (MockOAuthUserLoginSecurityContext.tokenAuthenticationMethod) {
      case client_secret_post -> {
        assertEquals(clientId, "the client ID");
        assertEquals(clientSecret, "the client secret");
      }
      // the base 64 encoded value of the client ID:the client secret
      case client_secret_basic -> assertEquals(httpRequest.getHeader("Authorization"), "Basic dGhlIGNsaWVudCBJRDp0aGUgY2xpZW50IHNlY3JldA==");
      case none -> assertTrue(true);
    }

    // if additional parameters were sent, validate them
    MockOAuthUserLoginSecurityContext.additionalParameters.forEach((key, value) -> {
      assertTrue(unknownParameters.containsKey(key), "Missing additional parameter: " + key);
      assertEquals(unknownParameters.get(key), value.toArray(), "Mismatched additional parameter value for: " + key);
    });

    JWT jwt = new JWT();
    jwt.audience = "prime-tests";
    jwt.issuedAt = ZonedDateTime.now(ZoneOffset.UTC);
    jwt.expiration = jwt.issuedAt.plusMinutes(1);
    jwt.issuer = "Prime";
    jwt.subject = Subject;

    // Use the same secret as the TestSecurityModule does
    response.access_token = new JWTEncoder().encode(jwt, HMACSigner.newSHA256Signer("secret"));

    return "success";
  }
}

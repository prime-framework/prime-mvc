/*
 * Copyright (c) 2021-2023, Inversoft Inc., All Rights Reserved
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

import io.fusionauth.jwt.JWTEncoder;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.security.oauth.RefreshResponse;
import static org.example.action.oauth.LoginAction.Subject;
import static org.testng.Assert.assertEquals;

@Action
@JSON
public class TokenAction {
  public String grant_type;

  public String refresh_token;

  @JSONResponse
  public RefreshResponse response = new RefreshResponse();

  public String post() {
    assertEquals(grant_type, "refresh_token");
    assertEquals(refresh_token, "prime-refresh-token-value");

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

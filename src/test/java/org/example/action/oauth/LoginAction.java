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
import java.util.UUID;

import com.google.inject.Inject;
import io.fusionauth.jwt.JWTEncoder;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import org.example.domain.User;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.security.MockOAuthUserLoginSecurityContext;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.oauth.Tokens;

@Action
@Status.List({
    @Status,
    @Status(code = "unauthenticated", status = 401)
})
public class LoginAction {
  public static final String Subject = UUID.randomUUID().toString();

  private final UserLoginSecurityContext context;

  public boolean expired;

  @Inject
  public LoginAction(UserLoginSecurityContext context) {
    this.context = context;
  }

  public String post() {
    JWT jwt = new JWT();
    jwt.audience = "prime-tests";
    jwt.issuedAt = ZonedDateTime.now(ZoneOffset.UTC);
    jwt.expiration = expired ? jwt.issuedAt.minusMinutes(1) : jwt.issuedAt.plusMinutes(1);
    jwt.issuer = "Prime";
    jwt.subject = Subject;

    // Use the same secret as the TestSecurityModule does
    String encodedJWT = new JWTEncoder().encode(jwt, HMACSigner.newSHA256Signer("secret"));
    context.login(new Tokens(encodedJWT, "prime-refresh-token-value"));

    // Setup the user
    User user = new User();
    user.setName("Brian Pontarelli");
    MockOAuthUserLoginSecurityContext.CurrentUser = user;

    return "success";
  }
}

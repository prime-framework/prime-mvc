/*
 * Copyright (c) 2015-2017, Inversoft Inc., All Rights Reserved
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

import java.util.Arrays;
import java.util.Set;

import com.google.inject.Inject;

/**
 * A security scheme that authenticates and authorizes users using a UserLoginSecurityContext implementation.
 *
 * @author Brian Pontarelli
 */
public class UserLoginSecurityScheme implements SecurityScheme {
  private UserLoginSecurityContext userLoginSecurityContext;

  @Override
  public void handle(String[] constraints) {
    if (userLoginSecurityContext == null) {
      return;
    }

    // Check if user is signed in
    if (!userLoginSecurityContext.isLoggedIn()) {
      throw new UnauthenticatedException();
    }

    // Check roles
    if (constraints.length > 0) {
      Set<String> userRoles = userLoginSecurityContext.getCurrentUsersRoles();
      if (Arrays.stream(constraints).noneMatch(userRoles::contains)) {
        throw new UnauthorizedException();
      }
    }
  }

  @Inject(optional = true)
  public void setUserLoginSecurityContext(UserLoginSecurityContext userLoginSecurityContext) {
    this.userLoginSecurityContext = userLoginSecurityContext;
  }
}

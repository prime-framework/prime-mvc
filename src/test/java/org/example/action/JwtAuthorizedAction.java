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
package org.example.action;

import org.primeframework.jwt.domain.JWT;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;
import org.primeframework.mvc.servlet.HTTPMethod;

/**
 * @author Daniel DeGroff
 */
@Action(requiresAuthentication = true, jwtEnabled = true)
@Status.List({
    @Status(code = "success", status = 200),
    @Status(code = "unauthenticated", status = 401),
    @Status(code = "unauthorized", status = 401)
})
public class JwtAuthorizedAction {

  public boolean authorized;

  @JWTAuthorizeMethod(httpMethods = {HTTPMethod.GET})
  public boolean authorize(JWT jwt) {
    return authorized;
  }

  public String get() {
    return "success";
  }
}

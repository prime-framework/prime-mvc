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

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.security.annotation.AnonymousAccess;

/**
 * Abstract class for managing API authentication and authorization using an authentication key and the HTTP methods
 * that are allowed.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractAPISecurityScheme implements SecurityScheme {
  protected final ActionInvocationStore actionInvocationStore;

  protected final HttpServletRequest request;

  protected AbstractAPISecurityScheme(ActionInvocationStore actionInvocationStore, HttpServletRequest request) {
    this.actionInvocationStore = actionInvocationStore;
    this.request = request;
  }

  @Override
  public void handle(String[] constraints) {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    if (actionInvocation.method.annotations.containsKey(AnonymousAccess.class)) {
      return;
    }

    String actionURI = actionInvocation.actionURI;
    String authenticationKey = authenticationKey();
    if (authenticationKey == null) {
      throw new UnauthenticatedException();
    }

    Set<String> allowedMethods = allowedMethods(authenticationKey, actionURI);
    if (!allowedMethods.contains(actionInvocation.method.httpMethod.toString())) {
      throw new UnauthorizedException();
    }
  }

  /**
   * Uses the authentication key and action URI to determine the allowed methods.
   *
   * @param authenticationKey The authentication key.
   * @param actionURI         The action URI.
   * @return The allowed methods.
   */
  protected abstract Set<String> allowedMethods(String authenticationKey, String actionURI);

  /**
   * @return The authentication key for the current request. The default implementation of this method uses the
   * Authorization header.
   */
  protected String authenticationKey() {
    return request.getHeader("Authorization");
  }
}

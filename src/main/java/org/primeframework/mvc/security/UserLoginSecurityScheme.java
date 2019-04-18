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

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import com.google.inject.Inject;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.servlet.ServletTools;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * A security scheme that authenticates and authorizes users using a UserLoginSecurityContext implementation.
 *
 * @author Brian Pontarelli
 */
public class UserLoginSecurityScheme implements SecurityScheme {
  private final MVCConfiguration configuration;

  private final UserLoginConstraintsValidator constraintsValidator;

  private final HTTPMethod method;

  private final HttpServletRequest request;

  private UserLoginSecurityContext userLoginSecurityContext;

  @Inject
  public UserLoginSecurityScheme(MVCConfiguration configuration, UserLoginConstraintsValidator constraintsValidator,
                                 HttpServletRequest request, HTTPMethod method) {
    this.configuration = configuration;
    this.constraintsValidator = constraintsValidator;
    this.request = request;
    this.method = method;
  }

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
    if (!constraintsValidator.validate(constraints)) {
      throw new UnauthorizedException();
    }

    if (!configuration.csrfEnabled()) {
      return;
    }

    // CSRF on POST only
    if (method == HTTPMethod.POST) {
      // Check for CSRF request origins
      String source = defaultIfNull(request.getHeader("Origin"), request.getHeader("Referer"));
      if (source == null) {
        throw new UnauthorizedException();
      }

      URI uri = ServletTools.getBaseURI(request);
      URI sourceURI = URI.create(source);
      // TODO Should we do equalsIgnoreCase on the scheme and host? the 'sourceURI' in theory could have a different case.
      // - I think in the getBaseURI we lowercase everything.
      if (!uri.getScheme().equals(sourceURI.getScheme()) || uri.getPort() != sourceURI.getPort() || !uri.getHost().equals(sourceURI.getHost())) {
        throw new UnauthorizedException();
      }

      // Handle CSRF tokens (for POST only) and remove it from the parameter map
      String token = CSRF.getSessionToken(request);
      String formToken = CSRF.getParameterToken(request);
      if (token != null && !token.equals(formToken)) {
        throw new UnauthorizedException();
      }
    }
  }

  @Inject(optional = true)
  public void setUserLoginSecurityContext(UserLoginSecurityContext userLoginSecurityContext) {
    this.userLoginSecurityContext = userLoginSecurityContext;
  }
}

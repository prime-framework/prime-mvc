/*
 * Copyright (c) 2015-2022, Inversoft Inc., All Rights Reserved
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

import java.net.URI;
import java.util.Set;

import com.google.inject.Inject;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.http.HTTPTools;
import org.primeframework.mvc.security.csrf.CSRFProvider;

/**
 * A security scheme that authenticates and authorizes users using a UserLoginSecurityContext implementation.
 *
 * @author Brian Pontarelli
 */
public class UserLoginSecurityScheme implements SecurityScheme {

  private static final Set<HTTPMethod> CSRF_METHODS = Set.of(HTTPMethod.POST, HTTPMethod.PUT, HTTPMethod.PATCH, HTTPMethod.DELETE);

  private final MVCConfiguration configuration;

  private final UserLoginConstraintsValidator constraintsValidator;

  private final CSRFProvider csrfProvider;

  private final HTTPMethod method;

  private final HTTPRequest request;

  private UserLoginSecurityContext userLoginSecurityContext;

  @Inject
  public UserLoginSecurityScheme(MVCConfiguration configuration, UserLoginConstraintsValidator constraintsValidator,
                                 CSRFProvider csrfProvider, HTTPRequest request, HTTPMethod method) {
    this.configuration = configuration;
    this.constraintsValidator = constraintsValidator;
    this.csrfProvider = csrfProvider;
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
    if (CSRF_METHODS.contains(method)) {
      // Check for CSRF request origins
      String source = HTTPTools.getOriginHeader(request);
      if (source == null) {
        throw new UnauthorizedException();
      }

      URI uri = URI.create(request.getBaseURL());
      URI sourceURI = URI.create(source);
      if (uri.getPort() != sourceURI.getPort() || !uri.getScheme().equalsIgnoreCase(sourceURI.getScheme()) || !uri.getHost().equalsIgnoreCase(sourceURI.getHost())) {
        throw new UnauthorizedException();
      }

      // Handle CSRF tokens (for modifying requests)
      if (!csrfProvider.validateRequest(request)) {
        // TODO : Should we do something less brute force here?
        throw new UnauthorizedException();
      }
    }
  }

  @Inject(optional = true)
  public void setUserLoginSecurityContext(UserLoginSecurityContext userLoginSecurityContext) {
    this.userLoginSecurityContext = userLoginSecurityContext;
  }
}

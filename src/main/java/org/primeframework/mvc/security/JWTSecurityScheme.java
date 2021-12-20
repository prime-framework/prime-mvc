/*
 * Copyright (c) 2016-2018, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Inject;
import io.fusionauth.jwt.domain.JWT;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.JWTMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.util.ReflectionUtils;

/**
 * Default implementation of the JWT security scheme.
 *
 * @author Daniel DeGroff
 */
public class JWTSecurityScheme implements SecurityScheme {
  protected final ActionInvocationStore actionInvocationStore;

  protected final JWTConstraintsValidator constraintsValidator;

  protected final HTTPRequest request;

  private final JWTSecurityContext jwtSecurityContext;

  @Inject
  public JWTSecurityScheme(ActionInvocationStore actionInvocationStore, JWTConstraintsValidator constraintsValidator,
                           JWTSecurityContext jwtSecurityContext, HTTPRequest request) {
    this.actionInvocationStore = actionInvocationStore;
    this.constraintsValidator = constraintsValidator;
    this.jwtSecurityContext = jwtSecurityContext;
    this.request = request;
  }

  @Override
  public void handle(String[] constraints) {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();

    JWT jwt = jwtSecurityContext.getJWT();
    if (!constraintsValidator.validate(jwt, constraints)) {
      throw new UnauthorizedException();
    }

    // The JWT has a valid signature and is not expired, further authorization is delegated to the action.
    ActionConfiguration actionConfiguration = actionInvocation.configuration;

    HTTPMethod method = request.getMethod();
    if (actionConfiguration.jwtAuthorizationMethods.containsKey(method)) {
      for (JWTMethodConfiguration methodConfig : actionConfiguration.jwtAuthorizationMethods.get(method)) {
        try {
          Boolean authorized = ReflectionUtils.invoke(methodConfig.method, actionInvocation.action, jwt);
          if (!authorized) {
            throw new UnauthorizedException();
          }
        } catch (ExpressionException e) {
          throw new PrimeException("Unable to invoke @JWTAuthorizeMethod on the class [" + actionConfiguration.actionClass + "]", e);
        }
      }
    }
  }
}

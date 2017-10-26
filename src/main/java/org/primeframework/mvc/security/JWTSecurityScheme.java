/*
 * Copyright (c) 2016-2017, Inversoft Inc., All Rights Reserved
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
import java.util.Map;

import org.primeframework.jwt.Verifier;
import org.primeframework.jwt.domain.InvalidJWTException;
import org.primeframework.jwt.domain.InvalidJWTSignatureException;
import org.primeframework.jwt.domain.JWT;
import org.primeframework.jwt.domain.JWTException;
import org.primeframework.jwt.domain.JWTExpiredException;
import org.primeframework.jwt.domain.JWTUnavailableForProcessingException;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.JWTMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.util.ReflectionUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Default implementation of the JWT security scheme.
 *
 * @author Daniel DeGroff
 */
public class JWTSecurityScheme implements SecurityScheme {

  protected final ActionInvocationStore actionInvocationStore;

  protected final JWTRequestAdapter jwtAdapter;

  protected final HttpServletRequest request;

  protected final Provider<Map<String, Verifier>> verifierProvider;

  @Inject
  public JWTSecurityScheme(ActionInvocationStore actionInvocationStore, JWTRequestAdapter jwtAdapter, HttpServletRequest request, Provider<Map<String, Verifier>> verifierProvider) {
    this.actionInvocationStore = actionInvocationStore;
    this.jwtAdapter = jwtAdapter;
    this.request = request;
    this.verifierProvider = verifierProvider;
  }

  @Override
  public void handle(String[] constraints) {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();

    try {
      String encodedJWT = jwtAdapter.getEncodedJWT();
      if (encodedJWT == null) {
        throw new UnauthenticatedException();
      }
      final JWT jwt = JWT.getDecoder().decode(encodedJWT, verifierProvider.get());

      // The JWT has a valid signature and is not expired, further authorization is delegated to the action.
      ActionConfiguration actionConfiguration = actionInvocation.configuration;

      HTTPMethod method = HTTPMethod.valueOf(request.getMethod().toUpperCase());
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

    } catch (InvalidJWTException | InvalidJWTSignatureException | JWTExpiredException | JWTUnavailableForProcessingException e) {
      jwtAdapter.invalidateJWT();
      throw new UnauthenticatedException();
    } catch (JWTException e) {
      throw new UnauthenticatedException();
    }
  }
}

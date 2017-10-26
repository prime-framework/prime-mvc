/*
 * Copyright (c) 2017, Inversoft Inc., All Rights Reserved
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
import java.util.Arrays;
import java.util.Map;

import org.primeframework.jwt.Verifier;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.AuthorizationMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.util.ReflectionUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * The scheme itself performs no authentication or authorization, it only calls the method(s) annotated with the {@link AuthorizeMethod}.
 *
 * @author Daniel DeGroff
 */
public class AuthorizeMethodScheme implements SecurityScheme {
  protected final ActionInvocationStore actionInvocationStore;

  protected final HttpServletRequest request;

  protected final Provider<Map<String, Verifier>> verifierProvider;

  @Inject
  public AuthorizeMethodScheme(ActionInvocationStore actionInvocationStore, HttpServletRequest request, Provider<Map<String, Verifier>> verifierProvider) {
    this.actionInvocationStore = actionInvocationStore;
    this.request = request;
    this.verifierProvider = verifierProvider;
  }

  @Override
  public void handle(String[] constraints) {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();

    Object[] parameters = new Object[]{};
    ActionConfiguration actionConfiguration = actionInvocation.configuration;

    HTTPMethod method = HTTPMethod.valueOf(request.getMethod().toUpperCase());
    if (actionConfiguration.authorizationMethods.containsKey(method)) {
      for (AuthorizationMethodConfiguration methodConfig : actionConfiguration.authorizationMethods.get(method)) {
        try {

          if (methodConfig.method.getParameterCount() == 1) {
            parameters = new Object[]{new AuthorizeSchemeData(Arrays.asList(constraints), method, request)};
          }

          Boolean authorized = ReflectionUtils.invoke(methodConfig.method, actionInvocation.action, parameters);
          if (!authorized) {
            throw new UnauthorizedException();
          }
        } catch (ExpressionException e) {
          throw new PrimeException("Unable to invoke @AuthorizeMethod on the class [" + actionConfiguration.actionClass + "]", e);
        }
      }
    }
  }
}

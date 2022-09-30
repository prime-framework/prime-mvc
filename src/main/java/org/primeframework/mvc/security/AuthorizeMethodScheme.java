/*
 * Copyright (c) 2017-2018, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Inject;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.AuthorizationMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.parameter.el.ExpressionException;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.util.ReflectionUtils;

/**
 * The scheme itself performs no authentication or authorization, it only calls the method(s) annotated with the
 * {@link AuthorizeMethod}.
 *
 * @author Daniel DeGroff
 */
public class AuthorizeMethodScheme implements SecurityScheme {
  protected final ActionInvocationStore actionInvocationStore;

  protected final HTTPRequest request;

  protected final VerifierProvider verifierProvider;

  @Inject
  public AuthorizeMethodScheme(ActionInvocationStore actionInvocationStore, HTTPRequest request,
                               VerifierProvider verifierProvider) {
    this.actionInvocationStore = actionInvocationStore;
    this.request = request;
    this.verifierProvider = verifierProvider;
  }

  @Override
  public void handle(String[] constraints) {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();

    Object[] parameters = new Object[]{};
    ActionConfiguration actionConfiguration = actionInvocation.configuration;

    HTTPMethod method = request.getMethod();
    // If this scheme is not configured for this method, throw UnauthenticatedException.
    //
    // - For example, using 'api' and 'authorize-method' schemes. The API key is omitted, so the next scheme used will be
    //   the 'authorize-method' (this scheme). In this case, if the Authorize Method is only configured for POST and
    //   this is a DELETE method, we need to throw an exception so we that we don't continue the current workflow.
    // - Throwing UnauthenticatedException will allow an additional scheme to be executed if configured.
    if (!actionConfiguration.authorizationMethods.containsKey(method)) {
      throw new UnauthenticatedException();
    }

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

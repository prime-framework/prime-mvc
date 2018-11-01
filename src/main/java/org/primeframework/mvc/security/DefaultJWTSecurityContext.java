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

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.InvalidJWTException;
import io.fusionauth.jwt.domain.InvalidJWTSignatureException;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.domain.JWTException;
import io.fusionauth.jwt.domain.JWTExpiredException;
import io.fusionauth.jwt.domain.JWTUnavailableForProcessingException;

/**
 * @author Daniel DeGroff
 */
public class DefaultJWTSecurityContext implements JWTSecurityContext {
  protected final JWTRequestAdapter requestAdapter;

  protected final Provider<Map<String, Verifier>> verifierProvider;

  @Inject
  public DefaultJWTSecurityContext(JWTRequestAdapter requestAdapter, Provider<Map<String, Verifier>> verifierProvider) {
    this.requestAdapter = requestAdapter;
    this.verifierProvider = verifierProvider;
  }

  @Override
  public JWT getJWT() {
    try {
      String encodedJWT = requestAdapter.getEncodedJWT();
      if (encodedJWT == null) {
        throw new UnauthenticatedException();
      }

      return JWT.getDecoder().decode(encodedJWT, verifierProvider.get());
    } catch (InvalidJWTException | InvalidJWTSignatureException | JWTExpiredException | JWTUnavailableForProcessingException e) {
      requestAdapter.invalidateJWT();
      throw new UnauthenticatedException();
    } catch (JWTException e) {
      throw new UnauthenticatedException();
    }
  }
}

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
package org.primeframework.mvc.security;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;

/**
 * Default JWT Extractor. Assumes the Authorization header looks like the following:
 * <pre>
 *   Authorization: JWT "XXXXXXXXXX.YYYYYYYYYY.ZZZZZZZZZZ"
 * </pre>
 * If you expect the JWT in a different authorization scheme, etc you should bind a different Extractor.
 *
 * @author Daniel DeGroff
 */
public class DefaultJWTExtractor implements JWTExtractor {

  protected final HttpServletRequest request;

  @Inject
  public DefaultJWTExtractor(HttpServletRequest request) {
    this.request = request;
  }

  @Override
  public String get() {
    String authorization = request.getHeader("Authorization");
    if (authorization.startsWith("JWT ")) {
      return authorization.substring("JWT ".length());
    }

    return null;
  }

  @Override
  public boolean requestContainsJWT() {
    String authorization = request.getHeader("Authorization");
    return authorization != null && authorization.startsWith("JWT ");
  }
}

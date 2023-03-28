/*
 * Copyright (c) 2016-2023, Inversoft Inc., All Rights Reserved
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
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;

/**
 * Default JWT Extractor. Assumes the Authorization header looks like the following:
 * <pre>
 *   Authorization: JWT "XXXXXXXXXX.YYYYYYYYYY.ZZZZZZZZZZ"
 * </pre>
 * or
 * <pre>
 *   Authorization: Bearer "XXXXXXXXXX.YYYYYYYYYY.ZZZZZZZZZZ"
 * </pre>
 * <p>
 * If an <code>Authorization</code> header is not found in the request next we'll look for a Cookie with a name of
 * <code>access_token</code>.
 * <p/>
 * If you expect the JWT in a different authorization scheme, or a different Cookie name, etc you should bind a different Extractor.
 *
 * @author Daniel DeGroff
 */
public class DefaultJWTRequestAdapter implements JWTRequestAdapter {
  protected final HTTPRequest request;

  protected final HTTPResponse response;

  @Inject
  public DefaultJWTRequestAdapter(HTTPRequest request, HTTPResponse response) {
    this.request = request;
    this.response = response;
  }

  @Override
  public String getEncodedJWT() {
    String authorization = request.getHeader("Authorization");
    if (authorization != null) {
      // Support Bearer and JWT scheme
      if (authorization.startsWith("Bearer ")) {
        return authorization.substring("Bearer ".length());
      } else if (authorization.startsWith("JWT ")) {
        return authorization.substring("JWT ".length());
      }
    }

    Cookie cookie = request.getCookie(cookieName());
    return cookie != null ? cookie.value : null;
  }

  /**
   * If we're using a JWT Cookie, attempt to get the browser to remove the cookie.
   */
  @Override
  public String invalidateJWT() {
    Cookie cookie = request.getCookie(cookieName());
    if (cookie != null) {
      String token = cookie.value;
      cookie.value = null;
      cookie.maxAge = 0L;
      cookie.path = "/";
      response.addCookie(cookie);
      return token;
    }

    return null;
  }

  @Override
  public boolean requestContainsJWT() {
    return getEncodedJWT() != null;
  }

  /**
   * @return the cookie name that is being managed by this adapter.
   */
  protected String cookieName() {
    return "access_token";
  }
}

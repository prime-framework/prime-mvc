/*
 * Copyright (c) 2020-2025, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security.csrf;

import io.fusionauth.http.server.HTTPRequest;

/**
 * @author Daniel DeGroff
 */
public interface CSRFProvider {
  /**
   * @return the CSRF header name
   */
  default String getHeaderName() {
    return "X-CSRF-Token";
  }

  /**
   * @return the CSRF parameter name
   */
  default String getParameterName() {
    return "primeCSRFToken";
  }

  /**
   * Return a CSRF token, it is up to the implementation to decide if the same value is returned during an entire
   * session or if a new value is returned each time one is requested.
   *
   * @param request the HTTP request.
   * @return a CSRF token that can be sent to the client to be set into a hidden field on a form.
   */
  String getToken(HTTPRequest request);

  /**
   * Return the CSRF token provided on the HTTP request.
   *
   * @param request the HTTP request
   * @return the CSRF token value if found in the HTTP request.
   */
  default String getTokenFromRequest(HTTPRequest request) {
    // Check the request for the CSRF token in the header and parameter.
    String headerToken = request.getHeader(getHeaderName());
    String paramToken = request.getParameter(getParameterName());

    // If both the header and parameter are provided, they must match else return null so we fail
    if (headerToken != null && paramToken != null) {
      if (!headerToken.equals(paramToken)) {
        return null;
      }
    }

    // By default, we return the parameter token if it exists, otherwise the header token.
    return paramToken != null ? paramToken : headerToken;
  }

  /**
   * Validate a request using the CSRF token.
   *
   * @param request the HTTP request
   * @return true if the request is valid, false if not.
   */
  boolean validateRequest(HTTPRequest request);
}

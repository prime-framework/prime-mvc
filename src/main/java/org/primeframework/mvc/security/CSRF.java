/*
 * Copyright (c) 2019, Inversoft Inc., All Rights Reserved
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
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

import org.primeframework.mock.servlet.MockHttpServletRequest;

/**
 * Helper class for CSRF tokens and session keys.
 *
 * @author Brian Pontarelli
 */
public final class CSRF {
  public static final String CSRF_PARAMETER_KEY = "primeCSRFToken";

  public static final String CSRF_SESSION_KEY = "prime-mvc-security-csrf-token";

  public static String generateCSRFToken() {
    SecureRandom random = new SecureRandom();
    byte[] buf = new byte[32];
    random.nextBytes(buf);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
  }

  public static String getParameterToken(HttpServletRequest request) {
    return request.getParameter(CSRF.CSRF_PARAMETER_KEY);
  }

  public static String getSessionToken(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      return (String) session.getAttribute(CSRF.CSRF_SESSION_KEY);
    }

    return null;
  }

  public static void setParameterToken(MockHttpServletRequest request) {
    String token = getSessionToken(request);
    if (token != null) {
      request.setParameter(CSRF_PARAMETER_KEY, token);
    }
  }

  public static void storeToken(HttpSession session) {
    session.setAttribute(CSRF_SESSION_KEY, generateCSRFToken());
  }
}

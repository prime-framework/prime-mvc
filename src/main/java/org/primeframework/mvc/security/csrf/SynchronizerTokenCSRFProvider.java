/*
 * Copyright (c) 2020, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * A CSRF Provider leveraging the Synchronizer Token Pattern as defined by OWASP.
 *
 * @author Daniel DeGroff
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#synchronizer-token-pattern">
 * OWASP CSRF Cheat Sheet - Synchronizer Token Pattern
 * </a>
 */
public class SynchronizerTokenCSRFProvider implements CSRFProvider {
  public static final String CSRF_SESSION_KEY = "prime-mvc-security-csrf-token";

  @Override
  public String getToken(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      String token = (String) session.getAttribute(CSRF_SESSION_KEY);
      return token == null ? generateToken() : token;
    }

    return null;
  }

  @Override
  public boolean validateRequest(HttpServletRequest request) {
    String token = getToken(request);
    String formToken = request.getParameter(CSRF_PARAMETER_KEY);
    return token == null || token.equals(formToken);
  }

  private String generateToken() {
    SecureRandom random = new SecureRandom();
    byte[] buf = new byte[32];
    random.nextBytes(buf);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
  }
}

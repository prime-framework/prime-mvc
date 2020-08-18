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

import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.security.CookieConfig;
import org.primeframework.mvc.security.Encryptor;

/**
 * A CSRF Provider leveraging the Encryption based Token Pattern  as defined by OWASP.
 *
 * @author Daniel DeGroff
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#encryption-based-token-pattern">
 * OWASP CSRF Cheat Sheet - Encryption based Token Pattern
 * </a>
 */
@SuppressWarnings("unused")
public abstract class BaseEncryptionBasedTokenCSRFProvider implements CSRFProvider {
  private final CookieConfig cookie;

  private final Encryptor encryptor;

  protected BaseEncryptionBasedTokenCSRFProvider(CookieConfig cookie, Encryptor encryptor) {
    this.cookie = cookie;
    this.encryptor = encryptor;
  }

  @Override
  public String getToken(HttpServletRequest request) {
    String sessionId = cookie.get(request);
    return sessionId == null ? null : generateToken(sessionId);
  }

  @Override
  public boolean validateRequest(HttpServletRequest request) {
    CSRFToken token = decrypt(request.getParameter(CSRF_PARAMETER_KEY));
    if (token == null) {
      return false;
    }

    // The sessionId must match.
    String sessionId = cookie.get(request);
    if (!token.sid.equals(sessionId)) {
      return false;
    }

    // Token can be up to 15 minutes old. This is totally made up. But the OWASP guide suggests using a timestamp as a 'nonce'
    // of sorts. So we could optionally ask the configuration for how long this should good for or just assume 10 minutes is long enough
    // to prevent a replay attack.
    long now = System.currentTimeMillis();
    return (token.instant + 900_000) >= now;
  }

  private CSRFToken decrypt(String s) {
    try {
      return encryptor.decrypt(CSRFToken.class, s);
    } catch (Exception e) {
      return null;
    }
  }

  private String generateToken(String sessionId) {
    try {
      CSRFToken token = new CSRFToken();
      token.sid = sessionId;
      token.instant = System.currentTimeMillis();

      return encryptor.encrypt(token);
    } catch (Exception e) {
      throw new ErrorException("error", e);
    }
  }

  private static class CSRFToken {
    public long instant;

    public String sid;
  }
}

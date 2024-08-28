/*
 * Copyright (c) 2020-2024, Inversoft Inc., All Rights Reserved
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

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.util.CookieTools;

/**
 * A CSRF Provider leveraging the Encryption based Token Pattern as defined by OWASP.
 *
 * @author Daniel DeGroff
 * @see <a
 *     href="https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#encryption-based-token-pattern">OWASP
 *     CSRF Cheat Sheet - Encryption based Token Pattern</a>
 */
@SuppressWarnings("unused")
public class DefaultEncryptionBasedTokenCSRFProvider implements CSRFProvider {
  private final Encryptor encryptor;

  private final ObjectMapper objectMapper;

  private final UserLoginSecurityContext securityContext;

  // Default to 15 minutes;
  private long nonceTimeout = TimeUnit.MINUTES.toMillis(15);

  @Inject
  public DefaultEncryptionBasedTokenCSRFProvider(Encryptor encryptor, ObjectMapper objectMapper,
                                                 UserLoginSecurityContext securityContext) {
    this.encryptor = encryptor;
    this.objectMapper = objectMapper;
    this.securityContext = securityContext;
  }

  @Override
  public String getToken(HTTPRequest request) {
    // Check to see if we have already generated the token, we store it in the request attribute.
    String csrfToken = (String) request.getAttribute(getParameterName());
    if (csrfToken == null) {
      String sessionId = securityContext.getSessionId();
      if (sessionId == null) {
        return null;
      }

      csrfToken = generateToken(sessionId);
      request.setAttribute(getParameterName(), csrfToken);
    }

    return csrfToken;
  }

  @Override
  public boolean validateRequest(HTTPRequest request) {
    CSRFToken token = decrypt(request.getParameter(getParameterName()));
    if (token == null) {
      return false;
    }

    // The sessionId must match the value provided in the HTTP request.
    String sessionId = securityContext.getSessionId();
    if (!token.sid.equals(sessionId)) {
      return false;
    }

    // If the 'nonce' is expired fail.
    long now = System.currentTimeMillis();
    return (token.instant + nonceTimeout) >= now;
  }

  /**
   * Optionally override the default nonce timeout. A longer duration is less secure but offers a potentially better
   * user experience. A shorter value is more secure but may impact the user experience.
   * <p>
   * This duration is essentially how long you want a user to be able to sit on a form and wait before submitting the
   * form.
   *
   * @param nonceTimeout the nonce timeout in milliseconds.
   */
  protected void setNonceTimeout(long nonceTimeout) {
    this.nonceTimeout = nonceTimeout;
  }

  private CSRFToken decrypt(String s) {
    try {
      return CookieTools.fromJSONCookie(s, CSRFToken.class, true, true, encryptor, objectMapper);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Generate an encrypted anti-CSRF token.
   * <p>
   * Note that the token value contains the current sessionId. This means that this token is strongly associated with
   * the user's current sessionId. This is important because it means this token is only valid or usable for a
   * particular user session.
   * <p>
   * Encrypting this cookie means it is functionally immutable on the client side.
   *
   * @param sessionId the user's sessionId
   * @return a new token, never null.
   */
  private String generateToken(String sessionId) {
    try {
      CSRFToken token = new CSRFToken();
      token.sid = sessionId;
      token.instant = System.currentTimeMillis();

      return CookieTools.toJSONCookie(token, false, true, encryptor, objectMapper);
    } catch (Exception e) {
      throw new ErrorException("error", e);
    }
  }

  private static class CSRFToken {
    public long instant;

    public String sid;
  }
}

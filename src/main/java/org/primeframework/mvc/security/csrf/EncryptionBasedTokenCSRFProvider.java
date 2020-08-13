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

import javax.crypto.Cipher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.CipherProvider;

/**
 * A CSRF Provider leveraging the Encryption based Token Pattern  as defined by OWASP.
 *
 * @author Daniel DeGroff
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#encryption-based-token-pattern">
 * OWASP CSRF Cheat Sheet - Encryption based Token Pattern
 * </a>
 */
public class EncryptionBasedTokenCSRFProvider implements CSRFProvider {
  private final CipherProvider cipherProvider;

  private final MVCConfiguration configuration;

  private final ObjectMapper objectMapper;

  @Inject
  public EncryptionBasedTokenCSRFProvider(CipherProvider cipherProvider, MVCConfiguration configuration,
                                          ObjectMapper objectMapper) {
    this.cipherProvider = cipherProvider;
    this.configuration = configuration;
    this.objectMapper = objectMapper;
  }

  @Override
  public String getToken(HttpServletRequest request) {
    String sessionId = getSessionId(request);
    return sessionId == null ? null : generateToken(sessionId);
  }

  @Override
  public boolean validateRequest(HttpServletRequest request) {
    CSRFToken token = decrypt(request.getParameter(CSRF_PARAMETER_KEY));
    if (token == null) {
      return false;
    }

    // The sessionId must match.
    if (!token.sid.equals(getSessionId(request))) {
      return false;
    }

    // Token can be up to 10 minutes old. This is totally made up. But the OWASP guide suggests using a timestamp as a 'nonce'
    // of sorts. So we could optionally ask the configuration for how long this should good for or just assume 10 minutes is long enough
    // to prevent a replay attack.
    long now = System.currentTimeMillis();
    return (token.instant + 600_000) >= now;
  }

  private CSRFToken decrypt(String s) {
    try {
      byte[] bytes = Base64.getUrlDecoder().decode(s.getBytes(StandardCharsets.UTF_8));
      Cipher cipher = cipherProvider.getDecryptor();
      byte[] result = new byte[cipher.getOutputSize(bytes.length)];
      int resultLength = cipher.update(bytes, 0, bytes.length, result, 0);
      resultLength += cipher.doFinal(result, resultLength);
      return objectMapper.readerFor(CSRFToken.class).readValue(Arrays.copyOfRange(result, 0, resultLength));
    } catch (Exception e) {
      return null;
    }
  }

  private String generateToken(String sessionId) {
    try {
      CSRFToken token = new CSRFToken();
      token.sid = sessionId;
      token.instant = System.currentTimeMillis();

      String value = objectMapper.writer().writeValueAsString(token);
      Cipher cipher = cipherProvider.getEncryptor();
      byte[] input = value.getBytes(StandardCharsets.UTF_8);
      byte[] result = new byte[cipher.getOutputSize(input.length)];
      int resultLength = cipher.update(input, 0, input.length, result, 0);
      resultLength += cipher.doFinal(result, resultLength);

      return Base64.getUrlEncoder().encodeToString(Arrays.copyOfRange(result, 0, resultLength));
    } catch (Exception e) {
      throw new ErrorException("error", e);
    }
  }

  private String getSessionId(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(configuration.userLoginSecurityContextCookieName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }

  private static class CSRFToken {
    public long instant;

    public String sid;
  }
}

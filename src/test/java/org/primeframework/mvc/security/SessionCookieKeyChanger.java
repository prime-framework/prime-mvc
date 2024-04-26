/*
 * Copyright (c) 2024-2024, Inversoft Inc., All Rights Reserved
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.Cookie;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.util.CookieTools;

/**
 * Decrypts and re-encrypts a cookie with a different encryption key
 */
public class SessionCookieKeyChanger {
  private final Encryptor encryptor;

  private final ObjectMapper objectMapper;

  @Inject
  SessionCookieKeyChanger(Encryptor encryptor, ObjectMapper objectMapper) {
    this.encryptor = encryptor;
    this.objectMapper = objectMapper;
  }

  /**
   * Decrypts the cookie using existing Guice encryptor, and re-encrypts with a new key
   *
   * @param cookie - mutated in place cookie
   */
  public void changeIt(Cookie cookie) {
    try {
      UserIdSessionContext existingContainer = CookieTools.fromJSONCookie(cookie.value, MockUserIdSessionContext.class, true, encryptor, objectMapper);
      byte[] result = objectMapper.writeValueAsBytes(existingContainer);
      var config = new MockConfiguration();
      config.regenerateCookieEncryptionKey();
      // guarantee we use a different key than this.encryptor
      var differentKeyCipherProvider = new DefaultCipherProvider(config);
      var differentKeyEncryptor = new DefaultEncryptor(differentKeyCipherProvider);
      cookie.value = CookieTools.toCookie(result, true, true, differentKeyEncryptor);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

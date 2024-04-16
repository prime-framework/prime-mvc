/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.Cookie;
import org.primeframework.mvc.security.DefaultCipherProvider;
import org.primeframework.mvc.security.DefaultEncryptor;
import org.primeframework.mvc.security.Encryptor;
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
      SerializedSessionContainer existingContainer = CookieTools.fromJSONCookie(cookie.value, SerializedSessionContainer.class, true, encryptor, objectMapper);
      byte[] result = objectMapper.writeValueAsBytes(existingContainer);
      var config = new SessionTestPrimeConfig();
      config.changeCookieEncryptionKey();
      // guarantee we use a different key than this.encryptor
      var differentKeyCipherProvider = new DefaultCipherProvider(config);
      var differentKeyEncryptor = new DefaultEncryptor(differentKeyCipherProvider);
      cookie.value = CookieTools.toCookie(result, true, true, differentKeyEncryptor);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

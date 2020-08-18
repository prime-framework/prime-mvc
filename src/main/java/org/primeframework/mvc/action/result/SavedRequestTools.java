/*
 * Copyright (c) 2016-2020, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.servlet.http.Cookie;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.SavedRequestException;
import org.primeframework.mvc.security.saved.SavedHttpRequest;

/**
 * Toolkit to help with Saved Request stuff.
 *
 * @author Brian Pontarelli
 */
public class SavedRequestTools {
  /**
   * Creates a Cookie Object for the given SavedHttpRequest object.
   *
   * @param savedRequest  The Saved Request.
   * @param configuration THe MVC Configuration that is used to determine the cookie name.
   * @param encryptor     The encryptor used to encrypt the cookie
   * @return The cookie.
   */
  public static Cookie toCookie(SavedHttpRequest savedRequest, MVCConfiguration configuration,
                                Encryptor encryptor) {
    try {
      String encrypted = encryptor.encrypt(savedRequest);
      Cookie cookie = new Cookie(configuration.savedRequestCookieName(), encrypted);
      cookie.setPath("/"); // Turn the cookie on for everything since we have no clue what URI will Re-execute the Saved Request
      cookie.setMaxAge(-1); // Be explicit
      cookie.setVersion(1); // Be explicit
      cookie.setHttpOnly(true);
      // Set to secure when schema is 'https'
      cookie.setSecure(savedRequest.uri.startsWith("https"));
      return cookie;
    } catch (JsonProcessingException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | ShortBufferException e) {
      throw new SavedRequestException(e);
    }
  }
}

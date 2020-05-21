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
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.CipherProvider;
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
   * @param objectMapper  The ObjectMapper that is used to create the JSON for the Saved Request.
   * @param configuration THe MVC Configuration that is used to determine the cookie name.
   * @return The cookie.
   */
  public static Cookie toCookie(SavedHttpRequest savedRequest, ObjectMapper objectMapper,
                                MVCConfiguration configuration,
                                CipherProvider cipherProvider) {
    try {
      String value = objectMapper.writer().writeValueAsString(savedRequest);
      Cipher cipher = cipherProvider.getEncryptor();
      byte[] input = value.getBytes(StandardCharsets.UTF_8);
      byte[] result = new byte[cipher.getOutputSize(input.length)];
      int resultLength = cipher.update(input, 0, input.length, result, 0);
      resultLength += cipher.doFinal(result, resultLength);

      String encoded = Base64.getEncoder().encodeToString(Arrays.copyOfRange(result, 0, resultLength));
      Cookie cookie = new Cookie(configuration.savedRequestCookieName(), encoded);
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

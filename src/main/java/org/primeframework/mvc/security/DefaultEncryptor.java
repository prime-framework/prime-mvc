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
package org.primeframework.mvc.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * @author Daniel DeGroff
 */
public class DefaultEncryptor implements Encryptor {
  private final CipherProvider cipherProvider;

  private final ObjectMapper objectMapper;

  @Inject
  public DefaultEncryptor(CipherProvider cipherProvider, ObjectMapper objectMapper) {
    this.cipherProvider = cipherProvider;
    this.objectMapper = objectMapper;
  }

  @Override
  public <T> T decrypt(Class<T> type, String s)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, ShortBufferException, BadPaddingException, IllegalBlockSizeException, IOException {
    byte[] decrypted = decrypt(s);
    return objectMapper.readerFor(type).readValue(decrypted);
  }

  @Override
  public <T> T decrypt(TypeReference<?> type, String s)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, ShortBufferException, BadPaddingException, IllegalBlockSizeException, IOException {
    byte[] decrypted = decrypt(s);
    return objectMapper.readerFor(type).readValue(decrypted);
  }

  @Override
  public String encrypt(Object o)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, JsonProcessingException, ShortBufferException, BadPaddingException, IllegalBlockSizeException {
    byte[] input = objectMapper.writer().writeValueAsBytes(o);

    // The first 16 bytes are the IV
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);

    Cipher cipher = cipherProvider.getEncryptor(iv);
    byte[] result = new byte[cipher.getOutputSize(input.length)];
    int resultLength = cipher.update(input, 0, input.length, result, 0);
    resultLength += cipher.doFinal(result, resultLength);

    // Combine the IV + encrypted result
    byte[] combined = new byte[resultLength + iv.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(result, 0, combined, iv.length, resultLength);

    return new String(Base64.getUrlEncoder().encode(combined), StandardCharsets.UTF_8);
  }

  private byte[] decrypt(String s)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, ShortBufferException, BadPaddingException, IllegalBlockSizeException {
    byte[] bytes = Base64.getUrlDecoder().decode(s.getBytes(StandardCharsets.UTF_8));

    // The first 16 bytes are the IV
    byte[] iv = new byte[16];
    System.arraycopy(bytes, 0, iv, 0, 16);

    Cipher cipher = cipherProvider.getDecryptor(iv);

    byte[] json = Arrays.copyOfRange(bytes, 16, bytes.length);
    byte[] result = new byte[cipher.getOutputSize(json.length)];
    int resultLength = cipher.update(json, 0, result.length, result, 0);
    resultLength += cipher.doFinal(result, resultLength);

    return Arrays.copyOfRange(result, 0, resultLength);
  }
}

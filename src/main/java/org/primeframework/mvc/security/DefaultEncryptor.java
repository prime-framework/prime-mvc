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
import java.util.Arrays;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    byte[] bytes = Base64.getUrlDecoder().decode(s.getBytes(StandardCharsets.UTF_8));
    Cipher cipher = cipherProvider.getDecryptor();
    byte[] result = new byte[cipher.getOutputSize(bytes.length)];
    int resultLength = cipher.update(bytes, 0, bytes.length, result, 0);
    resultLength += cipher.doFinal(result, resultLength);
    return objectMapper.readerFor(type).readValue(Arrays.copyOfRange(result, 0, resultLength));
  }

  @Override
  public String encrypt(Object o)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, JsonProcessingException, ShortBufferException, BadPaddingException, IllegalBlockSizeException {
    String json = objectMapper.writer().writeValueAsString(o);
    Cipher cipher = cipherProvider.getEncryptor();
    byte[] input = json.getBytes(StandardCharsets.UTF_8);
    byte[] result = new byte[cipher.getOutputSize(input.length)];
    int resultLength = cipher.update(input, 0, input.length, result, 0);
    resultLength += cipher.doFinal(result, resultLength);
    return Base64.getUrlEncoder().encodeToString(Arrays.copyOfRange(result, 0, resultLength));
  }
}

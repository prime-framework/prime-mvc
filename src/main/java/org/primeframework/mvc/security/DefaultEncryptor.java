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

import javax.crypto.Cipher;
import java.security.SecureRandom;
import java.util.Arrays;

import com.google.inject.Inject;

/**
 * @author Daniel DeGroff
 */
public class DefaultEncryptor implements Encryptor {
  private final CipherProvider cipherProvider;

  @Inject
  public DefaultEncryptor(CipherProvider cipherProvider) {
    this.cipherProvider = cipherProvider;
  }

  @Override
  public byte[] decrypt(byte[] bytes) throws Exception {
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

  @Override
  public byte[] encrypt(byte[] bytes) throws Exception {
    // The first 16 bytes are the IV
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);

    Cipher cipher = cipherProvider.getEncryptor(iv);
    byte[] result = new byte[cipher.getOutputSize(bytes.length)];
    int resultLength = cipher.update(bytes, 0, bytes.length, result, 0);
    resultLength += cipher.doFinal(result, resultLength);

    // Combine the IV + encrypted result
    byte[] combined = new byte[resultLength + iv.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(result, 0, combined, iv.length, resultLength);

    return combined;
  }
}

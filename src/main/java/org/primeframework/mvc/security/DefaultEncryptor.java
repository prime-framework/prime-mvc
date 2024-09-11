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
package org.primeframework.mvc.security;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author Daniel DeGroff
 */
public class DefaultEncryptor implements Encryptor {
  private final CipherProvider cbcCipherProvider;

  private final CipherProvider gcmCipherProvider;

  @Inject
  public DefaultEncryptor(@Named("CBC") CipherProvider cbcCipherProvider, @Named("GCM") CipherProvider gcmCipherProvider) {
    this.cbcCipherProvider = cbcCipherProvider;
    this.gcmCipherProvider = gcmCipherProvider;
  }

  @Override
  public byte[] decrypt(byte[] bytes) throws Exception {
    // The first 16 bytes contain the initialization vector (IV)
    byte[] iv = Arrays.copyOfRange(bytes, 0, 16);
    // The remainder contains the encrypted bytes
    byte[] encryptedBytes = Arrays.copyOfRange(bytes, 16, bytes.length);

    try {
      // Attempt to decrypt using AES/GCM
      Cipher cipher = gcmCipherProvider.getDecryptor(iv);
      return doDecrypt(encryptedBytes, cipher);
    } catch (GeneralSecurityException gcmException) {
      // If GCM failed, try decrypting in CBC mode
      try {
        Cipher cipher = cbcCipherProvider.getDecryptor(iv);
        return doDecrypt(encryptedBytes, cipher);
      } catch (GeneralSecurityException cbcException) {
        // If CBC also failed, re-throw the original GCM exception
        throw gcmException;
      }
    }
  }

  @Override
  public byte[] encrypt(byte[] bytes) throws Exception {
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);
    Cipher cipher = gcmCipherProvider.getEncryptor(iv);
    return doEncrypt(bytes, cipher);
  }

  /**
   * Decrypt a set of bytes using the provided cipher
   *
   * @param bytes  The bytes to decrypt
   * @param cipher The cipher for decryption
   * @return the decrypted bytes
   */
  private byte[] doDecrypt(byte[] bytes, Cipher cipher) throws GeneralSecurityException {
    byte[] result = new byte[cipher.getOutputSize(bytes.length)];
    int resultLength = cipher.update(bytes, 0, bytes.length, result, 0);
    resultLength += cipher.doFinal(result, resultLength);
    return Arrays.copyOfRange(result, 0, resultLength);
  }

  /**
   * Encrypt a set of bytes using the provided cipher
   *
   * @param bytes  The bytes to encrypt
   * @param cipher The cipher for encryption
   * @return the encrypted bytes
   */
  private byte[] doEncrypt(byte[] bytes, Cipher cipher) throws GeneralSecurityException {
    // Allocate array for encryption result
    byte[] result = new byte[cipher.getOutputSize(bytes.length)];
    int resultLength = cipher.update(bytes, 0, bytes.length, result, 0);
    resultLength += cipher.doFinal(result, resultLength);

    // Extract IV from cipher and combine with encrypted result
    byte[] iv = cipher.getIV();
    byte[] combined = new byte[iv.length + resultLength];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(result, 0, combined, iv.length, resultLength);

    return combined;
  }
}

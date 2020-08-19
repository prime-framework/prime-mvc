/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Provider for getting a Cipher instance that can be used for encryption and any other security as necessary.
 *
 * @author Brian Pontarelli
 */
public interface CipherProvider {
  /**
   * Return a cipher used for decrypting.
   *
   * @param iv the initialization vector
   * @return the Cipher object.
   * @throws NoSuchPaddingException             when this happens.
   * @throws NoSuchAlgorithmException           when this happens.
   * @throws InvalidAlgorithmParameterException when this happens.
   * @throws InvalidKeyException                when this happens.
   */
  Cipher getDecryptor(byte[] iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException;

  /**
   * Return a cipher used for decrypting.
   *
   * @return the Cipher object.
   * @throws NoSuchPaddingException             when this happens.
   * @throws NoSuchAlgorithmException           when this happens.
   * @throws InvalidAlgorithmParameterException when this happens.
   * @throws InvalidKeyException                when this happens.
   * @deprecated Prefer the user of {@link #getDecryptor(byte[])}
   */
  @Deprecated
  Cipher getDecryptor()
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException;

  /**
   * Return a cipher used for encrypting.
   *
   * @return the Cipher object.
   * @throws NoSuchPaddingException             when this happens.
   * @throws NoSuchAlgorithmException           when this happens.
   * @throws InvalidAlgorithmParameterException when this happens.
   * @throws InvalidKeyException                when this happens.
   * @deprecated Prefer the use of {@link #getEncryptor(byte[])}
   */
  @Deprecated
  Cipher getEncryptor()
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException;


  /**
   * Return a cipher used for encrypting.
   *
   * @param iv the initialization vector
   * @return the Cipher object.
   * @throws NoSuchPaddingException             when this happens.
   * @throws NoSuchAlgorithmException           when this happens.
   * @throws InvalidAlgorithmParameterException when this happens.
   * @throws InvalidKeyException                when this happens.
   */
  Cipher getEncryptor(byte[] iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException;
}

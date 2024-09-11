/*
 * Copyright (c) 2016-2024, Inversoft Inc., All Rights Reserved
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

/**
 * Encrypt and decrypt stuff.
 *
 * @author Daniel DeGroff
 */
public interface Encryptor {
  /**
   * Decrypt a set of bytes.
   * <p>
   * This will attempt to decrypt with GCM and then CBC if that fails.
   *
   * @param bytes The bytes to decrypt.
   * @return The decrypted bytes.
   */
  byte[] decrypt(byte[] bytes) throws Exception;

  /**
   * Encrypt a set of bytes using AES.
   *
   * @param bytes The bytes to encrypt.
   * @return The encrypted bytes.
   */
  byte[] encrypt(byte[] bytes) throws Exception;
}

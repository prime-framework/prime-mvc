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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Encrypt and decrypt stuff.
 *
 * @author Daniel DeGroff
 */
public interface Encryptor {
  /**
   * Decrypt a string.
   *
   * @param type the type of object used for de-serialization.
   * @param s    the encrypted string returned by the {{@link #encrypt(Object)}} method on this interface.
   * @param <T>  the type
   * @return a object of the provided type.
   * @throws InvalidAlgorithmParameterException thrown when this happens.
   * @throws NoSuchAlgorithmException           thrown when this happens.
   * @throws InvalidKeyException                thrown when this happens.
   * @throws NoSuchPaddingException             thrown when this happens.
   * @throws ShortBufferException               thrown when this happens.
   * @throws BadPaddingException                thrown when this happens.
   * @throws IllegalBlockSizeException          thrown when this happens.
   * @throws IOException                        thrown when this happens.
   */
  <T> T decrypt(Class<T> type, String s)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, ShortBufferException, BadPaddingException, IllegalBlockSizeException, IOException;

  /**
   * Decrypt a string.
   *
   * @param type the type of object used for de-serialization.
   * @param s    the encrypted string returned by the {{@link #encrypt(Object)}} method on this interface.
   * @param <T>  the type
   * @return a object of the provided type.
   * @throws InvalidAlgorithmParameterException thrown when this happens.
   * @throws NoSuchAlgorithmException           thrown when this happens.
   * @throws InvalidKeyException                thrown when this happens.
   * @throws NoSuchPaddingException             thrown when this happens.
   * @throws ShortBufferException               thrown when this happens.
   * @throws BadPaddingException                thrown when this happens.
   * @throws IllegalBlockSizeException          thrown when this happens.
   * @throws IOException                        thrown when this happens.
   */
  <T> T decrypt(TypeReference<?> type, String s)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, ShortBufferException, BadPaddingException, IllegalBlockSizeException, IOException;

  /**
   * Encrypt an object and return a URL safe encoded string.
   *
   * @param o the object to encrypt
   * @return an encoded URL safe encrypted string version of the provided object.
   * @throws InvalidAlgorithmParameterException thrown when this happens.
   * @throws NoSuchAlgorithmException           thrown when this happens.
   * @throws InvalidKeyException                thrown when this happens.
   * @throws NoSuchPaddingException             thrown when this happens.
   * @throws JsonProcessingException            thrown when this happens.
   * @throws ShortBufferException               thrown when this happens.
   * @throws BadPaddingException                thrown when this happens.
   * @throws IllegalBlockSizeException          thrown when this happens.
   */
  String encrypt(Object o)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, JsonProcessingException, ShortBufferException, BadPaddingException, IllegalBlockSizeException;
}

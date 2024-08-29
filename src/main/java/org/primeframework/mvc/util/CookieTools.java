/*
 * Copyright (c) 2022-2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.util;

import java.util.Arrays;
import java.util.Base64;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.primeframework.mvc.security.Encryptor;

public final class CookieTools {
  /**
   * The current highest bit-mask.
   */
  public static final int HIGHEST_BIT_MASK = 0x03;

  /**
   * Processes a cookie value and calls a Function to convert it to a meaningful value
   * for the application (or Prime).
   *
   * @param value          The cookie value.
   * @param encryptedIfOld Whether a legacy cookie was likely encrypted or not.
   * @param encryptor      The encryptor to use if needed.
   * @param oldFunction    The function to call if the cookie looks legacy.
   * @param newFunction    The function to call if the cookie looks new (contains our magic header).
   * @param <T>            The type that the function returns.
   * @return The value or null if the cookie is empty.
   * @throws Exception If the operation fails.
   */
  @Deprecated(forRemoval = true, since = "4.22.13")
  public static <T> T fromCookie(String value, boolean encryptedIfOld, Encryptor encryptor,
                                 ThrowingFunction<byte[], T> oldFunction, ThrowingFunction<byte[], T> newFunction) throws Exception {
    return fromCookie(value, false, encryptedIfOld, encryptor, oldFunction, newFunction);
  }

  /**
   * Processes a cookie value and calls a Function to convert it to a meaningful value for the application (or Prime).
   *
   * @param value              The cookie value.
   * @param encryptionRequired Whether encryption is required or not
   * @param encryptedIfOld     Whether a legacy cookie was likely encrypted or not.
   * @param encryptor          The encryptor to use if needed.
   * @param oldFunction        The function to call if the cookie looks legacy.
   * @param newFunction        The function to call if the cookie looks new (contains our magic header).
   * @param <T>                The type that the function returns.
   * @return The value or null if the cookie is empty.
   * @throws Exception If the operation fails.
   */
  public static <T> T fromCookie(String value, boolean encryptionRequired, boolean encryptedIfOld, Encryptor encryptor,
                                 ThrowingFunction<byte[], T> oldFunction, ThrowingFunction<byte[], T> newFunction)
      throws Exception {
    if (value == null || value.isBlank()) {
      return null;
    }

    byte[] result = Base64.getUrlDecoder().decode(value);
    if (result.length < 5) {
      // Old style of cookie which was not compressed but could be encrypted
      if (encryptedIfOld) {
        result = encryptor.decrypt(result);
      }

      return oldFunction.apply(result);
    }

    if (result[0] != 0x42 || result[1] != 0x42 || result[2] != 0x42 || result[3] > HIGHEST_BIT_MASK) {
      // Old style of cookie which was not compressed but could be encrypted
      if (encryptedIfOld) {
        result = encryptor.decrypt(result);
      }

      return oldFunction.apply(result);
    }

    boolean encrypt = (result[3] & 0x01) == 0x01; // First bit is encrypted
    boolean compress = (result[3] & 0x02) == 0x02; // Second bit is compressed
    result = Arrays.copyOfRange(result, 4, result.length);

    if (encryptionRequired && !encrypt) {
      throw new EncryptionException("Encryption is required to decrypt cookie but a non-encrypted cookie was presented");
    } else if (encrypt) {
      result = encryptor.decrypt(result);
    }

    if (compress) {
      result = Compressor.decompress(result);
    }

    return newFunction.apply(result);
  }

  /**
   * Processes a cookie value and converts it to an object
   *
   * @param value          The cookie value.
   * @param type           The type of object to convert to.
   * @param encryptedIfOld If the cookie header indicates it is an older cookie, then we only decrypt it if this is
   *                       true.
   * @param encryptor      The encryptor to use for decrypting the cookie.
   * @param objectMapper   The ObjectMapper used to convert from JSON to an object.
   * @param <T>            The type to convert to.
   * @return The object or null if the cookie couldn't be converted.
   * @throws Exception If the operation fails.
   */
  @Deprecated(forRemoval = true, since = "4.22.13")
  public static <T> T fromJSONCookie(String value, TypeReference<T> type,
                                     boolean encryptedIfOld, Encryptor encryptor,
                                     ObjectMapper objectMapper) throws Exception {
    ThrowingFunction<byte[], T> read = r -> objectMapper.readerFor(type).readValue(r);
    return fromCookie(value, false, encryptedIfOld, encryptor, read, read);
  }

  /**
   * Processes a cookie value and converts it to an object.
   *
   * @param value              The cookie value.
   * @param type               The type of object to convert to.
   * @param encryptionRequired Whether encryption is required or not
   * @param encryptedIfOld     If the cookie header indicates it is an older cookie, then we only decrypt it if this is
   *                           true.
   * @param encryptor          The encryptor to use for decrypting the cookie.
   * @param objectMapper       The ObjectMapper used to convert from JSON to an object.
   * @param <T>                The type to convert to.
   * @return The object or null if the cookie couldn't be converted.
   * @throws Exception If the operation fails.
   */
  public static <T> T fromJSONCookie(String value, TypeReference<T> type, boolean encryptionRequired,
                                     boolean encryptedIfOld, Encryptor encryptor,
                                     ObjectMapper objectMapper) throws Exception {
    ThrowingFunction<byte[], T> read = r -> objectMapper.readerFor(type).readValue(r);
    return fromCookie(value, encryptionRequired, encryptedIfOld, encryptor, read, read);
  }

  /**
   * Processes a cookie value and converts it to an object.
   *
   * @param value          The cookie value.
   * @param type           The type of object to convert to.
   * @param encryptedIfOld If the cookie header indicates it is an older cookie, then we only decrypt it if this is
   *                       true.
   * @param encryptor      The encryptor to use for decrypting the cookie.
   * @param objectMapper   The ObjectMapper used to convert from JSON to an object.
   * @param <T>            The type to convert to.
   * @return The object or null if the cookie couldn't be converted.
   * @throws Exception If the operation fails.
   */
  @Deprecated(forRemoval = true, since = "4.22.13")
  public static <T> T fromJSONCookie(String value, Class<T> type, boolean encryptedIfOld, Encryptor encryptor,
                                     ObjectMapper objectMapper) throws Exception {
    ThrowingFunction<byte[], T> read = r -> objectMapper.readerFor(type).readValue(r);
    return fromCookie(value, false, encryptedIfOld, encryptor, read, read);
  }

  /**
   * Processes a cookie value and converts it to an object.
   *
   * @param value              The cookie value.
   * @param type               The type of object to convert to.
   * @param encryptionRequired Whether encryption is required or not
   * @param encryptedIfOld     If the cookie header indicates it is an older cookie, then we only decrypt it if this is
   *                           true.
   * @param encryptor          The encryptor to use for decrypting the cookie.
   * @param objectMapper       The ObjectMapper used to convert from JSON to an object.
   * @param <T>                The type to convert to.
   * @return The object or null if the cookie couldn't be converted.
   * @throws Exception If the operation fails.
   */
  public static <T> T fromJSONCookie(String value, Class<T> type, boolean encryptionRequired, boolean encryptedIfOld, Encryptor encryptor,
                                     ObjectMapper objectMapper) throws Exception {
    ThrowingFunction<byte[], T> read = r -> objectMapper.readerFor(type).readValue(r);
    return fromCookie(value, encryptionRequired, encryptedIfOld, encryptor, read, read);
  }

  /**
   * Handles the encoding of a set of bytes into a cookie. This optionally compresses and encrypts the bytes. It also
   * adds a header to the bytes that indicate how the cookie was processed.
   *
   * @param value     The bytes.
   * @param compress  Whether to compress the bytes.
   * @param encrypt   Whether to encrypt the bytes.
   * @param encryptor The encryptor if needed.
   * @return The String value of the cookie (always URL safe base 64 encoded).
   * @throws Exception If the operation fails.
   */
  public static String toCookie(byte[] value, boolean compress, boolean encrypt, Encryptor encryptor)
      throws Exception {
    if (compress) {
      value = Compressor.compress(value);
    }

    if (encrypt) {
      value = encryptor.encrypt(value);
    }

    byte[] header = new byte[4];
    header[0] = 0x42;
    header[1] = 0x42;
    header[2] = 0x42;
    header[3] |= encrypt ? 0x01 : 0x00;
    header[3] |= compress ? 0x02 : 0x00;

    byte[] buf = new byte[value.length + 4];
    System.arraycopy(header, 0, buf, 0, header.length);
    System.arraycopy(value, 0, buf, 4, value.length);
    return Base64.getUrlEncoder().encodeToString(buf);
  }

  /**
   * Converts the given object to a cookie value.
   *
   * @param value        The object.
   * @param compress     Whether the value is compressed.
   * @param encrypt      Whether the value is encrypted.
   * @param encryptor    The encryptor to use for encrypting the cookie.
   * @param objectMapper The ObjectMapper used to convert from the object to JSON.
   * @return The object converted to a cookie value.
   * @throws Exception If the operation fails.
   */
  public static String toJSONCookie(Object value, boolean compress, boolean encrypt, Encryptor encryptor,
                                    ObjectMapper objectMapper) throws Exception {
    if (value == null) {
      return null;
    }

    byte[] result = objectMapper.writeValueAsBytes(value);
    return toCookie(result, compress, encrypt, encryptor);
  }
}

/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import org.primeframework.mvc.config.AbstractMVCConfiguration;

/**
 * This is a mock configuration object that delegates to another MVCConfiguration instance, but also allows specific
 * properties to be mocked out.
 *
 * @author Brian Pontarelli
 */
public class MockConfiguration extends AbstractMVCConfiguration {
  public boolean allowUnknownParameters;

  public AlgorithmParameterSpec cookieEncryptionIV;

  public Key cookieEncryptionKey;

  public boolean csrfEnabled;

  public int freemarkerCheckSeconds;

  public int l10nReloadSeconds;

  public MockConfiguration() {
    try {
      SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
      byte[] ivBytes = new byte[16];
      randomSecureRandom.nextBytes(ivBytes);
      this.cookieEncryptionIV = new IvParameterSpec(ivBytes);

      byte[] keyBytes = new byte[16];
      randomSecureRandom.nextBytes(keyBytes);
      this.cookieEncryptionKey = new SecretKeySpec(keyBytes, "AES");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public MockConfiguration(int freemarkerCheckSeconds, int l10nReloadSeconds, boolean allowUnknownParameters,
                           boolean csrfEnabled) {
    this();
    this.freemarkerCheckSeconds = freemarkerCheckSeconds;
    this.l10nReloadSeconds = l10nReloadSeconds;
    this.allowUnknownParameters = allowUnknownParameters;
    this.csrfEnabled = csrfEnabled;
  }

  @Override
  public boolean allowUnknownParameters() {
    return allowUnknownParameters;
  }

  @Override
  public AlgorithmParameterSpec cookieEncryptionIV() {
    return cookieEncryptionIV;
  }

  @Override
  public Key cookieEncryptionKey() {
    return cookieEncryptionKey;
  }

  @Override
  public boolean csrfEnabled() {
    return csrfEnabled;
  }

  @Override
  public int l10nReloadSeconds() {
    return l10nReloadSeconds;
  }

  @Override
  public int templateCheckSeconds() {
    return freemarkerCheckSeconds;
  }
}
/*
 * Copyright (c) 2012-2023, Inversoft Inc., All Rights Reserved
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

import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Path;
import java.security.Key;
import java.security.SecureRandom;

import org.primeframework.mvc.config.AbstractMVCConfiguration;

/**
 * This is a mock configuration object that delegates to another MVCConfiguration instance, but also allows specific
 * properties to be mocked out.
 *
 * @author Brian Pontarelli
 */
public class MockConfiguration extends AbstractMVCConfiguration {
  public boolean allowUnknownParameters;

  public boolean autoHTMLEscapingEnabled = true;

  public Key cookieEncryptionKey;

  public boolean csrfEnabled;

  public int freemarkerCheckSeconds;

  public int l10nReloadSeconds;

  public MockConfiguration() {
    regenerateCookieEncryptionKey();
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
  public boolean autoHTMLEscapingEnabled() {
    return autoHTMLEscapingEnabled;
  }

  @Override
  public Path baseDirectory() {
    return Path.of("src/test/web");
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
  public String localeCookieName() {
    return localeCookieName;
  }

  public MockConfiguration regenerateCookieEncryptionKey() {
    byte[] keyBytes = new byte[16];
    new SecureRandom().nextBytes(keyBytes);
    this.cookieEncryptionKey = new SecretKeySpec(keyBytes, "AES");
    return this;
  }

  @Override
  public int templateCheckSeconds() {
    return freemarkerCheckSeconds;
  }
}

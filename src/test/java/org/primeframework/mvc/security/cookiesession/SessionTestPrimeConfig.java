/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.SecureRandom;

import org.primeframework.mvc.config.AbstractMVCConfiguration;

/**
 * We have test only templates we need to use
 */
public class SessionTestPrimeConfig extends AbstractMVCConfiguration {
  private SecretKeySpec cookieEncryptionKey;

  public SessionTestPrimeConfig() {
    changeCookieEncryptionKey();
  }

  public void changeCookieEncryptionKey() {
    byte[] keyBytes = new byte[16];
    new SecureRandom().nextBytes(keyBytes);
    this.cookieEncryptionKey = new SecretKeySpec(keyBytes, "AES");
  }

  @Override
  public boolean allowUnknownParameters() {
    return false;
  }

  @Override
  public boolean csrfEnabled() {
    return true;
  }

  @Override
  public Path baseDirectory() {
    return Paths.get("src/test/web");
  }

  @Override
  public Key cookieEncryptionKey() {
    return this.cookieEncryptionKey;
  }

  @Override
  public int l10nReloadSeconds() {
    return 0;
  }

  @Override
  public int templateCheckSeconds() {
    return 0;
  }
}

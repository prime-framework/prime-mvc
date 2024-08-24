/*
 * Copyright (c) 2021-2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.security.Encryptor;

/**
 * This is the request scope which fetches and stores values in a cookie.
 *
 * @author Daniel DeGroff
 */
public class ManagedCookieScope extends BaseManagedCookieScope<ManagedCookie> {
  @Inject
  public ManagedCookieScope(HTTPRequest request, HTTPResponse response, Encryptor encryptor,
                            ObjectMapper objectMapper) {
    super(request, response, encryptor, objectMapper);
  }

  @Override
  protected boolean compress(ManagedCookie scope) {
    return scope.compress();
  }

  @Override
  protected boolean encrypt(ManagedCookie scope) {
    return scope.encrypt();
  }

  @Override
  protected boolean encryptionRequired(ManagedCookie scope) {
    return scope.encryptionRequired();
  }

  @Override
  protected String getCookieName(String fieldName, ManagedCookie scope) {
    return "##field-name##".equals(scope.name()) ? fieldName : scope.name();
  }

  @Override
  protected boolean neverNull(ManagedCookie scope) {
    return scope.neverNull();
  }

  @Override
  protected void setCookieValues(Cookie cookie, ManagedCookie scope) {
    cookie.maxAge = scope.maxAge();
    cookie.sameSite = scope.sameSite();
  }
}

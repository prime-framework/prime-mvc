/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
import org.primeframework.mvc.scope.annotation.ManagedSessionCookie;
import org.primeframework.mvc.security.Encryptor;

/**
 * This is the request scope which fetches and stores values in a cookie.
 *
 * @author Daniel DeGroff
 */
public class ManagedSessionCookieScope extends BaseManagedCookieScope<ManagedSessionCookie> {
  @Inject
  public ManagedSessionCookieScope(HTTPRequest request, HTTPResponse response, Encryptor encryptor,
                                   ObjectMapper objectMapper) {
    super(request, response, encryptor, objectMapper);
  }

  @Override
  protected boolean compress(ManagedSessionCookie scope) {
    return scope.compress();
  }

  @Override
  protected boolean encrypt(ManagedSessionCookie scope) {
    return scope.encrypt();
  }

  @Override
  protected String getCookieName(String fieldName, ManagedSessionCookie scope) {
    return "##field-name##".equals(scope.name()) ? fieldName : scope.name();
  }

  @Override
  protected boolean neverNull(ManagedSessionCookie scope) {
    return scope.neverNull();
  }

  @Override
  protected void setCookieValues(Cookie cookie, ManagedSessionCookie scope) {
    cookie.sameSite = scope.sameSite();
  }
}

/*
 * Copyright (c) 2021-2022, Inversoft Inc., All Rights Reserved
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
import org.primeframework.mvc.scope.annotation.BrowserSession;
import org.primeframework.mvc.security.Encryptor;

/**
 * This is the session scope which fetches and stores objects as JSON in a cookie.
 *
 * @author Brian Pontarelli
 */
public class BrowserSessionScope extends BaseBrowserSessionScope<BrowserSession> {
  @Inject
  public BrowserSessionScope(HTTPRequest request, HTTPResponse response, Encryptor encryptor,
                             ObjectMapper objectMapper) {
    super(request, response, encryptor, objectMapper);
  }

  @Override
  protected boolean compress(BrowserSession scope) {
    return scope.compress();
  }

  @Override
  protected boolean encrypt(BrowserSession scope) {
    return scope.encrypt();
  }

  /**
   * Using the annotation or the current action invocation, this determines the name of the action used to get the
   * action session.
   *
   * @param fieldName the field name
   * @param scope     The scope annotation.
   * @return The action class name.
   */
  @Override
  protected String getCookieName(String fieldName, BrowserSession scope) {
    return "##field-name##".equals(scope.name()) ? fieldName : scope.name();
  }

  @Override
  protected void setCookieValues(Cookie cookie, BrowserSession scope) {
    cookie.sameSite = scope.sameSite();
  }
}

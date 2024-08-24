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
package org.example.action;

import io.fusionauth.http.Cookie;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.scope.annotation.ManagedCookie;

/**
 * @author Brian Pontarelli
 */
@Action
public class LegacyManagedCookieAction {
  // since CompressedManagedCookieAction sets encrypt to false, the 2nd HTTP request of
  // ManagedCookieTest.compressed_only_cookie, which is a POST, sets an unencrypted
  // cookie value. the get to this action which follows should be able to read it if
  // and ONLY if encryptionRequired is set to false here
  @ManagedCookie(encryptionRequired = false)
  public Cookie cookie;

  public String value;

  public String get() {
    return "input";
  }

  public String post() {
    cookie.value = value;
    return "input";
  }
}

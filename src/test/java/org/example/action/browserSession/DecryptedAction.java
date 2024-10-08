/*
 * Copyright (c) 2024-2024, Inversoft Inc., All Rights Reserved
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
package org.example.action.browserSession;

import org.example.domain.User;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.scope.annotation.BrowserSession;

@Action
@Redirect(code = "next", uri = "/browser-session/second")
public class DecryptedAction {
  // idea is to have this action serialize user to a decrypted cookie
  // and then attempt to decrypt using SecondAction, which
  // requires an encrypted cookie
  @BrowserSession(encrypt = false)
  public User user;

  public String get() {
    user = new User();
    user.setName("Brian Pontarelli");
    return "next";
  }
}

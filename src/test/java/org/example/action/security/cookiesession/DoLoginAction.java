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
package org.example.action.security.cookiesession;

import java.util.UUID;

import com.google.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.security.MockUser;
import org.primeframework.mvc.security.UserLoginSecurityContext;

@Action
@Forward(code = "error", status = 500)
public class DoLoginAction {
  @Inject
  private UserLoginSecurityContext context;

  @Inject
  private MessageStore messageStore;

  public String get() {
    var user = new MockUser("bob");
    user.id = UUID.randomUUID();
    context.login(user);
    return "success";
  }
}

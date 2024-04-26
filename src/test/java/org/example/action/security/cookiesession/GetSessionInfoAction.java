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

import java.util.Optional;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.security.BaseUserIdCookieSecurityContext;
import org.primeframework.mvc.security.MockUser;
import org.primeframework.mvc.security.UserLoginSecurityContext;

@Action
public class GetSessionInfoAction {
  public String currentUser;

  public String loggedIn;

  public String sessionId;

  public String update;

  public String updateNewUserEmail;

  public String userInRequest;

  @Inject
  private UserLoginSecurityContext context;

  @Inject
  private HTTPRequest httpRequest;

  public String get() throws Exception {
    if (update != null && update.equals("yes")) {
      context.getCurrentUser();
      context.updateUser(new MockUser(updateNewUserEmail));
    }
    try {
      this.currentUser = Optional.ofNullable(context.getCurrentUser())
                                 .map(u -> ((MockUser) u).email)
                                 .orElse("(no user)");
    } catch (Exception e) {
      this.currentUser = "failed to fetch, reason - " + e.getMessage();
      this.sessionId = "error";
      this.loggedIn = "error";
      this.userInRequest = "error";
      return "success";
    }
    this.sessionId = Optional.ofNullable(context.getSessionId())
                             .orElse("(no session)");
    this.loggedIn = context.isLoggedIn() ? "yes" : "no";
    var userObjectInRequest = httpRequest.getAttribute(BaseUserIdCookieSecurityContext.UserKey);
    if (userObjectInRequest == null) {
      this.userInRequest = "(nothing)";
    } else {
      var user = (MockUser) userObjectInRequest;
      this.userInRequest = user.email;
    }
    return "success";
  }
}

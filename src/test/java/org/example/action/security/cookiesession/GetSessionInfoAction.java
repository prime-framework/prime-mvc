/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.example.action.security.cookiesession;

import java.util.Optional;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.cookiesession.MockUser;
import org.primeframework.mvc.security.BaseUserIdCookieSecurityContext;

@Action
public class GetSessionInfoAction {
  @Inject
  private UserLoginSecurityContext context;

  @Inject
  private HTTPRequest httpRequest;

  public String currentUser;

  public String sessionId;

  public String loggedIn;

  public String userInRequest;

  public String update;

  public String updateNewUserEmail;

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
/*
 * Copyright (c) 2020, Inversoft Inc., All Rights Reserved
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
package org.example.action.store;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;

/**
 * @author Daniel DeGroff
 */
@Action
@ReexecuteSavedRequest(uri = "/store/")
public class LoginAction {
  private final HttpServletRequest request;

  @Inject
  public LoginAction(HttpServletRequest request) {
    this.request = request;
  }

  public String get() {
    HttpSession session = request.getSession(false);
    if (session != null) {
      Boolean isLoggedIn = (Boolean) session.getAttribute("LoggedIn");
      if (isLoggedIn != null) {
        return "success";
      }
    }

    return "input";
  }

  public String post() {
    HttpSession session = request.getSession(true);
    session.setAttribute("LoggedIn", true);
    return "success";
  }
}
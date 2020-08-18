/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import org.example.action.BaseCookieAction.Foo;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.scope.annotation.ActionCookie;

/**
 * @author Daniel DeGroff
 */
@Action
@Status
public class CookieAction extends BaseCookieAction<Foo> {
  public boolean clearSaveMe;

  public Cookie[] cookies;

  public String name;

  @Inject
  public HttpServletRequest request;

  @Inject
  public HttpServletResponse response;

  @ActionCookie
  public String saveMe;

  public String value;

  public String get() {
    cookies = request.getCookies();
    return "input";
  }

  public String post() {
    if (value != null) {
      Cookie cookie = new Cookie(name, value);
      response.addCookie(cookie);
    }

    if (clearSaveMe) {
      saveMe = null;
    }

    return "success";
  }
}

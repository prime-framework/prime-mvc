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

import java.util.List;

import com.google.inject.Inject;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.example.action.BaseCookieAction.Foo;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.scope.annotation.BrowserActionSession;

/**
 * @author Daniel DeGroff
 */
@Action
@Status
@Redirect(code = "redirect", uri = "/cookie")
public class CookieAction extends BaseCookieAction<Foo> {
  private final MessageStore messageStore;

  public boolean addMessage;

  public boolean blowChunks;

  public boolean clearSaveMe;

  public List<Cookie> cookies;

  public String name;

  @Inject
  public HTTPRequest request;

  @Inject
  public HTTPResponse response;

  @BrowserActionSession
  public String saveMe;

  public String value;

  @Inject
  public CookieAction(MessageStore messageStore) {
    this.messageStore = messageStore;
  }

  public String get() {
    if (blowChunks) {
      throw new CookieErrorException();
    }

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

    if (addMessage) {
      messageStore.add(new SimpleMessage(MessageType.INFO, "[NobodyDrinkTheBeer]", "Nobody drink the beer, the beer has gone bad!"));
      return "redirect";
    }

    return "success";
  }

  public static class CookieErrorException extends ErrorException {
    public CookieErrorException() {
      super("input");
    }
  }
}

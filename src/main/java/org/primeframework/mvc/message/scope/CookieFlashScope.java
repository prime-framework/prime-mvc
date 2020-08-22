/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.message.scope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.util.FlashMessageCookie;

/**
 * This is the flash scope which stores messages in a Cookie under the flash key. It fetches values from the
 * HttpServletRequest under the same key as well as the Cookie under that key. This allows for flash messages to be
 * migrated from the cookie to the request during request handling so that they are not persisted in the cookie forever.
 * However, it also allows flash values to be retrieved during the initial request from the cookie.
 *
 * @author Daniel DeGroff
 */
public class CookieFlashScope implements FlashScope {
  private final FlashMessageCookie cookie;

  private final HttpServletRequest request;

  @Inject
  public CookieFlashScope(MVCConfiguration configuration, ObjectMapper objectMapper, HttpServletRequest request,
                          HttpServletResponse response) {
    cookie = new FlashMessageCookie(configuration.messageFlashScopeCookieName(), objectMapper, request, response);
    this.request = request;
  }

  @Override
  public void add(Message message) {
    cookie.update(c -> c.add(message));
  }

  @Override
  public void addAll(Collection<Message> messages) {
    if (messages.isEmpty()) {
      return;
    }

    cookie.update(c -> c.addAll(messages));
  }

  @Override
  public void clear() {
    request.removeAttribute(FlashScope.KEY);
    cookie.delete();
  }

  @Override
  public List<Message> get() {
    @SuppressWarnings("unchecked")
    List<Message> requestList = (List<Message>) request.getAttribute(FlashScope.KEY);
    if (requestList != null) {
      return requestList;
    } else {
      return cookie.get();
    }
  }

  /**
   * Moves the flash from the session to the request.
   */
  public void transferFlash() {
    List<Message> messages = cookie.get();
    if (messages.size() > 0) {
      cookie.delete();
      request.setAttribute(FlashScope.KEY, messages);
    }
  }
}

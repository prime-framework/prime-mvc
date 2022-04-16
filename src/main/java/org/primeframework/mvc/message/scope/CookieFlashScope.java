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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.security.Encryptor;
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

  @Inject
  public CookieFlashScope(Encryptor encryptor, MVCConfiguration configuration, ObjectMapper objectMapper,
                          HTTPRequest request, HTTPResponse response) {
    this.cookie = new FlashMessageCookie(encryptor, configuration.messageFlashScopeCookieName(), objectMapper, request, response);
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
    cookie.delete();
  }

  @Override
  public List<Message> get() {
    return new ArrayList<>(cookie.get());
  }
}

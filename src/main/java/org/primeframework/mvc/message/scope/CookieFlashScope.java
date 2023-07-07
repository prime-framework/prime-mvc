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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.CookieTools;

/**
 * This is the flash scope which stores messages in a Cookie under the flash key. It fetches values from the HttpServletRequest under the same key as
 * well as the Cookie under that key. This allows for flash messages to be migrated from the cookie to the request during request handling so that
 * they are not persisted in the cookie forever. However, it also allows flash values to be retrieved during the initial request from the cookie.
 *
 * @author Daniel DeGroff
 */
public class CookieFlashScope implements FlashScope {
  private final Encryptor encryptor;

  private final List<Message> messages;

  private final String name;

  private final ObjectMapper objectMapper;

  private final HTTPRequest request;

  private final HTTPResponse response;

  @Inject
  public CookieFlashScope(Encryptor encryptor, MVCConfiguration configuration, ObjectMapper objectMapper,
                          HTTPRequest request, HTTPResponse response) {
    this.encryptor = encryptor;
    this.name = configuration.messageFlashScopeCookieName();
    this.objectMapper = objectMapper;
    this.request = request;
    this.response = response;

    Cookie cookie = request.getCookie(name);
    if (cookie != null) {
      messages = deserialize(cookie.value);
    } else {
      messages = new ArrayList<>();
    }
  }

  @Override
  public void add(Message message) {
    addAll(List.of(message));
  }

  @Override
  public void addAll(Collection<Message> newMessages) {
    // Don't write the cookie if the messages are empty
    if (newMessages == null || newMessages.isEmpty()) {
      return;
    }

    messages.addAll(newMessages);

    try {
      String value = CookieTools.toJSONCookie(messages, true, true, encryptor, objectMapper);
      Cookie cookie = new Cookie(name, value);
      cookie.httpOnly = true;
      cookie.path = "/";
      cookie.secure = "https".equalsIgnoreCase(request.getScheme());
      response.addCookie(cookie);
    } catch (Exception e) {
      throw new ErrorException(e);
    }
  }

  @Override
  public void clear() {
    Cookie cookie = request.getCookie(name);
    if (cookie != null) {
      cookie.value = null;
      cookie.maxAge = 0L;
      cookie.path = "/";
      response.addCookie(cookie);
      request.deleteCookie(name);
    }

    messages.clear();
  }

  @Override
  public List<Message> get() {
    return new ArrayList<>(messages);
  }

  private List<Message> deserialize(String s) {
    try {
      // @formatter:off
      List<Message> messages = CookieTools.fromJSONCookie(s, new TypeReference<>() {}, true, encryptor, objectMapper);
      // @formatter:on

      if (messages == null) {
        return new ArrayList<>();
      }

      return new ArrayList<>(messages);
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }
}

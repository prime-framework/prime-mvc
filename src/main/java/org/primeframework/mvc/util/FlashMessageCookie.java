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
package org.primeframework.mvc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.http.Cookie;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.http.MutableHTTPRequest;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.security.Encryptor;

/**
 * @author Daniel DeGroff
 */
public class FlashMessageCookie extends AbstractCookie {
  private final Encryptor encryptor;

  private final List<Message> messages;

  private final String name;

  public FlashMessageCookie(Encryptor encryptor, String name, HTTPRequest request, HTTPResponse response) {
    super(request, response);
    this.encryptor = encryptor;
    this.name = name;

    Cookie cookie = request.getCookie(name);
    if (cookie != null) {
      messages = deserialize(cookie.value);
    } else {
      messages = new ArrayList<>();
    }
  }

  /**
   * Sends a cookie delete message back in the HTTP response and clears the local list of messages. Any future calls to
   * {@link #get()} will return an empty list to indicate that the flash has been cleared. Similarly, the cookie is
   * completely removed from the request so that it isn't reloaded.
   */
  public void delete() {
    deleteCookie(name);
    ((MutableHTTPRequest) request).deleteCookie(name);
    messages.clear();
  }

  /**
   * @return A copy of the messages from this request.
   */
  public List<Message> get() {
    return new ArrayList<>(messages);
  }

  public void update(Consumer<List<Message>> consumer) {
    consumer.accept(messages);
    String value = serialize(messages);
    addSecureHTTPOnlySessionCookie(name, value);
  }

  private List<Message> deserialize(String s) {
    try {
      return encryptor.decrypt(new TypeReference<List<Message>>() {
      }, s);
    } catch (Exception e) {
      return null;
    }
  }

  private String serialize(List<Message> messages) {
    try {
      return encryptor.encrypt(messages);
    } catch (Exception e) {
      throw new ErrorException(e);
    }
  }
}

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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.security.Encryptor;

/**
 * @author Daniel DeGroff
 */
public class FlashMessageCookie extends AbstractCookie {
  private final Encryptor encryptor;

  private final String name;

  public FlashMessageCookie(Encryptor encryptor, String name, HttpServletRequest request,
                            HttpServletResponse response) {
    super(request, response);
    this.encryptor = encryptor;
    this.name = name;
  }

  public void delete() {
    deleteCookie(name);

    // Ensure we do not return the cookie again after it has been deleted within the same request.
    request.setAttribute(name + "Deleted", true);
  }

  public List<Message> get() {
    // Ensure we do not return the cookie again after it has been deleted within the same request.
    if (request.getAttribute(name + "Deleted") != null) {
      return new ArrayList<>(0);
    }

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(name)) {
          List<Message> flash = deserialize(cookie.getValue());
          if (flash != null) {
            return flash;
          }
        }
      }
    }

    try {
      return new ArrayList<>(0);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void update(Consumer<List<Message>> consumer) {
    List<Message> messages = get();
    consumer.accept(messages);
    String value = serialize(messages);

    addSecureHttpOnlySessionCookie(name, value);
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

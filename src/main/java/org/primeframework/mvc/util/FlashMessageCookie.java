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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.message.Message;

/**
 * @author Daniel DeGroff
 */
public class FlashMessageCookie {
  private final String name;

  private final ObjectMapper objectMapper;

  private final HttpServletRequest request;

  private final HttpServletResponse response;

  public FlashMessageCookie(String name, ObjectMapper objectMapper, HttpServletRequest request,
                            HttpServletResponse response) {
    this.name = name;
    this.objectMapper = objectMapper;
    this.request = request;
    this.response = response;
  }

  public void delete() {
    Cookie cookie = new Cookie(name, null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  public List<Message> get() {
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
      return new ArrayList<>();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void update(Consumer<List<Message>> consumer) {
    List<Message> messages = get();
    consumer.accept(messages);
    String value = serialize(messages);
    Cookie cookie = new Cookie(name, value);
    cookie.setSecure("https".equals(defaultIfNull(request.getHeader("X-Forwarded-Proto"), request.getScheme()).toLowerCase()));
    cookie.setHttpOnly(true);
    cookie.setMaxAge(-1);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  private String defaultIfNull(String string, String defaultString) {
    return string == null ? defaultString : string;
  }

  private List<Message> deserialize(String s) {
    try {
      String decoded = URLDecoder.decode(s, "UTF-8");
      return objectMapper.readerFor(new TypeReference<List<Message>>() {
      }).readValue(decoded);
    } catch (JsonProcessingException | UnsupportedEncodingException e) {
      return null;
    }
  }


  private String serialize(List<Message> messages) {
    try {
      return URLEncoder.encode(objectMapper.writeValueAsString(messages), "UTF-8");
    } catch (JsonProcessingException | UnsupportedEncodingException e) {
      throw new ErrorException(e);
    }
  }
}

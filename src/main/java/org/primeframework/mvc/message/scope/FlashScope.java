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
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.util.FlashCookie;

/**
 * This is the flash scope which stores messages in the HttpSession under the flash key. It fetches values from the
 * HttpServletRequest under the same key as well as the HttpSession under that key. This allows for flash messages to be
 * migrated from the session to the request during request handling so that they are not persisted in the session
 * forever. However, it also allows flash values to be retrieved during the initial request from the session.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class FlashScope extends AbstractSessionScope implements Scope {
  public static final String KEY = "primeFlashMessages";

  private final FlashCookie<List<Message>> cookie;

  private final HttpServletRequest request;

  @Inject
  public FlashScope(MVCConfiguration configuration, ObjectMapper objectMapper, HttpServletRequest request,
                    HttpServletResponse response) {
    super(request, KEY);
    this.request = request;
    if (configuration.useCookieForFlashScope()) {
      cookie = new FlashCookie<>(configuration.messageFlashScopeCookieName(), objectMapper, request, response, new TypeReference<List<Message>>() {
      }, ArrayList::new);
    } else {
      cookie = null;
    }
  }

  @Override
  public void add(Message message) {
    if (cookie != null) {
      List<Message> flash = cookie.get();
      flash.add(message);
      cookie.update(flash);
    } else {
      addMessage(message);
    }
  }

  @Override
  public void addAll(Collection<Message> messages) {
    if (cookie != null) {
      List<Message> flash = cookie.get();
      flash.addAll(messages);
      cookie.update(flash);
    } else {
      addAllMessages(messages);
    }
  }

  @Override
  public void clear() {
    if (cookie != null) {
      cookie.delete();
    } else {
      request.removeAttribute(KEY);
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.removeAttribute(KEY);
      }
    }
  }

  @Override
  public List<Message> get() {
    if (cookie != null) {
      return cookie.get();
    } else {
      List<Message> messages = new ArrayList<>();
      List<Message> requestList = (List<Message>) request.getAttribute(KEY);
      if (requestList != null) {
        messages.addAll(requestList);
      }

      HttpSession session = request.getSession(false);
      if (session != null) {
        synchronized (session) {
          List<Message> sessionList = (List<Message>) session.getAttribute(KEY);
          if (sessionList != null) {
            messages.addAll(sessionList);
          }
        }
      }

      return messages;
    }
  }

  /**
   * Moves the flash from the session to the request.
   */
  public void transferFlash() {
    if (cookie != null) {
      List<Message> flash = cookie.get();
      if (flash.size() > 0) {
        cookie.delete();
        request.setAttribute(KEY, flash);
      }
    } else {
      HttpSession session = request.getSession(false);
      if (session != null) {
        synchronized (session) {
          List<Message> messages = (List<Message>) session.getAttribute(KEY);
          if (messages != null) {
            session.removeAttribute(KEY);
            request.setAttribute(KEY, messages);
          }
        }
      }
    }
  }
}

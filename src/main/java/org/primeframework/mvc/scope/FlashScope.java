/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.scope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.scope.annotation.Flash;
import org.primeframework.mvc.util.FlashCookie;

/**
 * This is the flash scope which stores values in the HttpSession inside a Map under the flash key
 * <code>primeFlash</code>. It fetches values from both the HttpServletRequest and HttpSession under that key. This
 * allows for flash objects to be migrated from the session to the request during request handling so that they are not
 * persisted in the session forever. However, it also allows flash values to be retrieved during the initial request
 * from the session.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class FlashScope implements Scope<Flash> {
  public static final String FLASH_KEY = "primeFlash";

  private final FlashCookie<Map<String, Object>> cookie;

  private final HttpServletRequest request;

  @Inject
  public FlashScope(MVCConfiguration configuration, ObjectMapper objectMapper, HttpServletRequest request,
                    HttpServletResponse response) {
    this.request = request;
    if (configuration.useCookieForFlashScope()) {
      cookie = new FlashCookie<>(configuration.flashScopeCookieName(), objectMapper, request, response, new TypeReference<Map<String, Object>>() {
      }, LinkedHashMap::new);
    } else {
      cookie = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Object get(String fieldName, Flash scope) {
    String key = scope.value().equals("##field-name##") ? fieldName : scope.value();

    if (cookie != null) {
      synchronized (cookie) {
        Map<String, Object> flash = cookie.get();
        return flash.get(key);
      }
    } else {
      Map<String, Object> flash = (Map<String, Object>) request.getAttribute(FLASH_KEY);

      if (flash == null || !flash.containsKey(key)) {
        HttpSession session = request.getSession(false);
        if (session != null) {
          synchronized (session) {
            flash = (Map<String, Object>) session.getAttribute(FLASH_KEY);
          }
        }
      }

      if (flash == null) {
        return null;
      }

      return flash.get(key);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void set(String fieldName, Object value, Flash scope) {
    String key = scope.value().equals("##field-name##") ? fieldName : scope.value();

    if (cookie != null) {
      synchronized (cookie) {
        Map<String, Object> storage = cookie.get();
        if (value != null) {
          storage.put(key, value);
        } else {
          storage.remove(key);
        }
        cookie.update(storage);
      }
    } else {
      HttpSession session;
      if (value != null) {
        session = request.getSession(true);
      } else {
        session = request.getSession(false);
      }

      if (session == null) {
        return;
      }

      Map<String, Object> flash;
      synchronized (session) {
        flash = (Map<String, Object>) session.getAttribute(FLASH_KEY);
        if (flash == null) {
          flash = new HashMap<>();
          session.setAttribute(FLASH_KEY, flash);
        }
      }

      if (value != null) {
        flash.put(key, value);
      } else {
        flash.remove(key);
      }
    }

  }

  /**
   * Moves the flash from the session to the request.
   */
  public void transferFlash() {
    Map<String, Object> flash = Collections.emptyMap();

    if (cookie != null) {
      synchronized (cookie) {
        flash = cookie.get();
        cookie.delete();
      }
    } else {
      HttpSession session = request.getSession(false);
      if (session != null) {
        synchronized (session) {
          flash = (Map<String, Object>) session.getAttribute(FLASH_KEY);
          if (flash != null) {
            session.removeAttribute(FLASH_KEY);
          }
        }
      }
    }

    if (flash != null) {
      request.setAttribute(FLASH_KEY, flash);
    }
  }
}

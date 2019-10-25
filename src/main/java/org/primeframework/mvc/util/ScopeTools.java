/*
 * Copyright (c) 2019, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

import org.primeframework.mvc.scope.ActionSessionScope;

/**
 * This is a special purpose utility to retrieve values out of a scope.
 *
 * @author Daniel DeGroff
 */
public class ScopeTools {
  public static <T> T getActionSessionAttribute(HttpServletRequest request, String key, Class action) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> actionSession = (Map<String, Object>) session.getAttribute(ActionSessionScope.ACTION_SESSION_KEY);
      if (actionSession != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> requestedSession = (Map<String, Object>) actionSession.get(action.getCanonicalName());
        if (requestedSession != null) {
          return (T) requestedSession.get(key);
        }
      }
    } catch (Exception ignore) {
    }

    return null;
  }
}

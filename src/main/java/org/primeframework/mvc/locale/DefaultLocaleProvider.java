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
package org.primeframework.mvc.locale;

import java.util.Locale;

import com.google.inject.Inject;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.guice.Nullable;
import org.primeframework.mvc.http.Cookie;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.util.LocaleTools;

/**
 * This is the default LocaleProvider implementation.
 *
 * @author Brian Pontarelli
 */
public class DefaultLocaleProvider implements LocaleProvider {
  private final MVCConfiguration configuration;

  private final HTTPRequest request;

  private final HTTPResponse response;

  /**
   * Optionally inject the request.
   *
   * @param request The request.
   */
  @Inject
  public DefaultLocaleProvider(@Nullable HTTPRequest request, @Nullable HTTPResponse response,
                               MVCConfiguration configuration) {
    this.request = request;
    this.response = response;
    this.configuration = configuration;
  }

  /**
   * Looks up the Locale using this search order:
   * <p/>
   * <ol>
   *   <li>If there is no request, use the system default</li>
   *   <li>If there is a request, try a persistent cookie</li>
   *   <li>If the locale hasn't been found, get it from the request Accept-Language header</li>
   * </ol>
   *
   * @return The Locale and never null.
   */
  @Override
  public Locale get() {
    if (request == null) {
      return Locale.getDefault();
    }

    // Try a persistent cookie first
    String key = configuration.localeCookieName();
    Cookie cookie = request.getCookie(key);
    if (cookie != null && cookie.value != null) {
      try {
        return LocaleTools.toLocale(cookie.value);
      } catch (Exception e) {
        // Ignore and keep going
      }
    }

    return request.getLocale();
  }

  /**
   * Sets the Locale into the response as a cookie.
   *
   * @param locale The Locale to store.
   */
  @Override
  public void set(Locale locale) {
    if (response == null) {
      return;
    }

    String key = configuration.localeCookieName();
    Cookie cookie = new Cookie(key, locale != null ? locale.toString() : null);
    cookie.maxAge = locale != null ? Long.MAX_VALUE : 0L;
    cookie.path = "/";
    response.addCookie(cookie);
  }
}
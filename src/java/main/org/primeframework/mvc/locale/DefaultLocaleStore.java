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
package org.primeframework.mvc.locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Locale;

import com.google.inject.Inject;

/**
 * <p> This is the default LocaleProvider implementation. </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultLocaleStore implements LocaleStore {
  public static final String LOCALE_KEY = "jcatapultLocale";
  private final HttpServletRequest request;

  @Inject
  public DefaultLocaleStore(HttpServletRequest request) {
    this.request = request;
  }

  /**
   * Looks up the Locale using this search order:
   * <p/>
   * <ol> <li>If there is a session, look for an attribute under the {@link #LOCALE_KEY}</li> <li>If there is not a
   * session, look for an request attribute under the {@link #LOCALE_KEY}</li> <li>If the locale hasn't been found, get
   * it from the request</li> </ol>
   *
   * @return The Locale and never null.
   */
  public Locale get() {
    HttpSession session = request.getSession(false);
    Locale locale;
    if (session == null) {
      locale = (Locale) request.getAttribute(LOCALE_KEY);
    } else {
      locale = (Locale) session.getAttribute(LOCALE_KEY);
    }

    if (locale == null) {
      locale = request.getLocale();
    }

    // If it is found, store it in the JSTL context
    if (locale != null) {
      request.setAttribute("javax.servlet.jsp.jstl.fmt.locale", locale);
    }

    return locale;
  }

  /**
   * Sets the Locale into the session using the LOCALE_KEY constant. This doesn't ever create a session. If there isn't
   * a session, this falls back to storing the Locale in the request.
   *
   * @param locale The Locale to store.
   */
  public void set(Locale locale) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      request.setAttribute(LOCALE_KEY, locale);
    } else {
      session.setAttribute(LOCALE_KEY, locale);
    }

    request.setAttribute("javax.servlet.jsp.jstl.fmt.locale", locale);
  }
}
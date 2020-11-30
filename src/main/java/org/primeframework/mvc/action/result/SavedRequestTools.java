/*
 * Copyright (c) 2016-2020, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.DefaultSavedRequestWorkflow;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.SavedRequestException;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Toolkit to help with Saved Request stuff.
 *
 * @author Brian Pontarelli
 */
public class SavedRequestTools {
  private static final Logger logger = LoggerFactory.getLogger(SavedRequestTools.class);

  /**
   * Retrieve the saved request from a cookie for use in the {@link ReexecuteSavedRequestResult#execute(ReexecuteSavedRequest)}
   * phase.
   *
   * @param configuration The MVC configuration used to determine the name of the cookie.
   * @param encryptor     the encryptor used to decrypt the cookie
   * @param request       the HTTP servlet request
   * @param response      the HTTP servlet response
   * @return null if no save request was found.
   */
  public static SaveHttpRequestResult getSaveRequestForReExecution(MVCConfiguration configuration, Encryptor encryptor,
                                                                   HttpServletRequest request,
                                                                   HttpServletResponse response) {
    SaveHttpRequestResult result = getSaveHttpRequestResult(configuration, encryptor, request, response);
    if (result == null) {
      return null;
    }

    // Mark the cookie as ready
    result.cookie.setMaxAge(-1);
    result.cookie.setPath("/");
    result.cookie.setValue("ready_" + result.cookie.getValue());
    response.addCookie(result.cookie);

    return result;
  }

  /**
   * Retrieve the saved request from a cookie for use during the {@link DefaultSavedRequestWorkflow#perform(WorkflowChain)}
   * step.
   *
   * @param configuration The MVC configuration used to determine the name of the cookie.
   * @param encryptor     the encryptor used to decrypt the cookie
   * @param request       the HTTP servlet request
   * @param response      the HTTP servlet response
   * @return null if no save request was found.
   */
  public static SaveHttpRequestResult getSaveRequestForWorkflow(MVCConfiguration configuration, Encryptor encryptor,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) {
    SaveHttpRequestResult result = getSaveHttpRequestResult(configuration, encryptor, request, response);
    if (result == null) {
      return null;
    }

    if (result.ready) {
      result.cookie.setPath("/");
      result.cookie.setMaxAge(0);
      response.addCookie(result.cookie);
      return result;
    }

    return null;
  }

  /**
   * Creates a Cookie Object for the given SavedHttpRequest object.
   *
   * @param savedRequest  The Saved Request.
   * @param configuration THe MVC Configuration that is used to determine the cookie name.
   * @param encryptor     The encryptor used to encrypt the cookie
   * @return The cookie.
   */
  public static Cookie toCookie(SavedHttpRequest savedRequest, MVCConfiguration configuration,
                                Encryptor encryptor) {
    try {
      String encrypted = encryptor.encrypt(savedRequest);
      Cookie cookie = new Cookie(configuration.savedRequestCookieName(), encrypted);
      cookie.setPath("/"); // Turn the cookie on for everything since we have no clue what URI will Re-execute the Saved Request
      cookie.setMaxAge(-1); // Be explicit
      cookie.setVersion(1); // Be explicit
      cookie.setHttpOnly(true);
      // Set to secure when schema is 'https'
      cookie.setSecure(savedRequest.uri.startsWith("/") || savedRequest.uri.startsWith("https"));
      return cookie;
    } catch (Exception e) {
      throw new SavedRequestException(e);
    }
  }

  private static Cookie getCookie(MVCConfiguration configuration, HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (final Cookie cookie : cookies) {
        if (cookie.getName().equals(configuration.savedRequestCookieName())) {
          return cookie;
        }
      }
    }

    return null;
  }

  private static SaveHttpRequestResult getSaveHttpRequestResult(MVCConfiguration configuration, Encryptor encryptor,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) {
    Cookie cookie = getCookie(configuration, request);
    if (cookie == null) {
      return null;
    }

    try {
      String value = cookie.getValue();
      boolean ready = value.startsWith("ready_");
      if (ready) {
        value = value.substring("ready_".length());
      }

      return new SaveHttpRequestResult(cookie, ready, encryptor.decrypt(SavedHttpRequest.class, value));
    } catch (Exception e) {
      logger.warn("Bad SavedRequest cookie [{}]. Error is [{}]", cookie.getValue(), e.getMessage());

      // Delete the corrupted cookie.
      cookie.setMaxAge(0);
      cookie.setPath("/");
      response.addCookie(cookie);
    }

    return null;
  }

  public static class SaveHttpRequestResult {
    public Cookie cookie;

    public boolean ready;

    public SavedHttpRequest savedHttpRequest;

    public SaveHttpRequestResult(Cookie cookie, boolean ready, SavedHttpRequest savedHttpRequest) {
      this.cookie = cookie;
      this.ready = ready;
      this.savedHttpRequest = savedHttpRequest;
    }
  }
}

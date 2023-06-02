/*
 * Copyright (c) 2016-2023, Inversoft Inc., All Rights Reserved
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

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.DefaultSavedRequestWorkflow;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.SavedRequestException;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.util.CookieTools;
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
   * Retrieve the saved request from a cookie for use in the {@link ReexecuteSavedRequestResult#execute(ReexecuteSavedRequest)} phase.
   *
   * @param configuration The MVC configuration used to determine the name of the cookie.
   * @param encryptor     the encryptor used to decrypt the cookie
   * @param objectMapper  The ObjectMapper used to convert the objects to JSON.
   * @param request       the HTTP servlet request
   * @param response      the HTTP servlet response
   * @return null if no save request was found.
   */
  public static SaveHttpRequestResult getSaveRequestForReExecution(MVCConfiguration configuration, Encryptor encryptor,
                                                                   ObjectMapper objectMapper, HTTPRequest request,
                                                                   HTTPResponse response) {
    SaveHttpRequestResult result = getSaveHttpRequestResult(configuration, encryptor, objectMapper, request, response);
    if (result == null || result.savedHttpRequest == null) {
      return null;
    }

    if (result.savedHttpRequest.method == HTTPMethod.GET) {
      // Delete the cookie because we will just be redirecting the browser to the original GET
      result.cookie.maxAge = 0L;
      result.cookie.path = "/";
    } else {
      // Mark the cookie as ready and session based
      result.cookie.maxAge = null;
      result.cookie.path = "/";
      result.cookie.value = "ready_" + result.cookie.value;
    }

    result.cookie.sameSite = configuration.savedRequestCookieSameSite();
    response.addCookie(result.cookie);

    return result;
  }

  /**
   * Retrieve the saved request from a cookie for use during the {@link DefaultSavedRequestWorkflow#perform(WorkflowChain)} step.
   *
   * @param configuration The MVC configuration used to determine the name of the cookie.
   * @param encryptor     the encryptor used to decrypt the cookie
   * @param objectMapper  The ObjectMapper used to convert the objects to JSON.
   * @param request       the HTTP servlet request
   * @param response      the HTTP servlet response
   * @return null if no save request was found.
   */
  public static SaveHttpRequestResult getSaveRequestForWorkflow(MVCConfiguration configuration, Encryptor encryptor,
                                                                ObjectMapper objectMapper, HTTPRequest request,
                                                                HTTPResponse response) {
    SaveHttpRequestResult result = getSaveHttpRequestResult(configuration, encryptor, objectMapper, request, response);
    if (result == null) {
      return null;
    }

    if (!result.ready) {
      return null;
    }

    result.cookie.path = "/";
    result.cookie.maxAge = 0L;
    response.addCookie(result.cookie);
    return result;
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
                                Encryptor encryptor, ObjectMapper objectMapper) {
    try {
      String value = CookieTools.toJSONCookie(savedRequest, true, true, encryptor, objectMapper);
      Cookie cookie = new Cookie(configuration.savedRequestCookieName(), value);
      cookie.path = "/"; // Turn the cookie on for everything since we have no clue what URI will Re-execute the Saved Request
      cookie.httpOnly = true; // No JavaScript hacking
      cookie.sameSite = configuration.savedRequestCookieSameSite();
      cookie.secure = "https".equalsIgnoreCase(URI.create(savedRequest.uri).getScheme());
      return cookie;
    } catch (Exception e) {
      throw new SavedRequestException(e);
    }
  }

  private static Cookie getCookie(MVCConfiguration configuration, HTTPRequest request) {
    return request.getCookie(configuration.savedRequestCookieName());
  }

  private static SaveHttpRequestResult getSaveHttpRequestResult(MVCConfiguration configuration, Encryptor encryptor,
                                                                ObjectMapper objectMapper, HTTPRequest request,
                                                                HTTPResponse response) {
    Cookie cookie = getCookie(configuration, request);
    if (cookie == null) {
      return null;
    }

    try {
      String value = cookie.value;
      boolean ready = value.startsWith("ready_");
      if (ready) {
        value = value.substring("ready_".length());
      }

      SavedHttpRequest savedRequest = CookieTools.fromJSONCookie(value, SavedHttpRequest.class, true, encryptor, objectMapper);
      return new SaveHttpRequestResult(cookie, ready, savedRequest);
    } catch (Exception e) {
      logger.warn("Bad SavedRequest cookie [{}]. Error is [{}]", cookie.value, e.getMessage());

      // Delete the corrupted cookie.
      cookie.maxAge = 0L;
      cookie.path = "/";
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

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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.SavedRequestException;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
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
   * Delete the save request cookie.
   *
   * @param configuration the MVC configuration used to determine the name of the cookie.
   * @param response      the HTTP servlet response
   */
  public static void deleteCookie(MVCConfiguration configuration, HttpServletResponse response) {
    // Delete the cookie
    Cookie cookie = new Cookie(configuration.savedRequestCookieName(), null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }

  /**
   * Retrieve the saved request from a cookie.
   *
   * @param configuration The MVC configuration used to determine the name of the cookie.
   * @param encryptor     the encryptor used to decrypt the cookie
   * @param request       the HTTP servlet request
   * @return null if no save request was found.
   */
  public static SaveHttpRequestResult fromCookie(MVCConfiguration configuration, Encryptor encryptor,
                                                 HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (final Cookie cookie : cookies) {
        if (cookie.getName().equals(configuration.savedRequestCookieName())) {
          try {
            return new SaveHttpRequestResult(cookie, encryptor.decrypt(SavedHttpRequest.class, cookie.getValue()));
          } catch (IOException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | ShortBufferException e) {
            logger.warn("Bad SavedRequest cookie [{}]. Error is [{}]", cookie.getValue(), e.getMessage());
          }
        }
      }
    }

    return null;
  }

  /**
   * Check to see if we can consume the Save Request Cookie.
   *
   * @param configuration the MVC configuration
   * @param request       the HTTP servlet request
   * @param response      the HTTP servlet response
   * @return true if we are ready.
   */
  public static boolean isExecuted(MVCConfiguration configuration, HttpServletRequest request,
                                   HttpServletResponse response) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(configuration.savedRequestCookieName() + "_executed")) {
          cookie.setMaxAge(0);
          cookie.setPath("/");
          response.addCookie(cookie);
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Mark this cookie as ready to be used during the re-execute workflow.
   *
   * @param configuration the MVC configuration
   * @param response      the HTTP servlet response
   */
  public static void markExecuted(MVCConfiguration configuration, HttpServletResponse response) {
    Cookie cookie = new Cookie(configuration.savedRequestCookieName() + "_executed", "pending");
    cookie.setMaxAge(-1);
    cookie.setPath("/");
    response.addCookie(cookie);
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
    } catch (JsonProcessingException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | ShortBufferException e) {
      throw new SavedRequestException(e);
    }
  }

  public static class SaveHttpRequestResult {
    public Cookie cookie;

    public SavedHttpRequest savedHttpRequest;

    public SaveHttpRequestResult(Cookie cookie, SavedHttpRequest savedHttpRequest) {
      this.cookie = cookie;
      this.savedHttpRequest = savedHttpRequest;
    }
  }
}

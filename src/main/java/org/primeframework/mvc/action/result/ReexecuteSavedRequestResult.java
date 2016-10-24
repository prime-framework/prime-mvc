/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.CipherProvider;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * This result performs a HTTP redirect to a Saved Request URL. This also transfers all messages from the request to the
 * flash. If we don't transfer the messages they will be lost after the redirect.
 *
 * @author Brian Pontarelli
 */
public class ReexecuteSavedRequestResult extends AbstractRedirectResult<ReexecuteSavedRequest> {
  private static final Logger logger = LoggerFactory.getLogger(ReexecuteSavedRequestResult.class);

  private final CipherProvider cipherProvider;

  private final MVCConfiguration configuration;

  private final ObjectMapper objectMapper;

  @Inject
  public ReexecuteSavedRequestResult(MessageStore messageStore, ExpressionEvaluator expressionEvaluator, HttpServletResponse response,
                                     HttpServletRequest request, ActionInvocationStore actionInvocationStore,
                                     CipherProvider cipherProvider, MVCConfiguration configuration, ObjectMapper objectMapper) {
    super(expressionEvaluator, actionInvocationStore, messageStore, request, response);
    this.configuration = configuration;
    this.cipherProvider = cipherProvider;
    this.objectMapper = objectMapper;
  }

  /**
   * {@inheritDoc}
   */
  public boolean execute(final ReexecuteSavedRequest reexecuteSavedRequest) throws IOException, ServletException {
    moveMessagesToFlash();

    String savedRequestCookieName = configuration.savedRequestCookieName();
    Cookie[] cookies = request.getCookies();
    String uri = null;

    if (cookies != null) {
      for (final Cookie cookie : cookies) {
        if (cookie.getName().equals(savedRequestCookieName)) {
          // Kill the cookie
          cookie.setMaxAge(0);
          response.addCookie(cookie);

          // Move the saved request to the session and redirect the saved request URI
          SavedHttpRequest savedRequest = parseSavedRequest(cookie.getValue());
          if (savedRequest == null) {
            break;
          }

          HttpSession session = request.getSession(true);
          session.setAttribute(SavedHttpRequest.LOGGED_IN_SESSION_KEY, savedRequest);
          uri = savedRequest.uri;
        }
      }
    }

    sendRedirect(uri, reexecuteSavedRequest.uri(), reexecuteSavedRequest.encodeVariables(), reexecuteSavedRequest.perm());
    return true;
  }

  private SavedHttpRequest parseSavedRequest(String value) {
    try {
      byte[] bytes = Base64.getDecoder().decode(value);
      Cipher cipher = cipherProvider.getDecryptor();
      byte[] result = new byte[cipher.getOutputSize(bytes.length)];
      int resultLength = cipher.update(bytes, 0, bytes.length, result, 0);
      resultLength += cipher.doFinal(result, resultLength);
      String json = new String(result, 0, resultLength, Charset.forName("UTF-8"));
      return objectMapper.readValue(json, SavedHttpRequest.class);
    } catch (IOException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | ShortBufferException e) {
      logger.warn("Bad SavedRequest cookie [{}]. Error is [{}]", value, e.getMessage());
      return null;
    }
  }

  public static class ReexecuteSavedRequestImpl implements ReexecuteSavedRequest {
    private final String code;

    private final boolean encode;

    private final boolean perm;

    private final String uri;

    public ReexecuteSavedRequestImpl(String uri, String code, boolean perm, boolean encode) {
      this.uri = uri;
      this.code = code;
      this.perm = perm;
      this.encode = encode;
    }

    public Class<? extends Annotation> annotationType() {
      return ReexecuteSavedRequest.class;
    }

    public String code() {
      return code;
    }

    public boolean encodeVariables() {
      return encode;
    }

    public boolean perm() {
      return perm;
    }

    public String uri() {
      return uri;
    }
  }
}
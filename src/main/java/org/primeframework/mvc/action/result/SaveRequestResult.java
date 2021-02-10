/*
 * Copyright (c) 2015-2020, Inversoft Inc., All Rights Reserved
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;
import org.primeframework.mvc.action.result.annotation.SaveRequest;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.servlet.HTTPMethod;

/**
 * This result stores the current request in a cookie and then performs a HTTP redirect to the login page.
 *
 * @author Brian Pontarelli
 */
public class SaveRequestResult extends AbstractRedirectResult<SaveRequest> {
  private final MVCConfiguration configuration;

  private final Encryptor encryptor;

  @Inject
  public SaveRequestResult(MessageStore messageStore, ExpressionEvaluator expressionEvaluator,
                           HttpServletResponse response, HttpServletRequest request,
                           ActionInvocationStore actionInvocationStore,
                           MVCConfiguration configuration, Encryptor encryptor) {
    super(expressionEvaluator, actionInvocationStore, messageStore, request, response);
    this.configuration = configuration;
    this.encryptor = encryptor;
  }

  public boolean execute(SaveRequest saveRequest) throws IOException {
    moveMessagesToFlash();

    Map<String, String[]> requestParameters = null;
    String redirectURI;
    HTTPMethod method = HTTPMethod.valueOf(request.getMethod().toUpperCase());
    if (method == HTTPMethod.GET) {
      Map<String, String[]> params = request.getParameterMap();
      redirectURI = request.getRequestURI() + makeQueryString(params);
    } else {
      requestParameters = request.getParameterMap();
      redirectURI = request.getRequestURI();
    }

    // Build a saved request cookie
    Cookie saveRequestCookie = SavedRequestTools.toCookie(new SavedHttpRequest(method, redirectURI, requestParameters), configuration, encryptor);

    // If the resulting cookie exceeds the maximum configured size in bytes, it would be bad.
    //
    // Peter: I'm fuzzy on the whole good/bad thing. What do you mean, "bad"?
    // Egon: Try to imagine all life as you know it stopping instantaneously, and every molecule in your body exploding at the speed of light.
    // Ray: [shocked gasp] Total protonic reversal.
    // Peter:  Right. That's bad. Okay. All right. Important safety tip. Thanks, Egon.
    //
    // Ok, not that bad, but Tomcat will exception and the user will receive a 500. See MVCConfiguration.savedRequestCookieMaximumSize for more information.
    if (saveRequestCookie.getValue().getBytes(StandardCharsets.UTF_8).length <= configuration.savedRequestCookieMaximumSize()) {
      response.addCookie(saveRequestCookie);
    }

    // Handle setting cache controls
    addCacheControlHeader(saveRequest, response);

    sendRedirect(null, saveRequest.uri(), saveRequest.encodeVariables(), saveRequest.perm());
    return true;
  }

  @Override
  protected String getCacheControl(SaveRequest result) {
    return result.cacheControl();
  }

  @Override
  protected boolean getDisableCacheControl(SaveRequest result) {
    return result.disableCacheControl();
  }

  private String makeQueryString(Map<String, String[]> parameters) {
    if (parameters.size() == 0) {
      return "";
    }

    StringBuilder build = new StringBuilder();
    for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
      for (String value : entry.getValue()) {
        if (build.length() > 0) {
          build.append("&");
        }

        try {
          build.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return "?" + build.toString();
  }

  public static class SaveRequestImpl implements SaveRequest {
    private final String cacheControl;

    private final String code;

    private final boolean disableCacheControl;

    private final boolean encode;

    private final boolean perm;

    private final String uri;

    public SaveRequestImpl(String uri, String code, boolean perm, boolean encode) {
      this.cacheControl = "no-store";
      this.code = code;
      this.disableCacheControl = false;
      this.encode = encode;
      this.uri = uri;
      this.perm = perm;
    }

    public Class<? extends Annotation> annotationType() {
      return ReexecuteSavedRequest.class;
    }

    @Override
    public String cacheControl() {
      return cacheControl;
    }

    public String code() {
      return code;
    }

    @Override
    public boolean disableCacheControl() {
      return disableCacheControl;
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
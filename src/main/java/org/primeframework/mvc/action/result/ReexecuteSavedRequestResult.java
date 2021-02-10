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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;

import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.SavedRequestTools.SaveHttpRequestResult;
import org.primeframework.mvc.action.result.annotation.ReexecuteSavedRequest;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.Encryptor;

/**
 * This result performs a HTTP redirect to a Saved Request URL. This also transfers all messages from the request to the
 * flash. If we don't transfer the messages they will be lost after the redirect.
 *
 * @author Brian Pontarelli
 */
public class ReexecuteSavedRequestResult extends AbstractRedirectResult<ReexecuteSavedRequest> {
  private final MVCConfiguration configuration;

  private final Encryptor encryptor;

  @Inject
  public ReexecuteSavedRequestResult(MessageStore messageStore, ExpressionEvaluator expressionEvaluator,
                                     HttpServletResponse response, HttpServletRequest request,
                                     ActionInvocationStore actionInvocationStore,
                                     MVCConfiguration configuration, Encryptor encryptor) {
    super(expressionEvaluator, actionInvocationStore, messageStore, request, response);
    this.configuration = configuration;
    this.encryptor = encryptor;
  }

  /**
   * {@inheritDoc}
   */
  public boolean execute(final ReexecuteSavedRequest reexecuteSavedRequest) throws IOException {
    moveMessagesToFlash();

    SaveHttpRequestResult result = SavedRequestTools.getSaveRequestForReExecution(configuration, encryptor, request, response);
    String uri = result == null || result.savedHttpRequest.uri == null ? null : result.savedHttpRequest.uri;

    // Handle setting cache controls
    addCacheControlHeader(reexecuteSavedRequest, response);

    sendRedirect(uri, reexecuteSavedRequest.uri(), reexecuteSavedRequest.encodeVariables(), reexecuteSavedRequest.perm());
    return true;
  }

  @Override
  protected String getCacheControl(ReexecuteSavedRequest result) {
    return result.cacheControl();
  }

  @Override
  protected boolean getDisableCacheControl(ReexecuteSavedRequest result) {
    return result.disableCacheControl();
  }

  public static class ReexecuteSavedRequestImpl implements ReexecuteSavedRequest {
    private final String cacheControl;

    private final String code;

    private final boolean disableCacheControl;

    private final boolean encode;

    private final boolean perm;

    private final String uri;

    public ReexecuteSavedRequestImpl(String uri, String code, boolean perm, boolean encode) {
      this.cacheControl = "no-cache";
      this.code = code;
      this.disableCacheControl = false;
      this.encode = encode;
      this.perm = perm;
      this.uri = uri;
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
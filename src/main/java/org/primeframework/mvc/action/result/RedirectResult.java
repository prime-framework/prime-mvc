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
package org.primeframework.mvc.action.result;

import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;

import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * This result performs a HTTP redirect to a URL. This also transfers all messages from the request to the flash. If we
 * don't transfer the messages they will be lost after the redirect.
 *
 * @author Brian Pontarelli
 */
public class RedirectResult extends AbstractRedirectResult<Redirect> {
  @Inject
  public RedirectResult(MessageStore messageStore, ExpressionEvaluator expressionEvaluator,
                        HTTPResponse response,
                        HTTPRequest request, ActionInvocationStore actionInvocationStore) {
    super(expressionEvaluator, actionInvocationStore, messageStore, request, response);
  }

  /**
   * {@inheritDoc}
   */
  public boolean execute(Redirect redirect) throws IOException {
    moveMessagesToFlash();

    // Handle setting cache controls
    addCacheControlHeader(redirect, response);

    sendRedirect(null, redirect.uri(), redirect.encodeVariables(), redirect.perm());
    return true;
  }

  @Override
  protected String getCacheControl(Redirect result) {
    return result.cacheControl();
  }

  @Override
  protected boolean getDisableCacheControl(Redirect result) {
    return result.disableCacheControl();
  }

  public static class RedirectImpl implements Redirect {
    private final String cacheControl;

    private final String code;

    private final boolean disableCacheControl;

    private final boolean encode;

    private final boolean perm;

    private final String uri;

    public RedirectImpl(String uri, String code, boolean perm, boolean encode) {
      this.cacheControl = "no-cache";
      this.disableCacheControl = false;
      this.encode = encode;
      this.code = code;
      this.perm = perm;
      this.uri = uri;
    }

    public Class<? extends Annotation> annotationType() {
      return Redirect.class;
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
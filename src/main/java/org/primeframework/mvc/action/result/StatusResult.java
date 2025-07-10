/*
 * Copyright (c) 2001-2025, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.lang.annotation.Annotation;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.Header;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * This result returns a status response.
 *
 * @author Brian Pontarelli
 */
public class StatusResult extends AbstractResult<Status> {
  private final ActionInvocationStore actionInvocationStore;

  private final HTTPResponse response;

  @Inject
  public StatusResult(ExpressionEvaluator expressionEvaluator, HTTPResponse response,
                      ActionInvocationStore actionInvocationStore) {
    super(expressionEvaluator);
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * {@inheritDoc}
   */
  public boolean execute(Status status) throws IOException {
    Object action = actionInvocationStore.getCurrent().action;
    setStatus(status.status(), status.statusStr(), action, response);

    for (Header header : status.headers()) {
      response.setHeader(header.name(), header.value());
    }

    // Handle setting cache controls
    addCacheControlHeader(status, response);

    return true;
  }

  @Override
  protected String getCacheControl(Status result) {
    return result.cacheControl();
  }

  @Override
  protected boolean getDisableCacheControl(Status result) {
    return result.disableCacheControl();
  }

  public static class StatusImpl implements Status {
    private final String cacheControl;

    private final String code;

    private final boolean disableCacheControl;

    private final int status;

    private final String statusStr;

    public StatusImpl(String code, int status) {
      this.cacheControl = "no-cache";
      this.code = code;
      this.disableCacheControl = false;
      this.status = status;
      this.statusStr = "";
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return Status.class;
    }

    @Override
    public String cacheControl() {
      return cacheControl;
    }

    @Override
    public String code() {
      return code;
    }

    @Override
    public boolean disableCacheControl() {
      return disableCacheControl;
    }

    @Override
    public Header[] headers() {
      return new Header[0];
    }

    @Override
    public int status() {
      return status;
    }

    @Override
    public String statusStr() {
      return statusStr;
    }
  }
}

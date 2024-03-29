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

import java.lang.annotation.Annotation;

import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.HTTPValues.Headers;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * This result performs a servlet forward to a JSP or renders a FreeMarker template depending on the extension of the
 * page.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractResult<U extends Annotation> implements Result<U> {
  protected final ExpressionEvaluator expressionEvaluator;

  protected AbstractResult(ExpressionEvaluator expressionEvaluator) {
    this.expressionEvaluator = expressionEvaluator;
  }

  protected void addCacheControlHeader(U result, HTTPResponse response) {
    if (getDisableCacheControl(result)) {
      return;
    }

    response.setHeader(Headers.CacheControl, getCacheControl(result));
  }

  /**
   * Expands any variables in the String.
   *
   * @param str    The String to expand.
   * @param action The action used to expand.
   * @param encode Whether the variable replacements are URL encoded.
   * @return The result.
   */
  protected String expand(String str, Object action, boolean encode) {
    if (action != null) {
      return expressionEvaluator.expand(str, action, encode);
    }

    return str;
  }

  /**
   * return the cache control header if one is defined. If you wish to disable control and manage Cache-Control header
   * on your own, use the {@link #getDisableCacheControl(Annotation)} method.
   *
   * @param result the result
   * @return the value of the <code>Cache-Control</code> header.
   */
  protected abstract String getCacheControl(U result);

  /**
   * Return a boolean value indicating if the <code>Cache-Control</code> HTTP response header will be set by this
   * result. If you wish to manage these headers on your own, disable control.
   *
   * @param result the result
   * @return true if you wish to disable Cache Control for this result.
   */
  protected abstract boolean getDisableCacheControl(U result);

  /**
   * Return true if the current invocation is for an HTTP HEAD request.
   *
   * @param actionInvocation the ActionInvocation
   * @return true if the current action invocation is a HTTP HEAD request
   */
  protected boolean isHeadRequest(ActionInvocation actionInvocation) {
    return actionInvocation.method != null && HTTPMethod.HEAD.is(actionInvocation.method.httpMethod);
  }

  /**
   * Sets the status into the response. If the String <code>statusStr</code> is set, it overrides the int code.
   *
   * @param status    The default code to use.
   * @param statusStr The String to expand and convert to an int (if specified).
   * @param action    The action to use for expansion.
   * @param response  The response to set the status into.
   */
  protected void setStatus(int status, String statusStr, Object action, HTTPResponse response) {
    int code = status;
    if (!statusStr.isEmpty()) {
      code = Integer.parseInt(expand(statusStr, action, false));
    }

    response.setStatus(code);
  }
}
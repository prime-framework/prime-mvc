/*
 * Copyright (c) 2001-2016, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.servlet.HTTPMethod;

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

  /**
   * Expands any variables in the String.
   *
   * @param str    The String to expand.
   * @param action The action used to expand.
   * @param encode Whether or not variable replacements are URL encoded.
   * @return The result.
   */
  protected String expand(String str, Object action, boolean encode) {
    if (action != null) {
      return expressionEvaluator.expand(str, action, encode);
    }

    return str;
  }

  /**
   * Return true if the current invocation is for an HTTP HEAD request.
   *
   * @param actionInvocation the ActionInvocation
   * @return true if the current action invocation is a HTTP HEAD request
   */
  protected boolean isHeadRequest(ActionInvocation actionInvocation) {
    return actionInvocation.method != null && actionInvocation.method.httpMethod == HTTPMethod.HEAD;
  }

  /**
   * Sets the status into the response. If the String <code>statusStr</code> is set, it overrides the int code.
   *
   * @param status    The default code to use.
   * @param statusStr The String to expand and convert to an int (if specified).
   * @param action    The action to use for expansion.
   * @param response  The response to set the status into.
   */
  protected void setStatus(int status, String statusStr, Object action, HttpServletResponse response) {
    int code = status;
    if (!statusStr.isEmpty()) {
      code = Integer.valueOf(expand(statusStr, action, false));
    }

    response.setStatus(code);
  }
}
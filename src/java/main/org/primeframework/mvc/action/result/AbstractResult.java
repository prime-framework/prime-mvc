/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
import java.lang.annotation.Annotation;

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

  /**
   * If the action invocation isn't null, this returns an instance of the {@link ResultHttpServletRequest} class.
   *
   * @param invocation The action invocation.
   * @param request    The request.
   * @return The wrapped request or the request passed in, depending.
   */
  protected HttpServletRequest wrapRequest(ActionInvocation invocation, HttpServletRequest request) {
    if (invocation.action() != null) {
      return new ResultHttpServletRequest(request, invocation.action(), expressionEvaluator);
    }

    return request;
  }

  /**
   * Expands any variables in the String.
   *
   * @param str    The String to expand.
   * @param action The action used to expand.
   * @return The result.
   */
  protected String expand(String str, Object action, boolean encode) {
    if (action != null) {
      return expressionEvaluator.expand(str, action);
    }

    return str;
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
      code = Integer.valueOf(expand(statusStr, action));
    }

    response.setStatus(code);
  }
}

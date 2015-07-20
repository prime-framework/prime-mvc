/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Stream;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import com.google.inject.Inject;

/**
 * This result writes bytes to the response output steam.
 *
 * @author Brian Pontarelli
 */
public class StreamResult extends AbstractResult<Stream> {
  private final HttpServletResponse response;
  private final ActionInvocationStore actionInvocationStore;

  @Inject
  public StreamResult(ExpressionEvaluator expressionEvaluator, HttpServletResponse response,
                      ActionInvocationStore actionInvocationStore) {
    super(expressionEvaluator);
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * {@inheritDoc}
   */
  public void execute(Stream stream) throws IOException, ServletException {
    ActionInvocation current = actionInvocationStore.getCurrent();
    Object action = current.action;
    String property = stream.property();
    String length = expand(stream.length(), action, false);
    String name = expand(stream.name(), action, true);
    String type = expand(stream.type(), action, false);

    Object object = expressionEvaluator.getValue(property, action);
    if (object == null || !(object instanceof InputStream)) {
      throw new PrimeException("Invalid property [" + property + "] for Stream result. This " +
        "property returned null or an Object that is not an InputStream.");
    }

    response.setContentType(type);

    if (StringUtils.isNotBlank(length)) {
      response.setContentLength(Integer.parseInt(length));
    }

    if (StringUtils.isNotBlank(name)) {
      response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
    }

    if (isHeadRequest(current)) {
      return;
    }

    InputStream is = (InputStream) object;
    ServletOutputStream sos = response.getOutputStream();
    try {
      // Then output the file
      byte[] b = new byte[8192];
      int len;
      while ((len = is.read(b)) != -1) {
        sos.write(b, 0, len);
      }
    } finally {
      sos.flush();
      sos.close();
    }
  }
}
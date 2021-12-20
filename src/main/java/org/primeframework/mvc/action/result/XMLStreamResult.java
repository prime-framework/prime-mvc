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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.XMLStream;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * This result writes bytes which represent xml to the response output stream and sets the content type to
 * "application/xhtml+xml"
 *
 * @author jhumphrey
 */
public class XMLStreamResult extends AbstractResult<XMLStream> {
  private final ActionInvocationStore actionInvocationStore;

  private final HTTPResponse response;

  @Inject
  public XMLStreamResult(ExpressionEvaluator expressionEvaluator, HTTPResponse response,
                         ActionInvocationStore actionInvocationStore) {
    super(expressionEvaluator);
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
  }

  public boolean execute(XMLStream xmlStream) throws IOException {
    String xml = xmlStream.property();

    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    Object object = expressionEvaluator.getValue(xml, actionInvocation.action);
    if (!(object instanceof String)) {
      throw new PrimeException("Invalid property [" + xml + "] for XMLStream result. This " +
          "property returned null or an Object that is not a String.");
    }

    byte[] xmlBytes = ((String) object).getBytes(StandardCharsets.UTF_8);

    response.setStatus(xmlStream.status());
    response.setContentType("application/xhtml+xml; charset=UTF-8");
    response.setContentLength((long) xmlBytes.length);

    // Handle setting cache controls
    addCacheControlHeader(xmlStream, response);

    if (isHeadRequest(actionInvocation)) {
      return true;
    }

    InputStream is = new ByteArrayInputStream(xmlBytes);
    writeToOutputStream(is, response);

    return true;
  }

  @Override
  protected String getCacheControl(XMLStream result) {
    return result.cacheControl();
  }

  @Override
  protected boolean getDisableCacheControl(XMLStream result) {
    return result.disableCacheControl();
  }
}

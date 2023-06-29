/*
 * Copyright (c) 2001-2023, Inversoft Inc., All Rights Reserved
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
import java.io.InputStream;
import java.time.ZonedDateTime;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPResponse;
import org.apache.commons.lang3.StringUtils;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Stream;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.util.DateTools;
import org.primeframework.mvc.util.EncodingUtils;

/**
 * This result writes bytes to the response output steam.
 *
 * @author Brian Pontarelli
 */
public class StreamResult extends AbstractResult<Stream> {
  private final ActionInvocationStore actionInvocationStore;

  private final HTTPResponse response;

  @Inject
  public StreamResult(ExpressionEvaluator expressionEvaluator, HTTPResponse response,
                      ActionInvocationStore actionInvocationStore) {
    super(expressionEvaluator);
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * {@inheritDoc}
   */
  public boolean execute(Stream stream) throws IOException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    Object action = actionInvocation.action;
    String property = stream.property();
    String lastModifiedProperty = stream.lastModifiedProperty();
    String length = expand(stream.length(), action, false);
    String name = expand(stream.name(), action, false);
    String type = expand(stream.type(), action, false);

    ZonedDateTime lastModified = null;
    try {
      Object object = expressionEvaluator.getValue(lastModifiedProperty, action);
      if (!(object instanceof ZonedDateTime)) {
        throw new PrimeException("Invalid property [" + property + "] for Stream result. This " +
            "property returned null or an Object that is not an InputStream.");
      }
      lastModified = (ZonedDateTime) object;
    } catch (Exception e) {
      // Ignore since this property isn't required
    }

    response.setStatus(stream.status());
    response.setContentType(type);

    if (StringUtils.isNotBlank(length)) {
      response.setContentLength(Long.parseLong(length));
    }

    if (StringUtils.isNotBlank(name)) {
      response.setHeader("Content-Disposition", "attachment; filename=\"" + EncodingUtils.escapedQuotedString(name) + "\"; filename*=UTF-8''" + EncodingUtils.rfc5987_encode(name));
    }

    if (lastModified != null) {
      response.setHeader("Last-Modified", lastModified.format(DateTools.RFC_5322_DATE_TIME));
    }

    // Handle setting cache controls
    addCacheControlHeader(stream, response);

    // If not null, this must be an InputStream
    Object object = expressionEvaluator.getValue(property, action);
    if (object != null && !(object instanceof InputStream)) {
      throw new PrimeException("Invalid property [" + property + "] for Stream result. This property is of type [" + object.getClass().getSimpleName() + "] but it must be an InputStream.");
    }

    if (!isHeadRequest(actionInvocation) && object == null) {
      throw new PrimeException("Invalid property [" + property + "] for Stream result. This property is must not be null unless the HTTP method is [HEAD].");
    }

    // We don't know what type of InputStream was provided. This will ensure it is closed if non-null.
    try (InputStream is = (InputStream) object) {
      if (isHeadRequest(actionInvocation)) {
        return true;
      }

      // We have already validated that this is not null.
      //noinspection DataFlowIssue
      is.transferTo(response.getOutputStream());
    }

    return true;
  }

  @Override
  protected String getCacheControl(Stream result) {
    return result.cacheControl();
  }

  @Override
  protected boolean getDisableCacheControl(Stream result) {
    return result.disableCacheControl();
  }
}

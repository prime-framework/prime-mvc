/*
 * Copyright (c) 2016-2019, Inversoft Inc., All Rights Reserved
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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.result.annotation.Binary;
import org.primeframework.mvc.content.binary.BinaryActionConfiguration;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * This result writes bytes to the response output stream with <code>Content-Type</code> set to
 * <code>application/octet-stream</code>. The default <code>Content-Type</code> can be modified in the {@link Binary}
 * annotation.
 *
 * @author Daniel DeGroff
 */
public class BinaryResult extends AbstractResult<Binary> {
  private final ActionInvocationStore actionInvocationStore;

  private final HTTPResponse response;

  @Inject
  public BinaryResult(ExpressionEvaluator expressionEvaluator, ActionInvocationStore actionInvocationStore,
                      HTTPResponse response) {
    super(expressionEvaluator);
    this.actionInvocationStore = actionInvocationStore;
    this.response = response;
  }

  /**
   * {@inheritDoc}
   */
  public boolean execute(Binary binary) throws IOException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    Object action = actionInvocation.action;
    if (action == null) {
      throw new PrimeException("There is no action class and somehow we got into the BinaryResult code. No es muy bueno.");
    }

    ActionConfiguration configuration = actionInvocation.configuration;
    if (configuration == null) {
      throw new PrimeException("The action [" + action.getClass() + "] has no configuration. This should be impossible!");
    }

    Path file;
    BinaryActionConfiguration binaryFileActionConfiguration = (BinaryActionConfiguration) configuration.additionalConfiguration.get(BinaryActionConfiguration.class);
    if (binaryFileActionConfiguration == null || binaryFileActionConfiguration.responseMember == null) {
      throw new PrimeException("The action [" + action.getClass() + "] is missing a field annotated with @BinaryResponse. This is used to figure out what to send back in the response.");
    }

    file = expressionEvaluator.getValue(binaryFileActionConfiguration.responseMember, action);
    if (file == null) {
      throw new PrimeException("The @BinaryResponse field [" + binaryFileActionConfiguration.responseMember + "] in the action [" + action.getClass() + "] is null. It cannot be null!");
    }

    response.setStatus(binary.status());
    response.setContentType(binary.contentType());

    // Handle setting cache controls
    addCacheControlHeader(binary, response);

    if (isHeadRequest(actionInvocation)) {
      return true;
    }

    // Stream the file out
    OutputStream outputStream = response.getOutputStream();
    Files.copy(file, outputStream);

    // Delete the file if instructed by the @BinaryResponse
    if (binaryFileActionConfiguration.deleteResponseMemberUponCompletion) {
      try {
        Files.deleteIfExists(file);
      } catch (IOException ignore) {
      }
    }

    return true;
  }

  @Override
  protected String getCacheControl(Binary result) {
    return result.cacheControl();
  }

  @Override
  protected boolean getDisableCacheControl(Binary result) {
    return result.disableCacheControl();
  }
}
/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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
import java.nio.file.Files;
import java.nio.file.Path;

import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.result.annotation.Binary;
import org.primeframework.mvc.content.binary.BinaryActionConfiguration;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import com.google.inject.Inject;

/**
 * This result writes bytes to the response output steam with <code>Content-Type</code> set to
 * <code>application/octet-stream</code>.
 *
 * @author Daniel DeGroff
 */
public class BinaryResult extends AbstractResult<Binary> {
  private final ActionInvocationStore actionInvocationStore;

  private final HttpServletResponse response;

  @Inject
  public BinaryResult(ExpressionEvaluator expressionEvaluator, ActionInvocationStore actionInvocationStore, HttpServletResponse response) {
    super(expressionEvaluator);
    this.actionInvocationStore = actionInvocationStore;
    this.response = response;
  }

  /**
   * {@inheritDoc}
   */
  public boolean execute(Binary binary) throws IOException, ServletException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    Object action = actionInvocation.action;
    if (action == null) {
      throw new PrimeException("There is no action class and somehow we got into the BinaryResult code. No es muy bueno.");
    }

    ActionConfiguration configuration = actionInvocation.configuration;
    if (configuration == null) {
      throw new PrimeException("The action [" + action.getClass() + "] has no configuration. This should be impossible!");
    }

    Path object;
    BinaryActionConfiguration binaryFileActionConfiguration = (BinaryActionConfiguration) configuration.additionalConfiguration.get(BinaryActionConfiguration.class);
    if (binaryFileActionConfiguration == null || binaryFileActionConfiguration.responseMember == null) {
      throw new PrimeException("The action [" + action.getClass() + "] is missing a field annotated with @BinaryResponse. This is used to figure out what to send back in the response.");
    }

    object = expressionEvaluator.getValue(binaryFileActionConfiguration.responseMember, action);
    if (object == null) {
      throw new PrimeException("The @BinaryResponse field [" + binaryFileActionConfiguration.responseMember + "] in the action [" + action.getClass() + "] is null. It cannot be null!");
    }

    byte[] bytes = Files.readAllBytes(object);
    response.setStatus(binary.status());
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/octet-stream");
    response.setContentLength(bytes.length);

    if (isHeadRequest(actionInvocation)) {
      return true;
    }

    try (ServletOutputStream outputStream = response.getOutputStream()) {
      outputStream.write(bytes);
    }

    return true;
  }
}
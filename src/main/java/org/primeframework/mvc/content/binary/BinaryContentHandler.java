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
package org.primeframework.mvc.content.binary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.content.ContentHandler;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * Set a binary file into the action as a {@link Path} object.
 *
 * @author Daniel DeGroff
 */
public class BinaryContentHandler implements ContentHandler {
  public static final String RequestAttribute = "prime-mvc-body-file";

  private final ExpressionEvaluator expressionEvaluator;

  private final HTTPRequest request;

  private final ActionInvocationStore store;

  @Inject
  public BinaryContentHandler(ExpressionEvaluator expressionEvaluator, HTTPRequest request,
                              ActionInvocationStore store) {
    this.expressionEvaluator = expressionEvaluator;
    this.request = request;
    this.store = store;
  }

  @Override
  public void cleanup() {
    Path tmpFile = (Path) request.getAttribute(RequestAttribute);
    if (tmpFile != null) {
      try {
        Files.deleteIfExists(tmpFile);
      } catch (IOException e) {
        // Ignore
      }
    }
  }

  @Override
  public void handle() throws IOException {
    // Only run this handler if the request is NOT multipart
    if (request.isMultipart()) {
      return;
    }

    ActionInvocation actionInvocation = store.getCurrent();
    Object action = actionInvocation.action;
    if (action == null) {
      return;
    }

    ActionConfiguration config = actionInvocation.configuration;
    if (!config.additionalConfiguration.containsKey(BinaryActionConfiguration.class)) {
      return;
    }

    Long contentLength = request.getContentLength();
    if (contentLength == null || contentLength == 0) {
      return;
    }

    BinaryActionConfiguration binaryConfig = (BinaryActionConfiguration) config.additionalConfiguration.get(BinaryActionConfiguration.class);
    if (binaryConfig.requestMember == null) {
      return;
    }

    try {
      Path tmpFile = Files.createTempFile("prime-mvc", "binary");
      try (var output = Files.newOutputStream(tmpFile)) {
        request.getInputStream().transferTo(output);
      }

      request.setAttribute(RequestAttribute, tmpFile);
      expressionEvaluator.setValue(binaryConfig.requestMember, action, tmpFile);
    } catch (IOException e) {
      // Ignore
    }
  }
}

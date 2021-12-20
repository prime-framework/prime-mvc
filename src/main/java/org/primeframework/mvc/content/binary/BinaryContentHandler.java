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
import java.util.List;

import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.content.ContentHandler;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.fileupload.FileInfo;

/**
 * Set a binary file into the action as a {@link Path} object.
 *
 * @author Daniel DeGroff
 */
public class BinaryContentHandler implements ContentHandler {
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
    handle(false);
  }

  @Override
  public void handle() throws IOException {
    // Only run this handler if the request is NOT multipart
    if (request.isMultipart()) {
      return;
    }

    handle(true);
  }

  private void handle(boolean set) {
    ActionInvocation actionInvocation = store.getCurrent();
    Object action = actionInvocation.action;
    if (action == null) {
      return;
    }

    ActionConfiguration config = actionInvocation.configuration;
    if (!config.additionalConfiguration.containsKey(BinaryActionConfiguration.class)) {
      return;
    }

    long contentLength = request.getContentLength();
    if (contentLength == 0) {
      return;
    }

    List<FileInfo> files = request.getFiles();
    if (files.isEmpty()) {
      return;
    }

    BinaryActionConfiguration binaryConfig = (BinaryActionConfiguration) config.additionalConfiguration.get(BinaryActionConfiguration.class);
    if (binaryConfig.requestMember == null) {
      return;
    }

    FileInfo fileInfo = files.get(0);

    if (fileInfo == null) {
      return;
    }

    if (set) {
      expressionEvaluator.setValue(binaryConfig.requestMember, action, fileInfo.file);
    } else {
      try {
        Files.deleteIfExists(fileInfo.file);
      } catch (IOException e) {
        // Ignore
      }
    }
  }
}

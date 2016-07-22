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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.content.ContentHandler;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Set a binary file into the action as a {@link Path} object.
 *
 * @author Daniel DeGroff
 */
public class BinaryContentHandler implements ContentHandler {
  private static final Logger logger = LoggerFactory.getLogger(BinaryContentHandler.class);

  private final ExpressionEvaluator expressionEvaluator;

  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  private final HttpServletRequest request;

  private final ActionInvocationStore store;

  private Path tempFile;

  @Inject
  public BinaryContentHandler(ExpressionEvaluator expressionEvaluator, MessageProvider messageProvider, MessageStore messageStore,
                              HttpServletRequest request, ActionInvocationStore store) {
    this.expressionEvaluator = expressionEvaluator;
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
    this.request = request;
    this.store = store;
  }

  @Override
  public void cleanup() {
    if (tempFile != null) {
      try {
        Files.deleteIfExists(tempFile);
      } catch (IOException ignore) {
      }
    }
  }

  @Override
  public void handle() throws IOException, ServletException {
    ActionInvocation actionInvocation = store.getCurrent();
    Object action = actionInvocation.action;
    if (action == null) {
      return;
    }

    ActionConfiguration config = actionInvocation.configuration;
    if (!config.additionalConfiguration.containsKey(BinaryActionConfiguration.class)) {
      return;
    }

    int contentLength = request.getContentLength();
    if (contentLength == 0) {
      return;
    }

    BinaryActionConfiguration binaryFileActionConfiguration = (BinaryActionConfiguration) config.additionalConfiguration.get(BinaryActionConfiguration.class);
    if (binaryFileActionConfiguration.requestMember != null) {
      setupTemporaryFile();
      try (InputStream inputStream = request.getInputStream()) {
        Files.copy(inputStream, tempFile);
        // Leave the requestMember null if no bytes were read from the inputStream.
        if (tempFile.toFile().length() > 0) {
          expressionEvaluator.setValue(binaryFileActionConfiguration.requestMember, action, tempFile);
        }
      } catch (IOException e) {
        logger.error("Failed to write out binary stream to a file. [" + tempFile.getFileName() + "]", e);
        messageStore.add(new SimpleMessage(MessageType.ERROR, "[couldNotWriteToFile]", messageProvider.getMessage("[couldNotWriteToFile]", e.getMessage())));
      }
    }
  }

  private void setupTemporaryFile() {
    try {
      String tmpdir = System.getProperty("java.io.tmpdir");
      String unique = new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()), "UTF-8").substring(0, 5);
      tempFile = Paths.get(tmpdir + "/" + "_prime_binaryContent_" + unique);
      tempFile.toFile().deleteOnExit();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}

/*
 * Copyright (c) 2025, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.workflow;

import com.google.inject.Inject;
import io.fusionauth.http.ContentTooLargeException;
import io.fusionauth.http.HTTPProcessingException;
import io.fusionauth.http.UnprocessableContentException;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link io.fusionauth.http.HTTPProcessingException} when thrown.
 *
 * @author Daniel DeGroff
 */
public class HTTPProcessingExceptionHandler implements TypedExceptionHandler<HTTPProcessingException> {
  private static final Logger logger = LoggerFactory.getLogger(HTTPProcessingExceptionHandler.class);

  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  private final HTTPResponse response;

  private final ResultStore resultStore;

  @Inject
  public HTTPProcessingExceptionHandler(ResultStore resultStore, MessageStore messageStore, MessageProvider messageProvider, HTTPResponse response) {
    this.resultStore = resultStore;
    this.messageStore = messageStore;
    this.messageProvider = messageProvider;
    this.response = response;
  }

  @Override
  public void handle(HTTPProcessingException exception) {
    String code = getResultCode(exception);
    resultStore.set(code);

    // This may be modified by the result code, but this is the default.
    response.setStatus(exception.getStatus());

    // Get the message from the message provider.  key = name of the class
    try {
      String messageCode = "[" + exception.getClass().getSimpleName() + "]";
      Object[] args = new Object[]{exception.getMessage()};
      String message = messageProvider.getMessage(messageCode, args);
      messageStore.add(new SimpleMessage(MessageType.ERROR, messageCode, message));
    } catch (MissingMessageException mme) {
      // Ignore because there isn't a message
    }
  }

  /**
   * Note that the {@link HTTPProcessingException} comes from java-http so it does not know about result codes.
   *
   * @param exception the exception
   * @return the derived result code.
   */
  private String getResultCode(HTTPProcessingException exception) {
    if (exception.getClass() == ContentTooLargeException.class) {
      return "content-too-large";
    } else if (exception.getClass() == UnprocessableContentException.class) {
      return "unprocessable-content";
    }

    // Unexpected, but it is ok to just return error.
    logger.debug("Unexpected exception [{}]. Using [error] result code as a default.", exception.getClass());
    return "error";
  }
}

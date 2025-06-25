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
import io.fusionauth.http.HTTPProcessingException;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;

/**
 * Handles {@link io.fusionauth.http.HTTPProcessingException} when thrown.
 *
 * @author Daniel DeGroff
 */
public class HTTPProcessingExceptionHandler implements TypedExceptionHandler<HTTPProcessingException> {
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
//    // Set the result code.  if null, grab from mvc configuration
    String code = buildResultCode(exception);
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

  private String buildResultCode(HTTPProcessingException exception) {
    String name = exception.getClass().getSimpleName().replace("Exception", "");
    StringBuilder result = new StringBuilder();
    for (char c : name.toCharArray()) {
      if (Character.isUpperCase(c)) {
        if (!result.isEmpty()) {
          result.append("-");
        }
        result.append(Character.toLowerCase(c));

      } else {
        result.append(c);
      }
    }
    return result.toString();
  }
}

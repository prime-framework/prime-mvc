/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content;

import java.io.IOException;

import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.validation.ValidationException;

/**
 * Explode when no Content-Type handler is found for the requested Content-Type, or no Content-Type header is provided
 * and a body was provided.
 *
 * @author Daniel DeGroff
 */
public class ExplodingContentHandler implements ContentHandler {
  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  private final HTTPRequest request;

  @Inject
  public ExplodingContentHandler(ExpressionEvaluator expressionEvaluator, HTTPRequest request,
                                 ActionInvocationStore store, MessageProvider messageProvider,
                                 MessageStore messageStore) {
    this.request = request;
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
  }

  @Override
  public void cleanup() {

  }

  @Override
  public void handle() throws IOException {
    // If you send a request body, you must have a Content-Type header
    boolean hasBody = request.getBody() != null && request.getBody().array().length > 0;

    if (hasBody) {
      String contentType = request.getContentType();
      if (contentType == null || contentType.equals("")) {
        messageStore.add(new SimpleMessage(MessageType.ERROR, "[MissingContentType]", messageProvider.getMessage("[MissingContentType]")));
        throw new ValidationException();
      } else {
        messageStore.add(new SimpleMessage(MessageType.ERROR, "[UnsupportedContentType]", messageProvider.getMessage("[UnsupportedContentType]", contentType)));
        throw new ValidationException();
      }
    }
  }
}

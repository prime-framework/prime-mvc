/*
 * Copyright (c) 2013-2022, Inversoft Inc., All Rights Reserved
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
import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.content.guice.ContentHandlerFactory;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.validation.ValidationException;
import org.primeframework.mvc.workflow.WorkflowChain;

/**
 * Default content workflow. This uses the Content-Type header and {@link ContentHandler} implementations to handle the
 * content types.
 *
 * @author Brian Pontarelli
 */
public class DefaultContentWorkflow implements ContentWorkflow {
  private final ContentHandlerFactory factory;

  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  private final HTTPRequest request;

  private final ActionInvocationStore store;

  @Inject
  public DefaultContentWorkflow(MessageStore messageStore, ContentHandlerFactory factory,
                                MessageProvider messageProvider, HTTPRequest request,
                                ActionInvocationStore store) {
    this.factory = factory;
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
    this.request = request;
    this.store = store;
  }

  @Override
  public void perform(WorkflowChain workflowChain) throws IOException {
    String contentType = request.getContentType();
    validateContentType(contentType);

    ContentHandler handler = factory.build(contentType);
    if (handler != null) {
      handler.handle();
    }

    workflowChain.continueWorkflow();

    if (handler != null) {
      handler.cleanup();
    }
  }

  private void validateContentType(String contentType) {
    // If we are missing a contentType or the value is empty, it will get handled elsewhere.
    if (contentType != null && !contentType.equals("")) {
      ActionInvocation actionInvocation = store.getCurrent();
      if (actionInvocation != null && actionInvocation.configuration != null) {
        Set<String> validContentTypes = actionInvocation.configuration.validContentTypes;
        Annotation annotation = actionInvocation.method != null ? actionInvocation.method.annotations.get(ValidContentTypes.class) : null;
        if (annotation != null) {
          validContentTypes = Set.of(((ValidContentTypes) annotation).value());
        }

        if (!validContentTypes.isEmpty() && !validContentTypes.contains(contentType)) {
          messageStore.add(new SimpleMessage(MessageType.ERROR, "[InvalidContentType]", messageProvider.getMessage("[InvalidContentType]", contentType, String.join(", ", validContentTypes.stream().sorted().toList()))));
          throw new ValidationException();
        }
      }
    }
  }
}

/*
 * Copyright (c) 2012-2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content.guice;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.content.ContentHandler;
import org.primeframework.mvc.content.ValidContentTypes;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.validation.ValidationException;

/**
 * Builds {@link ContentHandler}s on demand.
 *
 * @author Brian Pontarelli
 */
public class ContentHandlerFactory {
  private static final Map<String, Class<? extends ContentHandler>> bindings = new HashMap<>();

  protected final ActionInvocationStore store;

  private final Injector injector;

  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  @Inject
  public ContentHandlerFactory(ActionInvocationStore store, Injector injector, MessageProvider messageProvider,
                               MessageStore messageStore) {
    this.store = store;
    this.injector = injector;
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
  }

  /**
   * Adds a binding to a ContentHandler for a specific Content-Type.
   *
   * @param binder      The binder to add a hard binding for the handler.
   * @param contentType The content type.
   * @param handlerType The handler type.
   */
  public static void addContentHandler(Binder binder, String contentType, Class<? extends ContentHandler> handlerType) {
    binder.bind(handlerType);
    bindings.put(contentType, handlerType);
  }

  /**
   * Builds a ContentHandler.
   *
   * @param contentType The content type.
   * @return The ContentHandler or null.
   */
  public ContentHandler build(String contentType) {
    validateContentType(contentType);

    Class<? extends ContentHandler> handlerType = bindings.get(contentType);
    if (handlerType == null && contentType != null) {
      handlerType = bindings.get(contentType);
    }

    if (handlerType == null) {
      // Use the default handler
      handlerType = bindings.get("");
    }

    return injector.getInstance(handlerType);
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
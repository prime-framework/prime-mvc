/*
 * Copyright (c) 2015-2019, Inversoft Inc., All Rights Reserved
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

import java.lang.annotation.Annotation;
import java.util.List;

import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.http.Status;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * @author Brian Pontarelli
 */
public abstract class AbstractRedirectResult<T extends Annotation> extends AbstractResult<T> {
  protected final ActionInvocationStore actionInvocationStore;

  protected final MessageStore messageStore;

  protected final HTTPRequest request;

  protected final HTTPResponse response;

  protected AbstractRedirectResult(ExpressionEvaluator expressionEvaluator, ActionInvocationStore actionInvocationStore,
                                   MessageStore messageStore, HTTPRequest request,
                                   HTTPResponse response) {
    super(expressionEvaluator);
    this.actionInvocationStore = actionInvocationStore;
    this.messageStore = messageStore;
    this.request = request;
    this.response = response;
  }

  protected void moveMessagesToFlash() {
    // Move the messages
    List<Message> messages = messageStore.get(MessageScope.REQUEST);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);
  }

  protected void sendRedirect(String uri, String defaultURI, boolean encodeVariables, boolean perm) {
    if (uri == null) {
      uri = expand(defaultURI, actionInvocationStore.getCurrent().action, encodeVariables);
    }

    String context = request.getContextPath();
    if (context.length() > 0 && uri.startsWith("/")) {
      uri = context + uri;
    }

    response.sendRedirect(uri);
    response.setStatus(perm ? Status.MOVED_PERMANENTLY : Status.MOVED_TEMPORARILY);
  }
}

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.FlashScope;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.servlet.ServletTools;

/**
 * @author Brian Pontarelli
 */
public abstract class AbstractRedirectResult<T extends Annotation> extends AbstractResult<T> {
  protected final ActionInvocationStore actionInvocationStore;

  protected final MessageStore messageStore;

  protected final HttpServletRequest request;

  protected final HttpServletResponse response;

  protected AbstractRedirectResult(ExpressionEvaluator expressionEvaluator, ActionInvocationStore actionInvocationStore,
                                   MessageStore messageStore, HttpServletRequest request,
                                   HttpServletResponse response) {
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

    // Preserve previously flashed messages so they will survive redirect after redirect
    @SuppressWarnings("unchecked")
    List<Message> requestList = (List<Message>) request.getAttribute(FlashScope.KEY);
    // If we have a session clear this from the request, in testing we may still use the request for additional assertions.
    //  - And the session will be re-used in between tests.
    if (request.getSession(false) != null) {
      request.removeAttribute(FlashScope.KEY);
    }

    if (requestList != null) {
      messageStore.addAll(MessageScope.FLASH, requestList);
    }
  }

  protected void sendRedirect(String uri, String defaultURI, boolean encodeVariables, boolean perm) throws IOException {
    if (uri == null) {
      uri = expand(defaultURI, actionInvocationStore.getCurrent().action, encodeVariables);
    }

    String context = request.getContextPath();
    if (context.length() > 0 && uri.startsWith("/")) {
      uri = context + uri;
    }

    uri += ServletTools.getSessionId(request);

    response.sendRedirect(uri);
    response.setStatus(perm ? HttpServletResponse.SC_MOVED_PERMANENTLY : HttpServletResponse.SC_MOVED_TEMPORARILY);
  }
}

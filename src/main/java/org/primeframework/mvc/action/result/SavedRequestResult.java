/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.SavedRequest;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.servlet.ServletTools;

import com.google.inject.Inject;

/**
 * This result performs a HTTP redirect to a Saved Request URL. This also transfers all messages from the request to the
 * flash. If we don't transfer the messages they will be lost after the redirect.
 *
 * @author Brian Pontarelli
 */
public class SavedRequestResult extends AbstractResult<SavedRequest> {
  private final ActionInvocationStore actionInvocationStore;

  private final MessageStore messageStore;

  private final HttpServletRequest request;

  private final HttpServletResponse response;

  @Inject
  public SavedRequestResult(MessageStore messageStore, ExpressionEvaluator expressionEvaluator, HttpServletResponse response,
                            HttpServletRequest request, ActionInvocationStore actionInvocationStore) {
    super(expressionEvaluator);
    this.messageStore = messageStore;
    this.response = response;
    this.request = request;
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * {@inheritDoc}
   */
  public void execute(final SavedRequest savedRequest) throws IOException, ServletException {
    List<Message> messages = messageStore.get(MessageScope.REQUEST);
    messageStore.clear(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);

    String uri = null;
    HttpSession session = request.getSession(false);
    if (session != null) {
      SavedHttpRequest savedHttpRequest = (SavedHttpRequest) session.getAttribute(SavedHttpRequest.INITIAL_SESSION_KEY);
      if (savedHttpRequest != null) {
        session.removeAttribute(SavedHttpRequest.INITIAL_SESSION_KEY);
        session.setAttribute(SavedHttpRequest.LOGGED_IN_SESSION_KEY, savedHttpRequest);
        uri = savedHttpRequest.uri;
      }
    }

    if (uri == null) {
      uri = expand(savedRequest.uri(), actionInvocationStore.getCurrent().action, savedRequest.encodeVariables());
    }

    String context = request.getContextPath();
    if (context.length() > 0 && uri.startsWith("/")) {
      uri = context + uri;
    }

    uri += ServletTools.getSessionId(request);

    boolean perm = savedRequest.perm();

    response.setStatus(perm ? 301 : 302);
    response.sendRedirect(uri);
  }

  public static class SavedRequestImpl implements SavedRequest {
    private final String code;

    private final boolean encode;

    private final boolean perm;

    private final String uri;

    public SavedRequestImpl(String uri, String code, boolean perm, boolean encode) {
      this.uri = uri;
      this.code = code;
      this.perm = perm;
      this.encode = encode;
    }

    public Class<? extends Annotation> annotationType() {
      return SavedRequest.class;
    }

    public String code() {
      return code;
    }

    public boolean encodeVariables() {
      return encode;
    }

    public boolean perm() {
      return perm;
    }

    public String uri() {
      return uri;
    }
  }
}
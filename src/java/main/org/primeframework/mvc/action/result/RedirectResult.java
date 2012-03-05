/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import net.java.variable.ExpanderException;
import net.java.variable.ExpanderStrategy;
import net.java.variable.VariableExpander;

import com.google.inject.Inject;

/**
 * This result performs a HTTP redirect to a URL. This also transfers all messages from the request to the flash. If we
 * don't transfer the messages they will be lost after the redirect.
 *
 * @author Brian Pontarelli
 */
public class RedirectResult extends AbstractResult<Redirect> {
  private final MessageStore messageStore;
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final ActionInvocationStore actionInvocationStore;

  @Inject
  public RedirectResult(MessageStore messageStore, ExpressionEvaluator expressionEvaluator, HttpServletResponse response,
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
  public void execute(final Redirect redirect) throws IOException, ServletException {
    List<Message> messages = messageStore.get(MessageScope.REQUEST);
    messageStore.addAll(MessageScope.FLASH, messages);

    String uri = VariableExpander.expand(redirect.uri(), new ExpanderStrategy() {
      public String expand(String variableName) throws ExpanderException {
        try {
          Object action = actionInvocationStore.getCurrent().action();
          String val = expressionEvaluator.getValue(variableName, action, new HashMap<String, String>());
          if (redirect.encodeVariables()) {
            val = URLEncoder.encode(val, "UTF-8");
          }

          return val;
        } catch (UnsupportedEncodingException e) {
          throw new ExpanderException(e);
        }
      }
    });

    String context = request.getContextPath();
    if (context.length() > 0 && uri.startsWith("/")) {
      uri = context + uri;
    }

    boolean perm = redirect.perm();

    response.setStatus(perm ? 301 : 302);
    response.sendRedirect(uri);
  }

  public static class RedirectImpl implements Redirect {
    private final String code;
    private final String uri;
    private final boolean perm;
    private final boolean encode;

    public RedirectImpl(String uri, String code, boolean perm, boolean encode) {
      this.uri = uri;
      this.code = code;
      this.perm = perm;
      this.encode = encode;
    }

    public String code() {
      return code;
    }

    public String uri() {
      return uri;
    }

    public boolean perm() {
      return perm;
    }

    public boolean encodeVariables() {
      return encode;
    }

    public Class<? extends Annotation> annotationType() {
      return Redirect.class;
    }
  }
}
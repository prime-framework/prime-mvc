/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action;

import java.util.ArrayDeque;
import java.util.Deque;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;

/**
 * This class is the default action invocation store.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class DefaultActionInvocationStore implements ActionInvocationStore {
  public static final String ACTION_INVOCATION_DEQUE_KEY = "primeActionInvocationDeque";

  public static final String ACTION_INVOCATION_KEY = "primeActionInvocation";

  private final HTTPRequest request;

  @Inject
  public DefaultActionInvocationStore(HTTPRequest request) {
    this.request = request;
  }

  /**
   * {@inheritDoc}
   */
  public ActionInvocation getCurrent() {
    Deque<ActionInvocation> deque = (Deque<ActionInvocation>) request.getAttribute(ACTION_INVOCATION_DEQUE_KEY);
    if (deque == null) {
      return null;
    }

    return deque.peek();
  }

  /**
   * {@inheritDoc}
   */
  public void setCurrent(ActionInvocation actionInvocation) {
    Deque<ActionInvocation> deque = (Deque<ActionInvocation>) request.getAttribute(ACTION_INVOCATION_DEQUE_KEY);
    if (deque == null) {
      deque = new ArrayDeque<>();
      request.setAttribute(ACTION_INVOCATION_DEQUE_KEY, deque);
    }

    deque.push(actionInvocation);
    request.setAttribute(ACTION_INVOCATION_KEY, actionInvocation);
  }

  /**
   * {@inheritDoc}
   */
  public Deque<ActionInvocation> getDeque() {
    return (Deque<ActionInvocation>) request.getAttribute(ACTION_INVOCATION_DEQUE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  public void removeCurrent() {
    Deque<ActionInvocation> deque = (Deque<ActionInvocation>) request.getAttribute(ACTION_INVOCATION_DEQUE_KEY);
    if (deque == null) {
      return;
    }

    deque.poll();
    request.removeAttribute(ACTION_INVOCATION_KEY);
  }
}
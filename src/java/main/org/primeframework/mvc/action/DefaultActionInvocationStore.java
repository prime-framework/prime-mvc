/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;

/**
 * <p>
 * This class is the default action invocation store.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class DefaultActionInvocationStore implements ActionInvocationStore {
    public static final String ACTION_INVOCATION_DEQUE_KEY = "jcatapultActionInvocationDeque";
    public static final String ACTION_INVOCATION_KEY = "jcatapultActionInvocation";
    private final HttpServletRequest request;

    @Inject
    public DefaultActionInvocationStore(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * {@inheritDoc}
     */
    public ActionInvocation getCurrent() {
        Deque deque = (Deque) request.getAttribute(ACTION_INVOCATION_DEQUE_KEY);
        if (deque == null) {
            return null;
        }

        return (ActionInvocation) deque.peek();
    }

    /**
     * {@inheritDoc}
     */
    public void setCurrent(ActionInvocation invocation) {
        Deque deque = (Deque) request.getAttribute(ACTION_INVOCATION_DEQUE_KEY);
        if (deque == null) {
            deque = new ArrayDeque();
            request.setAttribute(ACTION_INVOCATION_DEQUE_KEY, deque);
        }

        deque.push(invocation);
        request.setAttribute(ACTION_INVOCATION_KEY, invocation);
    }

    /**
     * {@inheritDoc}
     */
    public void removeCurrent() {
        Deque deque = (Deque) request.getAttribute(ACTION_INVOCATION_DEQUE_KEY);
        if (deque == null) {
            return;
        }

        deque.poll();
        request.removeAttribute(ACTION_INVOCATION_KEY);
    }

    /**
     * {@inheritDoc}
     */
    public Deque<ActionInvocation> getDeque() {
        return (Deque<ActionInvocation>) request.getAttribute(ACTION_INVOCATION_DEQUE_KEY);
    }
}
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
package org.primeframework.mvc.scope;

import javax.servlet.http.HttpServletRequest;

import org.primeframework.mvc.scope.annotation.Request;

import com.google.inject.Inject;

/**
 * <p>
 * This is the request scope which fetches and stores values in the
 * HttpServletRequest.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class RequestScope implements Scope<Request> {
    private final HttpServletRequest request;

    @Inject
    public RequestScope(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * {@inheritDoc}
     */
    public Object get(String fieldName, Request scope) {
        String key = scope.value().equals("##field-name##") ? fieldName : scope.value();
        return request.getAttribute(key);
    }

    /**
     * {@inheritDoc}
     */
    public void set(String fieldName, Object value, Request scope) {
        String key = scope.value().equals("##field-name##") ? fieldName : scope.value();
        if (value != null) {
            request.setAttribute(key, value);
        } else {
            request.removeAttribute(key);
        }
    }
}

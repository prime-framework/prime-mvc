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
package org.primeframework.mvc.action.result;

import org.primeframework.mvc.action.ActionInvocation;

import com.google.inject.ImplementedBy;

/**
 * <p>
 * This interface defines the mechanism used to load and cache results.
 * This should be flexible enough to support new results being added,
 * results being updated and results being removed during development.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ImplementedBy(DefaultResultInvocationProvider.class)
public interface ResultInvocationProvider {
    /**
     * Determines the result invocation for the given URI and result code.
     *
     * @param invocation
     * @return  The result invocation for the URI or null if nothing could be found or inferred.
     */
    ResultInvocation lookup(ActionInvocation invocation);

    /**
     * Determines the result invocation for the given action invocation, URI and result code.
     *
     * @param   invocation The ActionInvocation which is used to look for annotations.
     * @param   resultCode The result code or null of there isn't a result code.
     * @return  The result invocation for the URI or null if nothing could be found or inferred.
     */
    ResultInvocation lookup(ActionInvocation invocation, String resultCode);
}
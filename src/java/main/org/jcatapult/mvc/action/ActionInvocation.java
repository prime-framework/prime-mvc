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
package org.jcatapult.mvc.action;

import java.util.Collection;

import org.jcatapult.mvc.action.config.ActionConfiguration;

/**
 * <p>
 * This interface defines the information about an action invocation.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public interface ActionInvocation {
    /**
     * @return  The action object.
     */
    Object action();

    /**
     * @return  The action URI that maps to the action object. This does not include the extension.
     */
    String actionURI();

    /**
     * @return  The URI extension or null if there isn't one.
     */
    String extension();

    /**
     * @return  The additional parts of the URI that come after the action URI.
     */
    Collection<String> uriParameters();

    /**
     * @return  The action configuration for this invocation or null if there isn't one.
     */
    ActionConfiguration configuration();

    /**
     * @return  True if the result should be executed, false otherwise.
     */
    boolean executeResult();

    /**
     * @return  True if the action should be executed, false otherwise.
     */
    boolean executeAction();

    /**
     * @return  The default result code to use if the action is not executed according to the
     *          {@link #executeAction()} method. Or the result code from the action after it
     *          has been invoked.
     */
    String resultCode();

    /**
     * @return  The URI, including the extension if there is one.
     */
    String uri();
}

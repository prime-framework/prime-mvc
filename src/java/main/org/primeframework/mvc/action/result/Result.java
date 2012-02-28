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

import java.io.IOException;
import java.lang.annotation.Annotation;
import javax.servlet.ServletException;

import org.primeframework.mvc.action.ActionInvocation;

/**
 * <p>
 * This interface defines the public API for the result.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public interface Result<T extends Annotation> {
    /**
     * Executes the result.
     *
     * @param   annoation The annotation that caused the result to be invoked.
     * @param   invocation The action invocation (if there is one)
     * @throws  IOException If there is any IO error rendering the result.
     * @throws  ServletException If there is any servlet error rendering the result.
     */
    void execute(T annoation, ActionInvocation invocation) throws IOException, ServletException;
}
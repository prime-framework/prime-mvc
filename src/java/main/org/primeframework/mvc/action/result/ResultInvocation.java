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

import java.lang.annotation.Annotation;

/**
 * <p>
 * This class models the information necessary to invoke a result.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public interface ResultInvocation {
    /**
     * @return  The annotation for the result. This is always non-null so that the annotation type
     *          can be used to locate the correct {@link Result} instance.
     */
    Annotation annotation();

    /**
     * @return  The URI for this result invocation.
     */
    String uri();

    /**
     * @return  The result code from the action invocation or <em>null</em> if there is not action.
     */
    String resultCode();
}
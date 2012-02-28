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
 *
 */
package org.primeframework.mvc.action.result.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.primeframework.mvc.action.result.ForwardResult;

/**
 * <p>
 * This annotation marks a result from an action as a forward to a JSP
 * or FreeMarker template.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ResultAnnotation(ForwardResult.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Forward {
    /**
     * @return  The result code from the action's execute method that this Result is associated with.
     */
    String code() default "success";

    /**
     * @return  The location of the JSP or FreeMarker template (the extension of this attribute
     *          determines if JCatapult forwards to a JSP or renders the FreeMarker template. Any
     *          page with .jsp on the end goes to a JSP and .ftl goes to a FreeMarker template)
     *          If this isn't specified, then the default search method is used to find the correct
     *          FTL or JSP page.
     */
    String page() default "";

    /**
     * @return  The content type of the FTL or JSP page. This defaults to "text/html; charset=UTF-8".
     */
    String contentType() default "text/html; charset=UTF-8";

    /**
     * @return  The HTTP response status code. This defaults to 200.
     */
    int status() default 200;

    /**
     * @return  Overrides the status parameter. If this is set, JCatapult use the value of this parameter
     *          and first expand it. It uses the <code>${variable}</code> notation that is common for
     *          variable expanders. After it has been expanded, the result is converted into an int.
     *          Therefore, you can specify either a number as a String, or a variable expansion. Here
     *          are some examples: <code>"${myStatus}"</code>, <code>"200"</code>, <code>"40${someField}"</code>
     */
    String statusStr() default "";
}

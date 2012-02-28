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
package org.jcatapult.mvc.result.control;

import javax.servlet.http.HttpServletResponse;

import org.jcatapult.mvc.action.result.freemarker.FreeMarkerMap;
import org.jcatapult.mvc.parameter.el.ExpressionEvaluator;

import com.google.inject.Inject;

/**
 * <p>
 * This class an abstract Control implementation that is useful for creating new
 * components that will contain sbippets of functionality that might normally exist
 * inside the result FTL or JSP. An example might be pagination controls or a
 * search result table or a CRUD form. This provides access to the {@link FreeMarkerMap}
 * so that the component can use all of the JCatapult MVC controls (i.e.
 * form, text, actionmessage, etc) and also provides access to values from
 * the current action.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public abstract class AbstractComponentControl extends AbstractControl {
    protected ExpressionEvaluator expressionEvaluator;
    protected HttpServletResponse response;

    @Inject
    public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator;
    }

    @Inject
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * Converts the given parameters into a FreeMarkerMap root node.
     *
     * @return  The root, which is a {@link FreeMarkerMap}.
     */
    protected Object makeRoot() {
        return new FreeMarkerMap(request, response, expressionEvaluator, actionInvocationStore, parameters);
    }
}
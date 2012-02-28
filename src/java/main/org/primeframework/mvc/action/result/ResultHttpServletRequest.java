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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.ExpressionException;

import net.java.util.IteratorEnumeration;

/**
 * <p>
 * This class is a servlet request wrapper that interacts with the expression
 * evaluator in order to pull out attributes from the action.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class ResultHttpServletRequest extends HttpServletRequestWrapper {
    private final ExpressionEvaluator expressionEvaluator;
    private final Object action;

    public ResultHttpServletRequest(HttpServletRequest request, Object action,
            ExpressionEvaluator expressionEvaluator) {
        super(request);
        this.action = action;
        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * First checks if the action has an attribute with the given name by attempting to get the
     * value from the expression evaluator. If that fails this calls super.
     *
     * @param   name The name of the attribute.
     * @return  The attribute or null if it doesn't exist.
     */
    @Override
    public Object getAttribute(String name) {
        Object value;
        try {
            value = expressionEvaluator.getValue(name, action);
        } catch (ExpressionException e) {
            value = super.getAttribute(name);
        }

        return value;
    }

    /**
     * @return  A combined enumeration that contains all of the member names from the action and all
     *          of the request attribute names from super.
     */
    @Override
    public Enumeration getAttributeNames() {
        Set<String> names = new HashSet<String>(expressionEvaluator.getAllMembers(action.getClass()));

        Enumeration en = super.getAttributeNames();
        while (en.hasMoreElements()) {
            String name = (String) en.nextElement();
            names.add(name);
        }

        return new IteratorEnumeration(names.iterator());
    }
}
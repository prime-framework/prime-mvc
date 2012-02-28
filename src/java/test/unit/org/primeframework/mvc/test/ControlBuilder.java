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
package org.primeframework.mvc.test;

import java.util.HashMap;
import java.util.Map;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.result.control.Control;

/**
 * <p>
 * This class is a builder that helps create a test control configuration and
 * expected result.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class ControlBuilder {
    final Map<String, Object> attributes = new HashMap<String, Object>();
    final Map<String, String> dynamicAttributes = new HashMap<String, String>();
    final Control control;
    String result;
    String body;
    ActionInvocation actionInvocation;

    public ControlBuilder(Control control) {
        this.control = control;
    }

    /**
     * Sets an attributes.
     *
     * @param   name The name of the attribute.
     * @param   value The attribute value.
     * @return  This.
     */
    public ControlBuilder withAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    /**
     * Sets a dynamic  attributes.
     *
     * @param   name The name of the attribute.
     * @param   value The attribute value.
     * @return  This.
     */
    public ControlBuilder withDynAttribute(String name, String value) {
        dynamicAttributes.put(name, value);
        return this;
    }

    /**
     * Sets the body.
     *
     * @param   body The body.
     * @return  This.
     */
    public ControlBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Sets the current action invocation.
     *
     * @param   actionInvocation The action invocation.
     * @return  The action invocation.
     */
    public ControlBuilder withActionInvocation(ActionInvocation actionInvocation) {
        this.actionInvocation = actionInvocation;
        return this;
    }

    /**
     * Sets the expected result and then executes the test.
     *
     * @param   result The expected result.
     */
    public void expect(String result) {
        this.result = result;
        ControlTestRunner.run(this);
    }
}
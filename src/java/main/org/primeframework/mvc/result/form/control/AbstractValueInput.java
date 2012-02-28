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
package org.primeframework.mvc.result.form.control;

import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * <p>
 * This class is an abstract class that is used for the text and hidden
 * controls since they both grab the value from the action and setup the
 * value attribute.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public abstract class AbstractValueInput extends AbstractInput {
    protected final ExpressionEvaluator expressionEvaluator;

    protected AbstractValueInput(ExpressionEvaluator expressionEvaluator, boolean labeled) {
        super(labeled);
        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * Adds a String attribute named <strong>value</strong> by pulling the value associated with the
     * control. However, if there is already a value attribute, it is always used. Likewise, if the
     * value attribute is missing, the value associated with the control is null and there is a
     * <strong>defaultValue</strong> attribute, it is used.
     *
     */
    protected void addAdditionalAttributes() {
        // Call super to handle the ID
        super.addAdditionalAttributes();

        String name = (String) attributes.get("name");
        String value;
        if (!attributes.containsKey("value") && currentAction() != null) {
            value = expressionEvaluator.getValue(name, currentAction(), dynamicAttributes);
            if (value == null) {
                value = (String) attributes.get("defaultValue");
            }

            if (value != null) {
                attributes.put("value", value);
            }
        }

        attributes.remove("defaultValue");
    }
}
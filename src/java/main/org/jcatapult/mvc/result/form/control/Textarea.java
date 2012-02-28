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
package org.jcatapult.mvc.result.form.control;

import org.jcatapult.mvc.parameter.el.ExpressionEvaluator;
import org.jcatapult.mvc.result.control.annotation.ControlAttributes;
import org.jcatapult.mvc.result.control.annotation.ControlAttribute;

import com.google.inject.Inject;

/**
 * <p>
 * This class is the control for a input textarea.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ControlAttributes(
    required = {
        @ControlAttribute(name = "name")
    },
    optional = {
        @ControlAttribute(name = "cols", types = {int.class, Integer.class}),
        @ControlAttribute(name = "disabled", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "size", types = {int.class, Integer.class}),
        @ControlAttribute(name = "readonly", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "required", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "rows", types = {int.class, Integer.class}),
        @ControlAttribute(name = "tabindex", types = {int.class, Integer.class})
    }
)
public class Textarea extends AbstractInput {
    private final ExpressionEvaluator expressionEvaluator;

    @Inject
    public Textarea(ExpressionEvaluator expressionEvaluator) {
        super(true);
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
        Object action = currentAction();
        String value;
        if (!attributes.containsKey("value") && action != null) {
            value = expressionEvaluator.getValue(name, action, dynamicAttributes);
            if (value == null) {
                value = (String) attributes.get("defaultValue");
            }

            if (value != null) {
                attributes.put("value", value);
            }
        }

        attributes.remove("defaultValue");
    }

    /**
     * @return  text.ftl
     */
    protected String endTemplateName() {
        return "textarea.ftl";
    }
}
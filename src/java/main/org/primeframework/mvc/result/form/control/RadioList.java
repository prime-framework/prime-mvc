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
package org.primeframework.mvc.result.form.control;

import java.util.Map;
import java.util.Collection;

import org.primeframework.mvc.result.control.annotation.ControlAttributes;
import org.primeframework.mvc.result.control.annotation.ControlAttribute;

/**
 * <p>
 * This class is the control for a radio button.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ControlAttributes(
    required = {
        @ControlAttribute(name = "name"),
        @ControlAttribute(name = "items", types = {Collection.class, Map.class, Object[].class})
    },
    optional = {
        @ControlAttribute(name = "disabled", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "readonly", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "required", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "size", types = {int.class, Integer.class}),
        @ControlAttribute(name = "tabindex", types = {int.class, Integer.class})
    }
)
public class RadioList extends AbstractListInput {
    public RadioList() {
        super(true);
    }

    /**
     * Removes the uncheckedValue attribute and moves it to the parameters.
     *
     * @return  The Map.
     */
    @Override
    protected Map<String, Object> makeParameters() {
        Map<String, Object> params = super.makeParameters();
        String uncheckedValue = (String) attributes.remove("uncheckedValue");
        if (uncheckedValue != null) {
            params.put("uncheckedValue", uncheckedValue);
        } else {
            params.put("uncheckedValue", "");
        }

        return params;
    }

    /**
     * @return  radio-list.ftl
     */
    protected String endTemplateName() {
        return "radio-list.ftl";
    }
}
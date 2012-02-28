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

import javax.servlet.http.HttpServletRequest;

import org.jcatapult.mvc.result.control.annotation.ControlAttribute;
import org.jcatapult.mvc.result.control.annotation.ControlAttributes;

import com.google.inject.Inject;

/**
 * <p>
 * This class is the control for an image button.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ControlAttributes(
    required = {
        @ControlAttribute(name = "name")
    },
    optional = {
        @ControlAttribute(name = "disabled", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "ismap", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "required", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "size", types = {int.class, Integer.class}),
        @ControlAttribute(name = "tabindex", types = {int.class, Integer.class})
    }
)
public class Image extends AbstractButtonInput {
    @Inject
    public Image(HttpServletRequest request) {
        super(request);
    }

    /**
     * Calls super and then moves the ismap attribute out and if it is true set it back in as the
     * String <code>ismap</code>.
     *
     */
    @Override
    protected void addAdditionalAttributes() {
        super.addAdditionalAttributes();
        Boolean ismap = (Boolean) attributes.remove("ismap");
        if (ismap != null && ismap) {
            attributes.put("ismap", "ismap");
        }
    }

    /**
     * @return  image.ftl
     */
    protected String endTemplateName() {
        return "image.ftl";
    }
}
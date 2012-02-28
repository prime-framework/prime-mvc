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
package org.primeframework.mvc.result.form.jsp;

import org.primeframework.mvc.result.form.control.Textarea;

/**
 * <p>
 * This class is the textarea JSP taglib.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class TextareaTag extends AbstractInputTag<Textarea> {
    /**
     * Retrieves the tags rows attribute
     *
     * @return	Returns the tags rows attribute
     */
    public String getRows() {
        return (String) attributes.get("rows");
    }

    /**
     * Populates the tags rows attribute
     *
     * @param	rows The value of the tags rows attribute
     */
    public void setRows(String rows) {
        attributes.put("rows", rows);
    }

    /**
     * Retrieves the tags cols attribute
     *
     * @return	Returns the tags cols attribute
     */
    public String getCols() {
        return (String) attributes.get("cols");
    }

    /**
     * Populates the tags cols attribute
     *
     * @param	cols The value of the tags cols attribute
     */
    public void setCols(String cols) {
        attributes.put("cols", cols);
    }

    /**
     * Retrieves the tags defaultValue attribute
     *
     * @return	Returns the tags defaultValue attribute
     */
    public String getDefaultValue() {
        return (String) attributes.get("defaultValue");
    }

    /**
     * Populates the tags defaultValue attribute
     *
     * @param	defaultValue The value of the tags defaultValue attribute
     */
    public void setDefaultValue(String defaultValue) {
        attributes.put("defaultValue", defaultValue);
    }

    protected Class<Textarea> controlClass() {
        return Textarea.class;
    }
}
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
package org.primeframework.mvc.result.form.jsp;

import org.primeframework.mvc.result.form.control.Form;
import org.primeframework.mvc.result.jsp.AbstractControlTag;

/**
 * <p>
 * This class is the JSP taglib for the button control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class FormTag extends AbstractControlTag<Form> {
    /**
     * Retrieves the tags accept attribute
     *
     * @return	Returns the tags accept attribute
     */
    public String getAccept() {
        return (String) attributes.get("accept");
    }

    /**
     * Populates the tags accept attribute
     *
     * @param	accept The value of the tags accept attribute
     */
    public void setAccept(String accept) {
        attributes.put("accept", accept);
    }

    /**
     * Retrieves the tags action attribute
     *
     * @return	Returns the tags action attribute
     */
    public String getAction() {
        return (String) attributes.get("action");
    }

    /**
     * Populates the tags action attribute
     *
     * @param	action The value of the tags action attribute
     */
    public void setAction(String action) {
        attributes.put("action", action);
    }

    /**
     * Retrieves the tags method attribute
     *
     * @return	Returns the tags method attribute
     */
    public String getMethod() {
        return (String) attributes.get("method");
    }

    /**
     * Populates the tags method attribute
     *
     * @param	method The value of the tags method attribute
     */
    public void setMethod(String method) {
        attributes.put("method", method);
    }

    /**
     * Retrieves the tags enctype attribute
     *
     * @return	Returns the tags enctype attribute
     */
    public String getEnctype() {
        return (String) attributes.get("enctype");
    }

    /**
     * Populates the tags enctype attribute
     *
     * @param	enctype The value of the tags enctype attribute
     */
    public void setEnctype(String enctype) {
        attributes.put("enctype", enctype);
    }

    /**
     * Retrieves the tags onsubmit attribute
     *
     * @return	Returns the tags onsubmit attribute
     */
    public String getOnsubmit() {
        return (String) attributes.get("onsubmit");
    }

    /**
     * Populates the tags onsubmit attribute
     *
     * @param	onsubmit The value of the tags onsubmit attribute
     */
    public void setOnsubmit(String onsubmit) {
        attributes.put("onsubmit", onsubmit);
    }

    /**
     * Retrieves the tags onreset attribute
     *
     * @return	Returns the tags onreset attribute
     */
    public String getOnreset() {
        return (String) attributes.get("onreset");
    }

    /**
     * Populates the tags onreset attribute
     *
     * @param	onreset The value of the tags onreset attribute
     */
    public void setOnreset(String onreset) {
        attributes.put("onreset", onreset);
    }

    /**
     * @return  The {@link Form} class.
     */
    protected Class<Form> controlClass() {
        return Form.class;
    }
}
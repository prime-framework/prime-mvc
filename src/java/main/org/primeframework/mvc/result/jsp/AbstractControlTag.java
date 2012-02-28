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
package org.primeframework.mvc.result.jsp;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.primeframework.guice.GuiceContainer;
import org.primeframework.mvc.result.control.Control;

/**
 * <p>
 * This class is the abstract control tag for the majority of the
 * all controls. This class is simple in that it just collects the
 * tag attributes from the JSP and passes them along to the correct
 * implementation of the {@link Control} interface.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public abstract class AbstractControlTag<T extends Control> extends TagSupport implements DynamicAttributes {
    protected Map<String, Object> attributes = new HashMap<String, Object>();
    protected Map<String, String> parameterAttributes = new HashMap<String, String>();
    private T control;

    /**
     * Retrieves the tags bundle attribute.
     *
     * @return	Returns the tags bundle attribute.
     */
    public String getBundle() {
        return (String) attributes.get("bundle");
    }

    /**
     * Populates the tags bundle attribute.
     *
     * @param	bundle The value of the tags bundle attribute.
     */
    public void setBundle(String bundle) {
        attributes.put("bundle", bundle);
    }

    /**
     * @return  The Control class that the tag renders. Sub-classes must implement this method in
     *          order to render a Control.
     */
    protected abstract Class<T> controlClass();

    /**
     * Sets any dynamic attributes on the tag.
     *
     * @param   uri The URI.
     * @param   name The name of the dynamic attribute.
     * @param   value The value of the dynamic attribute.
     */
    public void setDynamicAttribute(String uri, String name, Object value) {
        if (name.startsWith("_")) {
            parameterAttributes.put(name.substring(1), value.toString());
        } else {
            attributes.put(name, value);
        }
    }

    @Override
    public int doStartTag() {
        control = GuiceContainer.getInjector().getInstance(controlClass());
        control.renderStart(pageContext.getOut(), attributes, parameterAttributes);
        return Tag.EVAL_PAGE;
    }

    @Override
    public int doEndTag() {
        control.renderEnd(pageContext.getOut());
        attributes.clear();
        return Tag.EVAL_PAGE;
    }
}
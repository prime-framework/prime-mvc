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

import org.primeframework.mvc.result.control.Control;
import org.primeframework.mvc.result.jsp.AbstractControlTag;

/**
 * <p>
 * This class is the abstract control tag for the majority of the
 * HTML controls. This class is simple in that it just collects the
 * tag attributes from the JSP and passes them along to the correct
 * implementation of the {@link Control} interface.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public abstract class AbstractHTMLControlTag<T extends Control> extends AbstractControlTag<T> {
    //-------------------------------------------------------------------------
    //----------------------- Core attributes for HTML tags -------------------
    //-------------------------------------------------------------------------

    /**
     * Gets the tags name.
     *
     * @return  The tags name
     */
    public String getName() {
        return (String) attributes.get("name");
    }

    /**
     * Sets the tags name. This method should only be called by the container.
     * Sub-classes should not call this name under any circumstances. Instead they
     * should use the variable directly.
     *
     * @param   name The name of the tag
     */
    public void setName(String name) {
        attributes.put("name", name);
    }

    /**
     * Gets the tags style.
     *
     * @return  The tags style
     */
    public String getStyle() {
        return (String) attributes.get("style");
    }

    /**
     * Sets the tags style and adds it to the attributes hash
     *
     * @param   style The new style of the tag
     */
    public void setStyle(String style) {
        attributes.put("style", style);
    }

    /**
     * Gets the tags title
     *
     * @return  The tags title
     */
    public String getTitle() {
        return (String) attributes.get("title");
    }

    /**
     * Sets the tags title
     *
     * @param   title The new title of the tag
     */
    public void setTitle(String title) {
        attributes.put("title", title);
    }


    //-------------------------------------------------------------------------
    //------------ Internationalization attributes for HTML tags --------------
    //-------------------------------------------------------------------------

    /**
     * Retrieves the tags lang attribute
     *
     * @return	Returns the tags lang attribute
     */
    public String getLang() {
        return (String) attributes.get("lang");
    }

    /**
     * Populates the tags lang attribute
     *
     * @param	lang The value of the tags lang attribute
     */
    public void setLang(String lang) {
        attributes.put("lang", lang);
    }

    /**
     * Retrieves the tags dir attribute
     *
     * @return	Returns the tags dir attribute
     */
    public String getDir() {
        return (String) attributes.get("dir");
    }

    /**
     * Populates the tags dir attribute
     *
     * @param	dir The value of the tags dir attribute
     */
    public void setDir(String dir) {
        attributes.put("dir", dir);
    }

    //-------------------------------------------------------------------------
    //--------------------- UI event attributes for HTML tags -----------------
    //-------------------------------------------------------------------------

    /**
     * Retrieves the onclick attribute of the tag
     *
     * @return	Returns the onclick attribute of the tag
     */
    public String getOnclick() {
        return (String) attributes.get("onclick");
    }

    /**
     * Populates the onclick attribute of the tag
     *
     * @param	onclick The value of the onclick attribute of the tag
     */
    public void setOnclick(String onclick) {
        attributes.put("onclick", onclick);
    }

    /**
     * Retrieves the tags ondblclick attribute
     *
     * @return	Returns the tags ondblclick attribute
     */
    public String getOndblclick() {
        return (String) attributes.get("ondblclick");
    }

    /**
     * Populates the tags ondblclick attribute
     *
     * @param	ondblclick The value of the tags ondblclick attribute
     */
    public void setOndblclick(String ondblclick) {
        attributes.put("ondblclick", ondblclick);
    }

    /**
     * Retrieves the tags onmousedown attribute
     *
     * @return	Returns the tags onmousedown attribute
     */
    public String getOnmousedown() {
        return (String) attributes.get("onmousedown");
    }

    /**
     * Populates the tags onmousedown attribute
     *
     * @param	onmousedown The value of the tags onmousedown attribute
     */
    public void setOnmousedown(String onmousedown) {
        attributes.put("onmousedown", onmousedown);
    }

    /**
     * Retrieves the tags onmouseup attribute
     *
     * @return	Returns the tags onmouseup attribute
     */
    public String getOnmouseup() {
        return (String) attributes.get("onmouseup");
    }

    /**
     * Populates the tags onmouseup attribute
     *
     * @param	onmouseup The value of the tags onmouseup attribute
     */
    public void setOnmouseup(String onmouseup) {
        attributes.put("onmouseup", onmouseup);
    }

    /**
     * Retrieves the tags onmouseover attribute
     *
     * @return	Returns the tags onmouseover attribute
     */
    public String getOnmouseover() {
        return (String) attributes.get("onmouseover");
    }

    /**
     * Populates the tags onmouseover attribute
     *
     * @param	onmouseover The value of the tags onmouseover attribute
     */
    public void setOnmouseover(String onmouseover) {
        attributes.put("onmouseover", onmouseover);
    }

    /**
     * Retrieves the tags onmousemove attribute
     *
     * @return	Returns the tags onmousemove attribute
     */
    public String getOnmousemove() {
        return (String) attributes.get("onmousemove");
    }

    /**
     * Populates the tags onmousemove attribute
     *
     * @param	onmousemove The value of the tags onmousemove attribute
     */
    public void setOnmousemove(String onmousemove) {
        attributes.put("onmousemove", onmousemove);
    }

    /**
     * Retrieves the tags onmouseout attribute
     *
     * @return	Returns the tags onmouseout attribute
     */
    public String getOnmouseout() {
        return (String) attributes.get("onmouseout");
    }

    /**
     * Populates the tags onmouseout attribute
     *
     * @param	onmouseout The value of the tags onmouseout attribute
     */
    public void setOnmouseout(String onmouseout) {
        attributes.put("onmouseout", onmouseout);
    }

    /**
     * Retrieves the tags onkeypress attribute
     *
     * @return	Returns the tags onkeypress attribute
     */
    public String getOnkeypress() {
        return (String) attributes.get("onkeypress");
    }

    /**
     * Populates the tags onkeypress attribute
     *
     * @param	onkeypress The value of the tags onkeypress attribute
     */
    public void setOnkeypress(String onkeypress) {
        attributes.put("onkeypress", onkeypress);
    }

    /**
     * Retrieves the tags onkeydown attribute
     *
     * @return	Returns the tags onkeydown attribute
     */
    public String getOnkeydown() {
        return (String) attributes.get("onkeydown");
    }

    /**
     * Populates the tags onkeydown attribute
     *
     * @param	onkeydown The value of the tags onkeydown attribute
     */
    public void setOnkeydown(String onkeydown) {
        attributes.put("onkeydown", onkeydown);
    }

    /**
     * Retrieves the tags onkeyup attribute
     *
     * @return	Returns the tags onkeyup attribute
     */
    public String getOnkeyup() {
        return (String) attributes.get("onkeyup");
    }

    /**
     * Populates the tags onkeyup attribute
     *
     * @param	onkeyup The value of the tags onkeyup attribute
     */
    public void setOnkeyup(String onkeyup) {
        attributes.put("onkeyup", onkeyup);
    }
}
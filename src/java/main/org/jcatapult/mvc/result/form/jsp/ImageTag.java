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
package org.jcatapult.mvc.result.form.jsp;

import org.jcatapult.mvc.result.form.control.Image;

/**
 * <p>
 * This class is the JSP taglib for the button control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class ImageTag extends AbstractInputTag<Image> {
    /**
     * Retrieves the tags ismap attribute
     *
     * @return	Returns the tags ismap attribute
     */
    public Boolean getIsmap() {
        return (Boolean) attributes.get("ismap");
    }

    /**
     * Populates the tags ismap attribute
     *
     * @param	ismap The value of the tags ismap attribute
     */
    public void setIsmap(Boolean ismap) {
        attributes.put("ismap", ismap);
    }

    /**
     * Retrieves the tags usemap attribute
     *
     * @return	Returns the tags usemap attribute
     */
    public String getUsemap() {
        return (String) attributes.get("usemap");
    }

    /**
     * Populates the tags usemap attribute
     *
     * @param	usemap The value of the tags usemap attribute
     */
    public void setUsemap(String usemap) {
        attributes.put("usemap", usemap);
    }

    /**
     * Retrieves the tags src attribute
     *
     * @return	Returns the tags src attribute
     */
    public String getSrc() {
        return (String) attributes.get("src");
    }

    /**
     * Populates the tags src attribute
     *
     * @param	src The value of the tags src attribute
     */
    public void setSrc(String src) {
        attributes.put("src", src);
    }

    /**
     * @return  The {@link Image} class.
     */
    protected Class<Image> controlClass() {
        return Image.class;
    }
}
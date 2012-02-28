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

import org.primeframework.mvc.result.form.control.CountriesSelect;

/**
 * <p>
 * This class is the JSP taglib for the countries select control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class CountriesSelectTag extends SelectTag<CountriesSelect> {
    /**
     * Retrieves the tags includeBlank attribute
     *
     * @return	Returns the tags includeBlank attribute
     */
    public Boolean getIncludeBlank() {
        return (Boolean) attributes.get("includeBlank");
    }

    /**
     * Populates the tags includeBlank attribute
     *
     * @param	includeBlank The value of the tags includeBlank attribute
     */
    public void setIncludeBlank(Boolean includeBlank) {
        attributes.put("includeBlank", includeBlank);
    }

    /**
     * Retrieves the tags preferredCodes attribute
     *
     * @return	Returns the tags preferredCodes attribute
     */
    public String getPreferredCodes() {
        return (String) attributes.get("preferredCodes");
    }

    /**
     * Populates the tags preferredCodes attribute
     *
     * @param	preferredCodes The value of the tags preferredCodes attribute
     */
    public void setPreferredCodes(String preferredCodes) {
        attributes.put("preferredCodes", preferredCodes);
    }

    /**
     * @return  The {@link CountriesSelect} class.
     */
    protected Class<CountriesSelect> controlClass() {
        return CountriesSelect.class;
    }
}
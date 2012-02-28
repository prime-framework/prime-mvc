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

import org.primeframework.mvc.result.form.control.YearsSelect;

/**
 * <p>
 * This class is the JSP taglib for the years select control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class YearsSelectTag extends SelectTag<YearsSelect> {
    /**
     * Retrieves the tags startYear attribute
     *
     * @return	Returns the tags startYear attribute
     */
    public Integer getStartYear() {
        return (Integer) attributes.get("startYear");
    }

    /**
     * Populates the tags startYear attribute
     *
     * @param	startYear The value of the tags startYear attribute
     */
    public void setStartYear(Integer startYear) {
        attributes.put("startYear", startYear);
    }

    /**
     * Retrieves the tags endYear attribute
     *
     * @return	Returns the tags endYear attribute
     */
    public Integer getEndYear() {
        return (Integer) attributes.get("endYear");
    }

    /**
     * Populates the tags endYear attribute
     *
     * @param	endYear The value of the tags endYear attribute
     */
    public void setEndYear(Integer endYear) {
        attributes.put("endYear", endYear);
    }

    /**
     * Retrieves the tags numberOfYears attribute
     *
     * @return	Returns the tags numberOfYears attribute
     */
    public Integer getNumberOfYears() {
        return (Integer) attributes.get("numberOfYears");
    }

    /**
     * Populates the tags numberOfYears attribute
     *
     * @param	numberOfYears The value of the tags numberOfYears attribute
     */
    public void setNumberOfYears(Integer numberOfYears) {
        attributes.put("numberOfYears", numberOfYears);
    }

    /**
     * @return  The {@link YearsSelect} class.
     */
    protected Class<YearsSelect> controlClass() {
        return YearsSelect.class;
    }
}
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

import java.util.ArrayList;
import java.util.List;

import org.jcatapult.mvc.result.control.annotation.ControlAttribute;
import org.jcatapult.mvc.result.control.annotation.ControlAttributes;
import org.joda.time.LocalDate;

/**
 * <p>
 * This class is the control for a select box that contains years.
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
        @ControlAttribute(name = "multiple", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "readonly", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "required", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "size", types = {int.class, Integer.class}),
        @ControlAttribute(name = "tabindex", types = {int.class, Integer.class}),
        @ControlAttribute(name = "startYear", types = {int.class, Integer.class}),
        @ControlAttribute(name = "endYear", types = {int.class, Integer.class}),
        @ControlAttribute(name = "numberOfYear", types = {int.class, Integer.class})
    }
)
public class YearsSelect extends Select {
    /**
     * <p>
     * Calls super then adds the years Map.
     * </p>
     *
     */
    @Override
    protected void addAdditionalAttributes() {
        super.addAdditionalAttributes();

        Integer start = (Integer) attributes.remove("startYear");
        Integer end = (Integer) attributes.remove("endYear");
        Integer numberOfYears = (Integer) attributes.remove("numberOfYears");
        if (start == null) {
            start = new LocalDate().getYear();
        }

        if (numberOfYears != null) {
            end = start + numberOfYears;
        } else if (end == null) {
            end = start + 10;
        } else if (end != null) {
            end = end + 1;
        }

        List<Integer> years = new ArrayList<Integer>();
        for (int i = start; i < end; i++) {
            years.add(i);
        }

        attributes.put("items", years);
    }
}
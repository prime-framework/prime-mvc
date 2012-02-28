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
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.primeframework.mvc.result.control.annotation.ControlAttributes;
import org.primeframework.mvc.result.control.annotation.ControlAttribute;

/**
 * <p>
 * This class is the control for a select box that contains months.
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
        @ControlAttribute(name = "tabindex", types = {int.class, Integer.class})
    }
)
public class MonthsSelect extends Select {
    /**
     * <p>
     * Calls super then adds the months Map.
     * </p>
     *
     */
    @Override
    protected void addAdditionalAttributes() {
        super.addAdditionalAttributes();

        Map<Integer, String> months = new TreeMap<Integer, String>();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM").withLocale(locale);
        for (int i = 1; i <= 12; i++) {
            LocalDate date = new LocalDate(2008, i, 1);
            months.put(i, formatter.print(date));
        }

        attributes.put("items", months);
    }
}
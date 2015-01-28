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
package org.primeframework.mvc.control.form;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.primeframework.mvc.control.annotation.ControlAttribute;
import org.primeframework.mvc.control.annotation.ControlAttributes;

/**
 * This class is the control for a select box that contains years.
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
    required = {
        @ControlAttribute(name = "name")
    },
    optional = {
        @ControlAttribute(name = "disabled", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "endYear", types = {int.class, Number.class}),
        @ControlAttribute(name = "multiple", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "numberOfYear", types = {int.class, Number.class}),
        @ControlAttribute(name = "readonly", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "required", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "size", types = {int.class, Number.class}),
        @ControlAttribute(name = "startYear", types = {int.class, Number.class}),
        @ControlAttribute(name = "tabindex", types = {int.class, Number.class})
    }
)
public class YearSelect extends Select {
  /**
   * Calls super then adds the years Map.
   */
  @Override
  protected void addAdditionalAttributes() {
    super.addAdditionalAttributes();

    Number start = (Number) attributes.remove("startYear");
    Number end = (Number) attributes.remove("endYear");
    Number numberOfYears = (Number) attributes.remove("numberOfYears");
    if (start == null) {
      start = LocalDate.now().getYear();
    }

    if (numberOfYears != null) {
      end = start.intValue() + numberOfYears.intValue();
    } else if (end == null) {
      end = start.intValue() + 10;
    } else {
      end = end.intValue() + 1;
    }

    List<Integer> years = new ArrayList<>();
    for (int i = start.intValue(); i < end.intValue(); i++) {
      years.add(i);
    }

    attributes.put("items", years);
  }
}
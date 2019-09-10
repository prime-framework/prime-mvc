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
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

import org.primeframework.mvc.control.annotation.ControlAttribute;
import org.primeframework.mvc.control.annotation.ControlAttributes;

/**
 * This class is the control for a select box that contains months.
 *
 * @author Brian Pontarelli
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
        @ControlAttribute(name = "size", types = {int.class, Number.class}),
        @ControlAttribute(name = "tabindex", types = {int.class, Number.class})
    }
)
public class MonthSelect extends Select {

  /**
   * Calls super then adds the months Map.
   */
  @Override
  protected void addAdditionalAttributes() {
    super.addAdditionalAttributes();

    Map<Integer, String> months = new TreeMap<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM").withLocale(localeProvider.get());
    for (int i = 1; i <= 12; i++) {
      LocalDate date = LocalDate.of(2008, i, 1);
      months.put(i, date.format(formatter));
    }

    attributes.put("items", months);
  }
}
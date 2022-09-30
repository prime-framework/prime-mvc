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

import java.util.Collection;
import java.util.Map;

import com.google.inject.Inject;
import org.primeframework.mvc.control.annotation.ControlAttribute;
import org.primeframework.mvc.control.annotation.ControlAttributes;

/**
 * This class is the control for a set of checkboxes.
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
    required = {
        @ControlAttribute(name = "name", types = {String.class}),
        @ControlAttribute(name = "items", types = {Collection.class, Map.class, Object[].class})
    },
    optional = {
        @ControlAttribute(name = "disabled", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "headerL10n", types = {String.class}),
        @ControlAttribute(name = "headerValue", types = {String.class}),
        @ControlAttribute(name = "l10nExpr", types = {String.class}),
        @ControlAttribute(name = "readonly", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "required", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "tabindex", types = {int.class, Number.class}),
        @ControlAttribute(name = "textExpr", types = {String.class}),
        @ControlAttribute(name = "valueExpr", types = {String.class})
    }
)

public class CheckboxList extends AbstractListInput {
  @Inject
  public CheckboxList() {
    super(true);
  }

  /**
   * @return checkbox-list.ftl
   */
  protected String endTemplateName() {
    return "checkbox-list.ftl";
  }
}
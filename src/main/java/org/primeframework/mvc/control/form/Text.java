/*
 * Copyright (c) 2001-2014, Inversoft Inc., All Rights Reserved
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

import org.primeframework.mvc.control.annotation.ControlAttribute;
import org.primeframework.mvc.control.annotation.ControlAttributes;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import com.google.inject.Inject;

/**
 * This class is the control for a input type=text.
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
  required = {
    @ControlAttribute(name = "name")
  },
  optional = {
    @ControlAttribute(name = "disabled", types = {boolean.class, Boolean.class}),
    @ControlAttribute(name = "maxlength", types = {int.class, Number.class}),
    @ControlAttribute(name = "readonly", types = {boolean.class, Boolean.class}),
    @ControlAttribute(name = "required", types = {boolean.class, Boolean.class}),
    @ControlAttribute(name = "size", types = {int.class, Number.class}),
    @ControlAttribute(name = "tabindex", types = {int.class, Number.class}),
    @ControlAttribute(name = "autocomplete", types = {String.class}),
    @ControlAttribute(name = "placeholder", types = {String.class})
  }
)
public class Text extends AbstractValueInput {
  @Inject
  public Text(ExpressionEvaluator expressionEvaluator) {
    super(expressionEvaluator, true);
  }

  /**
   * @return text.ftl
   */
  protected String endTemplateName() {
    return "text.ftl";
  }
}
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

import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.control.annotation.ControlAttribute;
import org.primeframework.mvc.control.annotation.ControlAttributes;

import com.google.inject.Inject;

/**
 * This class is the control for a input type=hidden.
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
  required = {
    @ControlAttribute(name = "name")
  }
)
public class Hidden extends AbstractValueInput {
  @Inject
  public Hidden(ExpressionEvaluator expressionEvaluator) {
    super(expressionEvaluator, false);
  }

  /**
   * @return hidden.ftl
   */
  protected String endTemplateName() {
    return "hidden.ftl";
  }
}
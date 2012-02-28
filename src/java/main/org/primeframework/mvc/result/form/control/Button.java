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

import javax.servlet.http.HttpServletRequest;

import org.primeframework.mvc.result.control.annotation.ControlAttribute;
import org.primeframework.mvc.result.control.annotation.ControlAttributes;

import com.google.inject.Inject;

/**
 * <p> This class is the control for a button button. </p>
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
  required = {
    @ControlAttribute(name = "name")
  },
  optional = {
    @ControlAttribute(name = "disabled", types = {boolean.class, Boolean.class}),
    @ControlAttribute(name = "required", types = {boolean.class, Boolean.class}),
    @ControlAttribute(name = "size", types = {int.class, Integer.class}),
    @ControlAttribute(name = "tabindex", types = {int.class, Integer.class})
  }
)
public class Button extends AbstractButtonInput {
  @Inject
  public Button(HttpServletRequest request) {
    super(request);
  }

  /**
   * @return button.ftl
   */
  protected String endTemplateName() {
    return "button.ftl";
  }
}
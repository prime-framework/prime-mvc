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
package org.primeframework.mvc.result.form.jsp;

import org.primeframework.mvc.result.control.Control;

/**
 * <p> This class is an abstract class for all controls that can be checked such as radio buttons and checkboxes. </p>
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractCheckedInputTag<T extends Control> extends AbstractInputTag<T> {
  /**
   * Retrieves the tag's checked attribute
   *
   * @return The tag's checked attribute
   */
  public Boolean getChecked() {
    return (Boolean) attributes.get("checked");
  }

  /**
   * Populates the tag's checked attribute
   *
   * @param checked The tag's checked attribute
   */
  public void setChecked(Boolean checked) {
    attributes.put("checked", checked);
  }

  /**
   * Retrieves the tags defaultChecked attribute
   *
   * @return Returns the tags defaultChecked attribute
   */
  public Boolean getDefaultChecked() {
    return (Boolean) attributes.get("defaultChecked");
  }

  /**
   * Populates the tags defaultChecked attribute
   *
   * @param defaultChecked The value of the tags defaultChecked attribute
   */
  public void setDefaultChecked(Boolean defaultChecked) {
    attributes.put("defaultChecked", defaultChecked);
  }

  /**
   * Retrieves the tags uncheckedValue attribute
   *
   * @return Returns the tags uncheckedValue attribute
   */
  public String getUncheckedValue() {
    return (String) attributes.get("uncheckedValue");
  }

  /**
   * Populates the tags uncheckedValue attribute
   *
   * @param uncheckedValue The value of the tags uncheckedValue attribute
   */
  public void setUncheckedValue(String uncheckedValue) {
    attributes.put("uncheckedValue", uncheckedValue);
  }
}
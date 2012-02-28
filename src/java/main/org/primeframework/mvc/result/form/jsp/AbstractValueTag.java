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
 * <p> This class is an abstract class for controls that use the value from the action, including file and hidden. </p>
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractValueTag<T extends Control> extends AbstractInputTag<T> {
  /**
   * Retrieves the tags defaultValue attribute
   *
   * @return Returns the tags defaultValue attribute
   */
  public String getDefaultValue() {
    return (String) attributes.get("defaultValue");
  }

  /**
   * Populates the tags defaultValue attribute
   *
   * @param defaultValue The value of the tags defaultValue attribute
   */
  public void setDefaultValue(String defaultValue) {
    attributes.put("defaultValue", defaultValue);
  }
}
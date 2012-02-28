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

import org.primeframework.mvc.result.form.control.File;

/**
 * <p> This class is the JSP taglib for the file control. </p>
 *
 * @author Brian Pontarelli
 */
public class FileTag extends AbstractInputTag<File> {
  /**
   * Retrieves the tags accept attribute
   *
   * @return Returns the tags accept attribute
   */
  public String getAccept() {
    return (String) attributes.get("accept");
  }

  /**
   * Populates the tags accept attribute
   *
   * @param accept The value of the tags accept attribute
   */
  public void setAccept(String accept) {
    attributes.put("accept", accept);
  }

  /**
   * @return The {@link File} class.
   */
  protected Class<File> controlClass() {
    return File.class;
  }
}
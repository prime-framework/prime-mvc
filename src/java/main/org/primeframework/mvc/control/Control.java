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
package org.primeframework.mvc.control;

import java.io.Writer;
import java.util.Map;

/**
 * <p> This interface defines a control is called from a JSP tag library or from a FreeMarker template to render some
 * HTML or other type of output. </p>
 *
 * @author Brian Pontarelli
 */
public interface Control {
  /**
   * Renders the start of the control. If the control doesn't have a start and an end, this method should be empty.
   *
   * @param writer            The writer to write the output to.
   * @param attributes        The attributes that are passed from the JSP tag or the FreeMarker directive.
   * @param dynamicAttributes The dynamic attributes that are passed to the tag. These are described in the class
   *                          comment of the {@link org.primeframework.mvc.parameter.ParameterWorkflow} class. In most
   *                          cases these are used for type conversion, such as date formats and currency codes.
   */
  void renderStart(Writer writer, Map<String, Object> attributes, Map<String, String> dynamicAttributes);

  /**
   * Renders the body of the control.
   *
   * @param writer The writer to write the body to.
   * @param body   The body.
   */
  void renderBody(Writer writer, Body body);

  /**
   * Renders the end of the control. If the control doesn't have a start and an end, this method should be perform the
   * main rendering.
   *
   * @param writer The writer to write the output to.
   */
  void renderEnd(Writer writer);
}
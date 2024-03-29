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

import java.util.Map;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;

/**
 * This class is the abstract control for a button input. Button tags are simple in that the value attribute is dynamic
 * and message based.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractButtonInput extends AbstractInput {
  protected final HTTPRequest request;

  @Inject
  public AbstractButtonInput(HTTPRequest request) {
    super(true);
    this.request = request;
  }

  /**
   * Calls the super method and then moves the label parameter to the value attribute since the value is viewable by the
   * user.
   *
   * @return The fixed parameters map.
   */
  @Override
  protected Map<String, Object> makeParameters() {
    Map<String, Object> parameters = super.makeParameters();
    Object label = parameters.remove("label");
    attributes.put("value", label);

    String action = (String) attributes.remove("action");
    if (action != null) {
      String contextPath = request.getContextPath();
      if (action.startsWith("/") && contextPath.length() > 0) {
        action = contextPath + action;
      }

      parameters.put("actionURI", action);
    }

    return parameters;
  }
}
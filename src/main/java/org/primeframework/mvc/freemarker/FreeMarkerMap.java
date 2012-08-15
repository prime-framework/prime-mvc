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
package org.primeframework.mvc.freemarker;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.ControlsHashModel;
import org.primeframework.mvc.control.Control;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import com.google.inject.Inject;

/**
 * This class is a FreeMarker model that provides access in the templates to the request, session and context attributes
 * as well as values from the action and the Control directives via the {@link ControlsHashModel} class.
 *
 * @author Brian Pontarelli
 */
public class FreeMarkerMap extends ControllessFreeMarkerMap {

  @Inject
  public FreeMarkerMap(ServletContext context, HttpServletRequest request, HttpServletResponse response,
                       ExpressionEvaluator expressionEvaluator, ActionInvocationStore actionInvocationStore,
                       MessageStore messageStore, Map<String, Set<Control>> controlSets,
                       Map<String, Set<NamedTemplateModel>> models) {
    super(context, request, response, expressionEvaluator, actionInvocationStore, messageStore, models);

    // Add the controls
    for (String prefix : controlSets.keySet()) {
      objects.put(prefix, new ControlsHashModel(controlSets.get(prefix)));
    }

    // TODO add debugging for figuring out what scope an object is in
  }
}
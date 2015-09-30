/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * This class implements the URIParameterWorkflow using patterns derived from the WADL specification.
 * <p>
 * The base URI for the action is fixed based on the package and class name. However, everything after the base can be
 * set into properties or fields of the action class using the WADL pattern here. The pattern is like this:
 * <p>
 * <pre>
 * {id}
 * </pre>
 * <p>
 * If the classes base URI is /admin/user/edit, the full specification for the URI that action can handle would be:
 * <p>
 * <pre>
 * /admin/user/edit/{id}
 * </pre>
 * <p>
 * If the URI is <strong>/admin/user/edit/42</strong>, the value of 42 would be set into the action's
 * <strong>id</strong> property or field.
 * <p>
 * The difference between the standard WADL specification pattern and Prime is that Prime allows you to capture all of
 * the URI parameters or just what is left over after everything else has been handled using a special notation. This
 * notation is:
 * <p>
 * <pre>
 * {id}/{*theRest}
 * </pre>
 * <p>
 * If the URI is <strong>/admin/user/edit/42/foo/bar</strong>, the value of 42 would be set into the action's
 * <strong>id</strong> property or field and a List of Strings will be set into the property or field named
 * <strong>theRest</strong>. The property that handles the all or the remaining parameters must be of type
 * Collection&lt;String> or List&lt;String>.
 *
 * @author Brian Pontarelli
 */
public class DefaultURIParameterWorkflow implements URIParameterWorkflow {
  private final ActionInvocationStore actionInvocationStore;

  private final HttpServletRequest request;

  @Inject
  public DefaultURIParameterWorkflow(HttpServletRequest request, ActionInvocationStore actionInvocationStore) {
    this.request = request;
    this.actionInvocationStore = actionInvocationStore;
  }

  /**
   * Fetches the ActionInvocation and if there are parameters and the action configuration exists, it means that this
   * action can accept the parameters. This loops over the pattern from the ActionConfiguration and based on the
   * pattern, it pulls the values from the URI parameters List in the ActionInvocation and sets them into the action
   * class properties or fields using the ExpressionEvaluator.
   *
   * @param workflowChain Called after processing.
   * @throws IOException If the WorkflowChain throws this exception.
   * @throws ServletException If the WorkflowChain throws this exception.
   */
  public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    LinkedList<String> parameters = new LinkedList<String>(actionInvocation.uriParameters);
    ActionConfiguration actionConfiguration = actionInvocation.configuration;

    if (!parameters.isEmpty() && actionConfiguration != null) {
      Map<String, String[]> uriParameters = new HashMap<>();
      String[] patternParts = actionConfiguration.patternParts;
      for (String patternPart : patternParts) {
        // If there are no more URI parameters, we are finished
        if (parameters.isEmpty()) {
          break;
        }

        if (patternPart.startsWith("{*")) {
          // Stuff the rest of the list into the property
          String name = patternPart.substring(2, patternPart.length() - 1);
          uriParameters.put(name, unescape(parameters).toArray(new String[parameters.size()]));
        } else if (patternPart.startsWith("{")) {
          // Stuff this parameter into the property
          String name = patternPart.substring(1, patternPart.length() - 1);
          String value = unescape(parameters.removeFirst());
          uriParameters.put(name, new String[]{value});
        } else {
          // Pop the value off
          parameters.removeFirst();
        }
      }

      HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) request;
      HttpServletRequest old = (HttpServletRequest) wrapper.getRequest();
      wrapper.setRequest(new ParameterHttpServletRequest(old, uriParameters));
    }

    workflowChain.continueWorkflow();
  }

  /**
   * Unescape all parameters provided in the list.
   *
   * @param parameters The list of parameters to unescape.
   * @return The list of unescaped parameters.
   */
  private List<String> unescape(List<String> parameters) {
    for (int i = 0; i < parameters.size(); i++) {
      parameters.set(i, unescape(parameters.get(i)));
    }
    return parameters;
  }

  /**
   * Return the unescaped parameter.
   *
   * @param string The parameter to unescape
   * @return
   */
  private String unescape(String string) {
    return string.replace("%20", " ");
  }
}
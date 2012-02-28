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
package org.primeframework.mvc.parameter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.servlet.WorkflowChain;

import com.google.inject.Inject;

/**
 * <p> This class implements the URIParameterWorkflow using patterns derived from the WADL specification. </p> <p/> <p>
 * The base URI for the action is fixed based on the package and class name. However, everything after the base can be
 * set into properties or fields of the action class using the WADL pattern here. The pattern is like this: </p> <p/>
 * <pre>
 * {id}
 * </pre>
 * <p/> <p> If the classes base URI is /admin/user/edit, the full specification for the URI that action can handle would
 * be: </p> <p/>
 * <pre>
 * /admin/user/edit/{id}
 * </pre>
 * <p/> <p> If the URI is <strong>/admin/user/edit/42</strong>, the value of 42 would be set into the action's
 * <strong>id</strong> property or field. </p> <p/> <p> The difference between the standard WADL specification pattern
 * and the JCatapult pattern is that JCatapult allows you to capture all of the URI parameters or just what is left over
 * after everything else has been handled using a special notation. This notation is: </p> <p/>
 * <pre>
 * {id}/{*theRest}
 * </pre>
 * <p/> <p> If the URI is <strong>/admin/user/edit/42/foo/bar</strong>, the value of 42 would be set into the action's
 * <strong>id</strong> property or field and a List of Strings will be set into the property or field named
 * <strong>theRest</strong>. The property that handles the all or the remaining parameters must be of type
 * Collection&lt;String> or List&lt;String>. </p>
 *
 * @author Brian Pontarelli
 */
public class DefaultURIParameterWorkflow implements URIParameterWorkflow {
  private final HttpServletRequest request;
  private final ActionInvocationStore actionInvocationStore;

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
   * @throws IOException      If the WorkflowChain throws this exception.
   * @throws ServletException If the WorkflowChain throws this exception.
   */
  @SuppressWarnings("unchecked")
  public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
    ActionInvocation invocation = actionInvocationStore.getCurrent();
    LinkedList<String> parameters = new LinkedList<String>(invocation.uriParameters());
    ActionConfiguration actionConfiguration = invocation.configuration();

    if (!parameters.isEmpty() && actionConfiguration != null) {
      Map<String, String[]> uriParameters = new HashMap<String, String[]>();
      String pattern = actionConfiguration.uriParameterPattern();
      String[] patternParts = pattern.split("/");
      for (String patternPart : patternParts) {
        // If there are no more URI parameters, we are finished
        if (parameters.isEmpty()) {
          break;
        }

        if (patternPart.startsWith("{*")) {
          // Stuff the rest of the list into the property
          String name = patternPart.substring(2, patternPart.length() - 1);
          uriParameters.put(name, parameters.toArray(new String[parameters.size()]));
        } else if (patternPart.startsWith("{")) {
          // Stuff this parameter into the property
          String name = patternPart.substring(1, patternPart.length() - 1);
          String value = parameters.removeFirst();
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
}
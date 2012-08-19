/*
 * Copyright (c) 2001-2010, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.annotation.Annotation;

import org.apache.commons.lang3.tuple.Pair;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.ForwardResult.ForwardImpl;
import org.primeframework.mvc.action.result.RedirectResult.RedirectImpl;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * Handles invoking the result.
 *
 * @author Brian Pontarelli
 */
public class DefaultResultInvocationWorkflow implements ResultInvocationWorkflow {
  private final ActionInvocationStore actionInvocationStore;
  private final ResultStore resultStore;
  private final ResourceLocator resourceLocator;
  private final ResultFactory factory;

  @Inject
  public DefaultResultInvocationWorkflow(ActionInvocationStore actionInvocationStore, ResultStore resultStore,
                                         ResourceLocator resourceLocator, ResultFactory factory) {
    this.actionInvocationStore = actionInvocationStore;
    this.resultStore = resultStore;
    this.resourceLocator = resourceLocator;
    this.factory = factory;
  }

  /**
   * Performs the action invocation using this process.
   * <p/>
   * <h3>Action-less request</h3>
   * <p/>
   * <ul> <li>Lookup an action-less result invocation</li> <li>If it doesn't exist, continue down the chain</li> <li>If
   * it does exist, call the ResultRegistry to find the Result</li> <li>Invoke the Result</li> </ul>
   * <p/>
   * <h3>Action request</h3>
   * <p/>
   * <ul> <li>Lookup an result invocation using the action invocation, action URI and result code from the action</li>
   * <li>If it doesn't exist, error out</li> <li>If it does exist, call the ResultRegistry to find the Result</li>
   * <li>Invoke the Result</li> </ul>
   *
   * @param chain The chain.
   * @throws IOException      If the chain throws an IOException.
   * @throws ServletException If the chain throws a ServletException or if the result can't be found.
   */
  @SuppressWarnings("unchecked")
  public void perform(WorkflowChain chain) throws IOException, ServletException {
    try {
      ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
      if (actionInvocation.executeResult) {
        Annotation annotation;
        if (actionInvocation.action == null) {
          Pair<Annotation, Class<?>> p = defaultResult(actionInvocation);
          if (p == null) {
            chain.continueWorkflow();
            return;
          }

          annotation = p.getLeft();
        } else {
          String resultCode = resultStore.get();
          annotation  = actionInvocation.configuration.resultConfigurations.get(resultCode);
          if (annotation == null) {
            String uri = resourceLocator.locate(ForwardResult.DIR);
            if (uri == null) {
              throw new PrimeException("Missing result for action class [" + actionInvocation.configuration.actionClass +
                "] URI [" + actionInvocation.actionURI + "] and result code [" + resultCode + "]");
            }

            annotation = new ForwardImpl(uri, "success");
          }
        }

        Result result = factory.build(annotation.annotationType());
        result.execute(annotation);
      }
    } finally {
      resultStore.clear();
    }
  }

  /**
   * Locates the default Forward or Redirect result for an action invocation and result code from an action.
   * <p/>
   * Checks for results using this search order:
   * <p/>
   * <ol> <li>/WEB-INF/templates/&lt;uri>-&lt;resultCode>.jsp</li> <li>/WEB-INF/templates/&lt;uri>-&lt;resultCode>.ftl</li>
   * <li>/WEB-INF/templates/&lt;uri>.jsp</li> <li>/WEB-INF/templates/&lt;uri>.ftl</li>
   * <li>/WEB-INF/templates/&lt;uri>/index.jsp</li> <li>/WEB-INF/templates/&lt;uri>/index.ftl</li> </ol>
   * <p/>
   * If nothing is found this bombs out.
   *
   * @param invocation The action invocation.
   * @return The Forward and never null.
   * @throws RuntimeException If the default forward could not be found.
   */
  protected Pair<Annotation, Class<?>> defaultResult(ActionInvocation invocation) {
    String uri = resourceLocator.locate(ForwardResult.DIR);
    if (uri != null) {
      return Pair.<Annotation, Class<?>>of(new ForwardImpl(uri, "success"), ForwardResult.class);
    }

    // If the URI ends with a / and the forward result doesn't exist, redirecting won't help.
    String actionURI = invocation.actionURI;
    if (actionURI.endsWith("/")) {
      return null;
    }

    uri = resourceLocator.locateIndex(ForwardResult.DIR);
    if (uri != null) {
      return Pair.<Annotation, Class<?>>of(new RedirectImpl(uri, "success", true, false), RedirectResult.class);
    }

    return null;
  }
}

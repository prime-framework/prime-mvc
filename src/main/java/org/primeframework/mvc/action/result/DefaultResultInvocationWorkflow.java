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
package org.primeframework.mvc.action.result;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.annotation.Annotation;

import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.ForwardResult.ForwardImpl;
import org.primeframework.mvc.action.result.RedirectResult.RedirectImpl;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles invoking the result.
 *
 * @author Brian Pontarelli
 */
public class DefaultResultInvocationWorkflow implements ResultInvocationWorkflow {
  private static final Logger logger = LoggerFactory.getLogger(DefaultResultInvocationWorkflow.class);

  private final ActionInvocationStore actionInvocationStore;

  private final MVCConfiguration configuration;

  private final ResultFactory factory;

  private final ResourceLocator resourceLocator;

  private final ResultStore resultStore;

  @Inject
  public DefaultResultInvocationWorkflow(ActionInvocationStore actionInvocationStore, MVCConfiguration configuration,
                                         ResultStore resultStore, ResourceLocator resourceLocator,
                                         ResultFactory factory) {
    this.actionInvocationStore = actionInvocationStore;
    this.configuration = configuration;
    this.resultStore = resultStore;
    this.resourceLocator = resourceLocator;
    this.factory = factory;
  }

  /**
   * Performs the action invocation using this process.
   * <p>
   * <h3>Action-less request</h3>
   * <p>
   * <ul> <li>Lookup an action-less result invocation</li> <li>If it doesn't exist, continue down the chain</li> <li>If
   * it does exist, call the ResultRegistry to find the Result</li> <li>Invoke the Result</li> </ul>
   * <p>
   * <h3>Action request</h3>
   * <p>
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
        Annotation annotation = null;
        String resultCode = "success";
        if (actionInvocation.action != null) {
          resultCode = resultStore.get();
          annotation = actionInvocation.configuration.resultConfigurations.get(resultCode);
        }

        if (annotation == null) {
          annotation = new ForwardImpl("", resultCode);
        }

        long start = System.currentTimeMillis();
        Result result = factory.build(annotation.annotationType());
        boolean handled = result.execute(annotation);

        if (logger.isDebugEnabled()) {
          logger.debug("Result execute took [{}]", (System.currentTimeMillis() - start));
        }

        if (!handled) {
          handleContinueOrRedirect(actionInvocation, chain);
        }
      }
    } finally {
      resultStore.clear();
    }
  }

  @SuppressWarnings("unchecked")
  private void handleContinueOrRedirect(ActionInvocation actionInvocation, WorkflowChain chain)
      throws IOException, ServletException {
    if (actionInvocation.actionURI.endsWith("/")) {
      chain.continueWorkflow();
    } else {
      String uri = resourceLocator.locateIndex(configuration.resourceDirectory() + "/templates");
      if (uri == null) {
        chain.continueWorkflow();
      } else {
        Annotation annotation = new RedirectImpl(uri, "success", true, false);
        Result redirectResult = factory.build(annotation.annotationType());
        redirectResult.execute(annotation);
      }
    }
  }
}

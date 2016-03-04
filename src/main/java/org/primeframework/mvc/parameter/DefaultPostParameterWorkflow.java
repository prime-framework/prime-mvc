/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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
import java.io.IOException;

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.util.ReflectionUtils;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * This class handles the invocation of all action methods annotated with {@link PostParameterMethod}
 *
 * @author Daniel DeGroff
 */
public class DefaultPostParameterWorkflow implements PostParameterWorkflow {

  private final ActionInvocationStore actionInvocationStore;

  @Inject
  public DefaultPostParameterWorkflow(ActionInvocationStore actionInvocationStore) {
    this.actionInvocationStore = actionInvocationStore;
  }

  @Override
  public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();

    if (actionInvocation.action != null) {
      ActionConfiguration actionConfiguration = actionInvocation.configuration;
      if (actionConfiguration.postParameterMethods.size() > 0) {
        Object action = actionInvocation.action;
        ReflectionUtils.invokeAll(action, actionConfiguration.postParameterMethods);
      }
    }

    workflowChain.continueWorkflow();
  }
}

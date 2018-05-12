/*
 * Copyright (c) 2018, Inversoft Inc., All Rights Reserved
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

import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.util.ReflectionUtils;

/**
 * @author Daniel DeGroff
 */
public class DefaultPostParameterHandler implements PostParameterHandler {
  @Override
  public void handle(ActionInvocation actionInvocation) {
    if (actionInvocation.action != null) {
      ActionConfiguration actionConfiguration = actionInvocation.configuration;
      if (actionConfiguration.postParameterMethods.size() > 0) {
        Object action = actionInvocation.action;
        ReflectionUtils.invokeAll(action, actionConfiguration.postParameterMethods);
      }
    }
  }
}

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
package org.primeframework.mvc.action;

import java.util.ArrayList;
import java.util.Collection;

import org.primeframework.mvc.action.config.ActionConfiguration;

/**
 * This interface defines the information about an action invocation.
 *
 * @author Brian Pontarelli
 */
public class ActionInvocation {
  public final Object action;
  public final ExecuteMethodConfiguration method;
  public final String actionURI;
  public final String extension;
  public final Collection<String> uriParameters;
  public final ActionConfiguration configuration;
  public final boolean executeResult;

  public ActionInvocation(Object action, ExecuteMethodConfiguration method, String uri, String extension, ActionConfiguration configuration) {
    this.action = action;
    this.method = method;
    this.actionURI = uri;
    this.extension = extension;
    this.uriParameters = new ArrayList<String>();
    this.configuration = configuration;
    this.executeResult = true;
  }

  public ActionInvocation(Object action, ExecuteMethodConfiguration method, String uri, String extension, Collection<String> uriParameters,
                          ActionConfiguration configuration, boolean executeResult) {
    this.action = action;
    this.method = method;
    this.actionURI = uri;
    this.extension = extension;
    this.uriParameters = uriParameters;
    this.configuration = configuration;
    this.executeResult = executeResult;
  }

  public String uri() {
    return actionURI + (extension != null ? "." + extension : "");
  }
}

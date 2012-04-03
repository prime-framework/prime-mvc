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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.primeframework.mvc.action.config.ActionConfiguration;

/**
 * This class is the default action invocation implementation. It provides a simple immutable Struct for containing
 * the values of the invocation.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionInvocation implements ActionInvocation {
  private final Object action;
  private final Method method;
  private final String uri;
  private final String extension;
  private final Collection<String> uriParameters;
  private final ActionConfiguration configuration;
  private final boolean executeResult;

  public DefaultActionInvocation(Object action, Method method, String uri, String extension, ActionConfiguration configuration) {
    this.action = action;
    this.method = method;
    this.uri = uri;
    this.extension = extension;
    this.uriParameters = new ArrayList<String>();
    this.configuration = configuration;
    this.executeResult = true;
  }

  public DefaultActionInvocation(Object action, Method method, String uri, String extension, Collection<String> uriParameters,
                                 ActionConfiguration configuration, boolean executeResult) {
    this.action = action;
    this.method = method;
    this.uri = uri;
    this.extension = extension;
    this.uriParameters = uriParameters;
    this.configuration = configuration;
    this.executeResult = executeResult;
  }

  @Override
  public Object action() {
    return action;
  }

  @Override
  public Method method() {
    return method;
  }

  @Override
  public String actionURI() {
    return uri;
  }

  @Override
  public String extension() {
    return extension;
  }

  @Override
  public Collection<String> uriParameters() {
    return Collections.unmodifiableCollection(uriParameters);
  }

  @Override
  public ActionConfiguration configuration() {
    return configuration;
  }

  @Override
  public boolean executeResult() {
    return executeResult;
  }

  @Override
  public String uri() {
    return uri + (extension != null ? "." + extension : "");
  }
}
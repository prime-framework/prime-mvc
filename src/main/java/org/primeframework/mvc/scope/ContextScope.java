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
package org.primeframework.mvc.scope;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPContext;
import org.primeframework.mvc.scope.annotation.Context;

/**
 * This is the request scope which fetches and stores values in the Context
 *
 * @author Brian Pontarelli
 */
public class ContextScope implements Scope<Context> {
  private final HTTPContext context;

  @Inject
  public ContextScope(HTTPContext context) {
    this.context = context;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object get(String fieldName, Class<?> type, Context scope) {
    String key = scope.value().equals("##field-name##") ? fieldName : scope.value();
    return context.getAttribute(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void set(String fieldName, Object value, Context scope) {
    String key = scope.value().equals("##field-name##") ? fieldName : scope.value();
    context.setAttribute(key, value);
  }
}
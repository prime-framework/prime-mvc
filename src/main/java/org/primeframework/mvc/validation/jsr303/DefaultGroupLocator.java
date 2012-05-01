/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.validation.jsr303;

import javax.validation.groups.Default;

import org.apache.commons.lang3.ArrayUtils;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.validation.jsr303.group.Create;
import org.primeframework.mvc.validation.jsr303.group.Delete;
import org.primeframework.mvc.validation.jsr303.group.Read;
import org.primeframework.mvc.validation.jsr303.group.Update;

import com.google.inject.Inject;
import static org.primeframework.mvc.servlet.HTTPMethod.*;

/**
 * Uses the HTTP request method or {@link Validation} annotation to determine the groups. The HTTP method mapping is:
 * <p/>
 * <ul>
 *   <li>GET - Read</li>
 *   <li>POST - Create</li>
 *   <li>PUT - Update</li>
 *   <li>DELETE- Delete</li>
 * </ul>
 * @author Brian Pontarelli
 */
public class DefaultGroupLocator implements GroupLocator {
  private final ActionInvocationStore store;
  private final HTTPMethod method;

  @Inject
  public DefaultGroupLocator(ActionInvocationStore store, HTTPMethod method) {
    this.store = store;
    this.method = method;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<?>[] groups() {
    // See if the execute method is annotated with the groups
    ActionInvocation actionInvocation = store.getCurrent();
    if (actionInvocation.method != null && actionInvocation.method.validation != null) {
      return actionInvocation.method.validation.groups();
    }

    if (method == GET) {
      return ArrayUtils.toArray(Read.class, Default.class);
    } else if (method == POST) {
      return ArrayUtils.toArray(Create.class, Default.class);
    } else if (method == PUT) {
      return ArrayUtils.toArray(Update.class, Default.class);
    } else if (method == DELETE) {
      return ArrayUtils.toArray(Delete.class, Default.class);
    }

    throw new PrimeException("Invalid HTTP method for validation [" + method + "]");
  }
}

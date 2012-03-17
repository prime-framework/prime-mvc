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

import javax.servlet.http.HttpServletRequest;
import javax.validation.groups.Default;

import org.apache.commons.lang3.ArrayUtils;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.validation.jsr303.group.Create;
import org.primeframework.mvc.validation.jsr303.group.Delete;
import org.primeframework.mvc.validation.jsr303.group.Read;
import org.primeframework.mvc.validation.jsr303.group.Update;

import com.google.inject.Inject;

/**
 * Uses the HTTP request method to determine the groups. The mapping is:
 * <p/>
 * <ul>
 *   <li>GET - Read</li>
 *   <li>POST - Create</li>
 *   <li>PUT - Update</li>
 *   <li>DELETE- Delete</li>
 * </ul>
 * @author Brian Pontarelli
 */
public class RequestMethodGroupLocator implements GroupLocator {
  private final HttpServletRequest request;

  @Inject
  public RequestMethodGroupLocator(HttpServletRequest request) {
    this.request = request;
  }

  @Override
  public Class<?>[] groups() {
    String method = request.getMethod();
    if (method.equals("GET")) {
      return ArrayUtils.toArray(Read.class, Default.class);
    } else if (method.equals("POST")) {
      return ArrayUtils.toArray(Create.class, Default.class);
    } else if (method.equals("PUT")) {
      return ArrayUtils.toArray(Update.class, Default.class);
    } else if (method.equals("DELETE")) {
      return ArrayUtils.toArray(Delete.class, Default.class);
    }
    
    throw new ErrorException("error", "Invalid HTTP method for validation [" + method + "]");
  }
}

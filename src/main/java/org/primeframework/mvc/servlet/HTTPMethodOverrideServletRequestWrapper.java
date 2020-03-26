/*
 * Copyright (c) 2020, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A request wrapper that allows prime-mvc to support the <code>X-HTTP-Method-Override</code> and
 * <code>X-Method-Override</code> HTTP headers.
 *
 * @author Daniel DeGroff
 */
public class HTTPMethodOverrideServletRequestWrapper extends HttpServletRequestWrapper {
  /**
   * Constructs a request object wrapping the given request.
   *
   * @param request the HTTP servlet request
   * @throws IllegalArgumentException if the request is null
   */
  public HTTPMethodOverrideServletRequestWrapper(HttpServletRequest request) {
    super(request);
  }

  @Override
  public String getMethod() {
    // Support for HTTP Method Override
    String methodOverride = getHeader("X-HTTP-Method-Override");
    if (methodOverride == null) {
      methodOverride = getHeader("X-Method-Override");
    }

    return methodOverride == null ? super.getMethod() : methodOverride;
  }
}

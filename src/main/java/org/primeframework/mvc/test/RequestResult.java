/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.test;

import com.google.inject.Injector;
import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletResponse;

/**
 * Result of a request to the {@link org.primeframework.mvc.test.RequestSimulator}.
 *
 * @author Brian Pontarelli
 */
public class RequestResult {
  public final MockHttpServletRequest request;
  public final MockHttpServletResponse response;
  public final Injector injector;
  public final String body;

  public RequestResult(MockHttpServletRequest request, MockHttpServletResponse response, Injector injector) {
    this.request = request;
    this.response = response;
    this.injector = injector;
    this.body = response.getStream().toString();
  }

  /**
   * Retrieves the instance of the given type from the Guice Injector.
   *
   * @param type The type.
   * @param <T>  The type.
   * @return The instance.
   */
  public <T> T get(Class<T> type) {
    return injector.getInstance(type);
  }
}

/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.http;

import org.primeframework.mvc.PrimeException;

/**
 * This class is a static storage location for the HTTP objects so that they can be later injected via providers.
 *
 * @author Brian Pontarelli
 */
public final class HTTPObjectsHolder {
  private final static ThreadLocal<HTTPRequest> Requests = new ThreadLocal<>();

  private final static ThreadLocal<HTTPResponse> Responses = new ThreadLocal<>();

  private HTTPObjectsHolder() {
  }

  /**
   * Removes the HTTP request for the current thread.
   */
  public static void clearRequest() {
    Requests.remove();
  }

  /**
   * Removes the HTTP response for the current thread.
   */
  public static void clearResponse() {
    Responses.remove();
  }

  /**
   * Gets the HTTP request for the current thread (if any).
   *
   * @return The HTTP request for the current thread.
   */
  public static HTTPRequest getRequest() {
    return Requests.get();
  }

  /**
   * Sets the HTTP request for the current thread.
   *
   * @param request The HTTP request for the current thread.
   */
  public static void setRequest(HTTPRequest request) {
    if (Requests.get() != null) {
      throw new PrimeException("Request is already set into the HTTPObjectsHolder");
    }

    Requests.set(request);
  }

  /**
   * Gets the HTTP response for the current thread (if any).
   *
   * @return The HTTP response for the current thread.
   */
  public static HTTPResponse getResponse() {
    return Responses.get();
  }

  /**
   * Sets the HTTP response for the current thread.
   *
   * @param response The HTTP response for the current thread.
   */
  public static void setResponse(HTTPResponse response) {
    if (Responses.get() != null) {
      throw new PrimeException("Response is already set into the HTTPObjectsHolder");
    }

    Responses.set(response);
  }
}
/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.primeframework.mvc.PrimeException;

/**
 * This class is a static storage location for the servlet objects so that they can be later injected via providers.
 * <p/>
 * This class is setup as long as the PrimeFilter is placed into the web.xml file and is the first filter in the chain.
 *
 * @author Brian Pontarelli
 */
public final class ServletObjectsHolder {

  private static ServletContext servletContext;

  private final static ThreadLocal<HttpServletRequestWrapper> request = new ThreadLocal<>();
  private final static ThreadLocal<HttpServletResponse> response = new ThreadLocal<>();

  /**
   * Gets the servlet context.
   *
   * @return The servlet context.
   */
  public static ServletContext getServletContext() {
    return servletContext;
  }

  /**
   * Sets the servlet context.
   *
   * @param servletContext The servlet context.
   */
  public static void setServletContext(ServletContext servletContext) {
    ServletObjectsHolder.servletContext = servletContext;
  }

  /**
   * Gets the servlet request for the current thread (if any).
   *
   * @return The servlet request for the current thread.
   */
  public static HttpServletRequestWrapper getServletRequest() {
    return request.get();
  }

  /**
   * Sets the servlet request for the current thread.
   *
   * @param servletRequest The servlet request for the current thread.
   */
  public static void setServletRequest(HttpServletRequestWrapper servletRequest) {
    if (request.get() != null) {
      throw new PrimeException("Request is already set into the ServletObjectsHolder");
    }

    request.set(servletRequest);
  }

  /**
   * Removes the servlet request for the current thread.
   */
  public static void clearServletRequest() {
    request.remove();
  }
  /**
   * Gets the servlet response for the current thread (if any).
   *
   * @return The servlet response for the current thread.
   */
  public static HttpServletResponse getServletResponse() {
    return response.get();
  }

  /**
   * Sets the servlet response for the current thread.
   *
   * @param servletResponse The servlet response for the current thread.
   */
  public static void setServletResponse(HttpServletResponse servletResponse) {
    if (response.get() != null) {
      throw new PrimeException("Response is already set into the ServletObjectsHolder");
    }

    response.set(servletResponse);
  }
  /**
   * Removes the servlet response for the current thread.
   */
  public static void clearServletResponse() {
    response.remove();
  }
}
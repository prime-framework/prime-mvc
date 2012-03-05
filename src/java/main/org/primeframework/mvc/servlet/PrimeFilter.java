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
package org.primeframework.mvc.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.primeframework.mvc.config.PrimeMVCConfiguration;
import org.primeframework.mvc.workflow.MVCWorkflow;

import com.google.inject.Injector;

/**
 * This is the main Servlet filter for the Prime MVC. This will setup the {@link ServletObjectsHolder} so that the
 * request, response and session can be injected.
 * <p/>
 * Next, this filter will use the {@link FilterWorkflowChain} in conjunction with the {@link MVCWorkflow} to invoke Prime.
 *
 * @author Brian Pontarelli
 */
public class PrimeFilter implements Filter {
  private static final Logger logger = Logger.getLogger(PrimeFilter.class.getName());
  private Injector injector;

  /**
   * Does nothing.
   *
   * @param filterConfig Not used.
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    this.injector = (Injector) filterConfig.getServletContext().getAttribute(PrimeServletContextListener.GUICE_INJECTOR_KEY);
    if (this.injector == null) {
      throw new ServletException("Guice was not initialized. You must define a ServletContext listener to setup Guice or " +
        "use the PrimeServletContextListener");
    }
  }

  /**
   * Invokes the Workflow chain.
   *
   * @param request  Passed down chain.
   * @param response Passed down chain.
   * @param chain    The chain.
   * @throws IOException      If the chain throws an exception.
   * @throws ServletException If the chain throws an exception.
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
  throws IOException, ServletException {
    long start = System.currentTimeMillis();
    PrimeMVCConfiguration configuration = injector.getInstance(PrimeMVCConfiguration.class);
    ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper((HttpServletRequest) request));
    ServletObjectsHolder.setServletResponse((HttpServletResponse) response);

    try {
      FilterWorkflowChain workflowChain = new FilterWorkflowChain(chain, injector.getInstance(MVCWorkflow.class));
      workflowChain.continueWorkflow();
    } catch (RuntimeException re) {
      boolean propogate = configuration.propagateRuntimeExceptions();
      if (propogate) {
        throw re;
      } else {
        ((HttpServletResponse) response).setStatus(500);
      }
    } finally {
      if (logger.isLoggable(Level.FINEST)) {
        long end = System.currentTimeMillis();
        logger.finest("Processing time in PrimeFilter [" + (end - start) + "]");
      }

      ServletObjectsHolder.clearServletRequest();
      ServletObjectsHolder.clearServletResponse();

      // Handle any extra logging for handling issues that might have occurred.
      Throwable t = (Throwable) request.getAttribute("javax.servlet.error.exception");
      if (t != null) {
        logger.log(Level.FINE, "Exception occurred during the request", t);
      }
      t = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
      if (t != null) {
        logger.log(Level.FINE, "Exception occurred during the request", t);
      }
    }
  }

  /**
   * Closes the Workflow instances
   */
  public void destroy() {
  }
}
/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Injector;
import org.primeframework.mvc.workflow.MVCWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main Servlet filter for the Prime MVC. This will setup the {@link ServletObjectsHolder} so that the
 * request, response and session can be injected.
 * <p>
 * Next, this filter will use the {@link FilterWorkflowChain} in conjunction with the {@link MVCWorkflow} to invoke
 * Prime.
 *
 * @author Brian Pontarelli
 */
public class PrimeFilter implements Filter {
  private static final Logger logger = LoggerFactory.getLogger(PrimeFilter.class);

  private ServletContext context;

  /**
   * Closes the Workflow instances
   */
  public void destroy() {
  }

  /**
   * Invokes the Workflow chain.
   *
   * @param request  Passed down chain.
   * @param response Passed down chain.
   * @param chain    The chain.
   * @throws ServletException If the chain throws an exception.
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException {
    long start = System.currentTimeMillis();

    Injector injector = (Injector) context.getAttribute(PrimeServletContextListener.GUICE_INJECTOR_KEY);
    if (injector == null) {
      throw new ServletException("Guice was not initialized. You must define a ServletContext listener to setup Guice or " +
          "use the PrimeServletContextListener");
    }

    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    ServletObjectsHolder.setServletRequest(new HTTPMethodOverrideServletRequestWrapper(httpServletRequest));
    ServletObjectsHolder.setServletResponse(httpServletResponse);

    boolean loggedError = false;
    try {
      FilterWorkflowChain workflowChain = new FilterWorkflowChain(chain, httpServletRequest, httpServletResponse, injector.getInstance(MVCWorkflow.class));
      workflowChain.continueWorkflow();
    } catch (Throwable t) {
      ((HttpServletResponse) response).setStatus(500);
      logger.error("Error encountered", t);
      loggedError = true;
    } finally {
      if (logger.isDebugEnabled()) {
        long end = System.currentTimeMillis();
        logger.debug("Processing time in PrimeFilter [" + (end - start) + "]");
      }

      ServletObjectsHolder.clearServletRequest();
      ServletObjectsHolder.clearServletResponse();

      // Handle any extra logging for handling issues that might have occurred.
      if (!loggedError) {
        Throwable t = (Throwable) request.getAttribute("javax.servlet.error.exception");
        if (t != null) {
          logger.debug("Exception occurred during the request", t);
        }
        t = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
        if (t != null) {
          logger.debug("Exception occurred during the request", t);
        }
      }
    }
  }

  /**
   * Does nothing.
   *
   * @param filterConfig Not used.
   */
  public void init(FilterConfig filterConfig) {
    this.context = filterConfig.getServletContext();
  }
}
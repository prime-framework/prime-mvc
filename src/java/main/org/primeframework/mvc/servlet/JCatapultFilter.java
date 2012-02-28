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
import org.primeframework.mvc.guice.GuiceContainer;

import com.google.inject.Inject;

/**
 * This is the main Servlet filter for the JCatapult framework. This will setup the {@link ServletObjectsHolder}, the
 * JPA context (if the project is using JPA) and any other JCatapult configuration that is needed.
 * <p/>
 * This filter can optionally perform a number of other tasks as well. First, it can create a Guice injector and place
 * it into the ServletContext under the key <strong>guiceInjector</strong>. This can be turned on using the JCatapult
 * configuration file and setting the property <strong>jcatapult.filter.guice.init</strong> to true. In order to specify
 * the Guice modules to use, the property <strong>jcatapult.filter.guice.modules</strong> should contain a comma
 * separated list of modules.
 * <p/>
 * This filter can also initialize and WILL initialize JPA on startup. This will create an EntityManagerFactory using
 * either a persistence unit named <strong>punit</strong> or using the value of the
 * <strong>jcatapult.filter.jpa.unit</strong> JCatapult configuration property. You can turn off JPA setup using the
 * <strong>jcatapult.filter.jpa.enabled</strong> boolean property. If this parameter is set to <strong>false</strong>,
 * JPA will not be setup.
 *
 * @author Brian Pontarelli
 */
public class JCatapultFilter implements Filter {
  private static final Logger logger = Logger.getLogger(JCatapultFilter.class.getName());
  public static final String ORIGINAL_REQUEST_URI = "ORIGINAL_REQUEST_URI";
  private PrimeMVCConfiguration configuration;

  /**
   * Sets the configuration.
   *
   * @param configuration The configuration.
   */
  @Inject
  public void setConfiguration(PrimeMVCConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * This fetches the top level workflows for JCatapult using the {@link WorkflowResolver} that is fetched from the
   * Guice injector, which is retrieved from {@link GuiceContainer}. This requires that the {@link
   * JCatapultServletContextListener} is setup in web.xml.
   *
   * @param filterConfig The filter config to get the init params from.
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    GuiceContainer.getInjector().injectMembers(this);
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
    if (request.getAttribute(ORIGINAL_REQUEST_URI) == null) {
      request.setAttribute(ORIGINAL_REQUEST_URI, ServletTools.getRequestURI((HttpServletRequest) request));
    }

    ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper((HttpServletRequest) request));
    ServletObjectsHolder.setServletResponse((HttpServletResponse) response);
    try {
      WorkflowChain workflowChain = GuiceContainer.getInjector().getInstance(WorkflowChain.class);
      workflowChain.start(chain);
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
        logger.finest("Processing time in JCatapultFilter [" + (end - start) + "]");
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
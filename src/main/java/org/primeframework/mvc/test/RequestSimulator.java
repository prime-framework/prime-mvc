/*
 * Copyright (c) 2012-2019, Inversoft Inc., All Rights Reserved
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

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Enumeration;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.primeframework.mock.servlet.MockContainer;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mvc.guice.GuiceBootstrap;
import org.primeframework.mvc.servlet.PrimeFilter;
import org.primeframework.mvc.servlet.PrimeServletContextListener;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a method for testing a full invocation of Prime. This simulates the JEE web objects
 * (HttpServletRequest, etc.) and an invocation of the PrimeFilter. You can also simulate multiple invocations across a
 * single session by using the same instance of this class multiple times.
 *
 * @author Brian Pontarelli
 */
public class RequestSimulator {
  private final static Logger logger = LoggerFactory.getLogger(RequestSimulator.class);

  public final MockContainer container;

  public final PrimeFilter filter = new PrimeFilter();

  public Injector injector;

  // the response of the last request, deprecated, Do Not use, only added for CS
  public MockHttpServletResponse response;

  /**
   * Creates a new request simulator that can be used to simulate requests to a Prime application.
   *
   * @param container  The application container to use for this simulator.
   * @param mainModule The main module.
   * @throws ServletException If the initialization of the PrimeServletContextListener failed.
   */
  public RequestSimulator(final MockContainer container, Module mainModule) throws ServletException {
    ServletObjectsHolder.setServletContext(container.getContext());
    this.container = container;
    this.injector = GuiceBootstrap.initialize(mainModule);
    init();
  }

  /**
   * Creates a new request simulator that can be used to simulate requests to a Prime application.
   * <p>
   * This constructor can be used if you already have an injector built through a test framework.
   *
   * @param container The application container to use for this simulator.
   * @param injector  The Guice injector.
   * @throws ServletException If the initialization of the PrimeServletContextListener failed.
   */
  public RequestSimulator(final MockContainer container, Injector injector) throws ServletException {
    // This isn't necessary if you override the ServletModule. Leaving in case anyone wishes to use this.
    ServletObjectsHolder.setServletContext(container.getContext());
    this.container = container;
    this.injector = injector;
    init();
  }

  /**
   * Starts a test for the given URL. This returns a RequestBuilder that you can use to set the request up correctly for
   * the test and then execute the GET or POST.
   *
   * @param uri The URI to test.
   * @return The RequestBuilder.
   */
  public RequestBuilder test(String uri) {
    // cache the response of the last request
    RequestBuilder rb = new RequestBuilder(uri, container, filter, injector);
    response = rb.response;
    return rb;
  }

  private void init() throws ServletException {
    this.container.getContext().setAttribute(PrimeServletContextListener.GUICE_INJECTOR_KEY, this.injector);
    logger.debug("Built RequestSimulator with context webDir " + container.getContext().webDir.getAbsolutePath());
    this.filter.init(new FilterConfig() {
      @Override
      public String getFilterName() {
        return "prime";
      }

      @Override
      public String getInitParameter(String s) {
        return null;
      }

      @Override
      public Enumeration<String> getInitParameterNames() {
        return null;
      }

      @Override
      public ServletContext getServletContext() {
        return container.getContext();
      }
    });
  }
}

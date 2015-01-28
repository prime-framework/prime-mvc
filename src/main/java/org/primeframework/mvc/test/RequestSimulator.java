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
package org.primeframework.mvc.test;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mock.servlet.MockHttpSession;
import org.primeframework.mock.servlet.MockServletContext;
import org.primeframework.mvc.guice.GuiceBootstrap;
import org.primeframework.mvc.servlet.PrimeFilter;
import org.primeframework.mvc.servlet.PrimeServletContextListener;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Enumeration;

/**
 * This class provides a method for testing a full invocation of Prime. This simulates the JEE web objects
 * (HttpServletRequest, etc.) and an invocation of the PrimeFilter. You can also simulate multiple invocations across a
 * single session by using the same instance of this class multiple times.
 * <p/>
 * <h3>Examples</h3>
 * <p/>
 * <pre>
 * TODO: Add examples.
 * </pre>
 *
 * @author Brian Pontarelli
 */
public class RequestSimulator {
  private final static Logger logger = LoggerFactory.getLogger(RequestSimulator.class);
  public final PrimeFilter filter = new PrimeFilter();
  public MockServletContext context;
  public MockHttpSession session;
  public Injector injector;
  // the response of the last request, deprecated, Do Not use, only added for CS
  public MockHttpServletResponse response;

  /**
   * Creates a new request simulator that can be used to simulate requests to a Prime application.
   *
   * @param context    The servlet context to use for this simulator.
   * @param mainModule The main module.
   * @throws ServletException If the initialization of the PrimeServletContextListener failed.
   */
  public RequestSimulator(final MockServletContext context, Module mainModule) throws ServletException {
    logger.debug("Built RequestSimulator with context webDir " + context.webDir.getAbsolutePath());
    ServletObjectsHolder.setServletContext(context);
    this.context = context;
    this.session = new MockHttpSession(this.context);
    this.injector = GuiceBootstrap.initialize(mainModule);
    this.context.setAttribute(PrimeServletContextListener.GUICE_INJECTOR_KEY, this.injector);
    this.filter.init(new FilterConfig() {
      @Override
      public String getFilterName() {
        return "prime";
      }

      @Override
      public ServletContext getServletContext() {
        return context;
      }

      @Override
      public String getInitParameter(String s) {
        return null;
      }

      @Override
      public Enumeration<String> getInitParameterNames() {
        return null;
      }
    });
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
    RequestBuilder rb = new RequestBuilder(uri, session, filter, injector);
    response = rb.response;
    return rb;
  }

  /**
   * Retrieves the instance of the given type from the Guice Injector.
   * @deprecated   to allow cleanspeak to compile....
   * @param type The type.
   * @param <T>  The type.
   * @return The instance.
   */
  public <T> T get(Class<T> type) {
    return injector.getInstance(type);
  }

  /**
   * Resets the session by creating a new session that is empty.
   */
  public void resetSession() {
    session = new MockHttpSession(this.context);
  }
}

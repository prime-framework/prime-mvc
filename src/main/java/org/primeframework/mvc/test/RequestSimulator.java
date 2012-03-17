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

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;

import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mock.servlet.MockHttpSession;
import org.primeframework.mock.servlet.MockServletContext;
import org.primeframework.mvc.guice.GuiceBootstrap;
import org.primeframework.mvc.servlet.PrimeFilter;
import org.primeframework.mvc.servlet.PrimeServletContextListener;
import org.primeframework.mvc.servlet.ServletObjectsHolder;

import com.google.inject.Injector;
import com.google.inject.Module;

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
  public final PrimeFilter filter = new PrimeFilter();
  public MockHttpServletRequest request;
  public MockHttpServletResponse response;
  public MockServletContext context;
  public MockHttpSession session;
  public Injector injector;

  /**
   * Creates a new request simulator that can be used to simulate requests to a Prime application.
   *
   * @param context The servlet context to use for this simulator.
   * @param modules A list of modules that contain mocks and other guice injections for the test.
   * @throws ServletException If the initialization of the PrimeServletContextListener failed.
   */
  public RequestSimulator(final MockServletContext context, Module... modules) throws ServletException {
    this.context = context;
    this.session = new MockHttpSession(this.context);
    this.injector = GuiceBootstrap.initialize(modules);
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
      public Enumeration getInitParameterNames() {
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
    return new RequestBuilder(uri, this);
  }

  /**
   * Resets the session by creating a new session that is empty.
   */
  public void resetSession() {
    session = new MockHttpSession(this.context);
  }

  void run(RequestBuilder builder) throws IOException, ServletException {
    if (!builder.getModules().isEmpty()) {
      this.injector = GuiceBootstrap.initialize(builder.getModules().toArray(new Module[builder.getModules().size()]));
      this.context.setAttribute(PrimeServletContextListener.GUICE_INJECTOR_KEY, this.injector);
    }

    // Remove the web objects if this instance is being used across multiple invocations
    ServletObjectsHolder.clearServletRequest();
    ServletObjectsHolder.clearServletResponse();

    // Build the request and response for this pass
    this.request = builder.getRequest();
    this.response = makeResponse();

    filter.doFilter(this.request, this.response, new FilterChain() {
      @Override
      public void doFilter(ServletRequest request, ServletResponse response) {
        throw new UnsupportedOperationException("The RequestSimulator class doesn't support testing " +
          "URIs that don't map to Prime resources");
      }
    });

    // Add these back so that anything that needs them can be retrieved from the Injector after
    // the run has completed (i.e. MessageStore for the MVC and such)
    ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper(this.request));
    ServletObjectsHolder.setServletResponse(this.response);
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

  /**
   * @return Makes a HttpServletResponse as a nice mock. Sub-classes can override this
   */
  protected MockHttpServletResponse makeResponse() {
    return new MockHttpServletResponse();
  }
}

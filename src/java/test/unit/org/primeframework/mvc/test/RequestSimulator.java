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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;

import org.primeframework.mvc.guice.GuiceContainer;
import org.primeframework.mvc.servlet.JCatapultFilter;
import org.primeframework.mvc.servlet.JCatapultServletContextListener;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mock.servlet.MockHttpSession;
import org.primeframework.mock.servlet.MockServletContext;
import org.primeframework.mock.servlet.WebTestHelper;

import com.google.inject.Module;

/**
 * <p> This class provides a method for testing a full invocation of JCatapult. This simulates the JEE web objects
 * (HttpServletRequest, etc.) and an invocation of the JCatapultFilter. You can also simulate multiple invocations
 * across a single session by using the same instance of this class multiple times. </p>
 * <p/>
 * <h3>Examples</h3>
 * <pre>
 * TODO: Add examples.
 * </pre>
 *
 * @author Brian Pontarelli
 */
public class RequestSimulator {
  public static RequestSimulator singletonInstance;

  /**
   * Creates or retrieves the singleton instance of the tester. If the singleton instance hasn't been created yet, this
   * method uses the given modules (optional) to create the singleton instance and initialize it. If it has already been
   * created, this returns the instance.
   *
   * @param modules The modules to initialize the instance with.
   * @return The instance and never null.
   * @throws ServletException If the instance is created and the creation failed.
   */
  public static RequestSimulator getSingleton(Module... modules) throws ServletException {
    if (singletonInstance == null) {
      singletonInstance = new RequestSimulator();
      singletonInstance.initialize(modules);
    }

    return singletonInstance;
  }

  public MockHttpServletRequest request;
  public MockHttpServletResponse response;
  public MockServletContext context;
  public MockHttpSession session;
  public JCatapultFilter filter = new JCatapultFilter();
  public boolean contextInitialized;

  public RequestSimulator() {
    this.context = makeContext();
    this.session = makeSession(this.context);
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
    session = makeSession(context);
  }

  /**
   * This method pre-initialies the Guice injector and the JCatapult singletons and statics. You might consider using
   * this method if you want to run a bunch of tests against the same Guice injector. Sometimes, Guice takes a while to
   * start up and you just want to do it once. This method allows you to set everything up once and then run many tests
   * against the same injector.
   *
   * @param modules A list of modules that contain mocks and other guice injections for the test.
   * @throws ServletException If the initialization of the JCatapultServletContextListener failed.
   */
  public void initialize(Module... modules) throws ServletException {
    if (modules != null && modules.length > 0) {
      GuiceContainer.setGuiceModules(modules);
    }
    JCatapultServletContextListener listener = new JCatapultServletContextListener();
    listener.contextInitialized(new ServletContextEvent(this.context));
    filter.init(new FilterConfig() {
      @Override
      public String getFilterName() {
        return "jcatapult";
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

    contextInitialized = true;
  }

  void run(RequestBuilder builder) throws IOException, ServletException {
    if (!contextInitialized) {
      initialize(builder.getModules().toArray(new Module[builder.getModules().size()]));
    } else if (!builder.getModules().isEmpty()) {
      throw new AssertionError("You can't mock out on the second use of the RequestSimulator. All" +
        "the mocked interfaces must be setup during the first use of the RequestSimulator. " +
        "Reusing the same RequestSimulator ensures that the Guice injector is re-used, which " +
        "simulates multiple requests to the same webapp.");
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
          "URIs that don't map to JCatapult resources");
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
    return GuiceContainer.getInjector().getInstance(type);
  }

  /**
   * @return Makes a HttpServletResponse as a nice mock. Sub-classes can override this
   */
  protected MockHttpServletResponse makeResponse() {
    return new MockHttpServletResponse();
  }

  /**
   * @return Makes a MockServletContext
   */
  protected MockServletContext makeContext() {
    return WebTestHelper.makeContext();
  }

  /**
   * @param context The mock servlet context.
   * @return Makes a MockHttpSession.
   */
  protected MockHttpSession makeSession(MockServletContext context) {
    return WebTestHelper.makeSession(context);
  }
}

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

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletRequest.Method;
import org.primeframework.mock.servlet.MockServletInputStream;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * <p> This class is a builder that helps create a test HTTP request that is sent to the MVC. </p>
 *
 * @author Brian Pontarelli
 */
public class RequestBuilder {
  private final MockHttpServletRequest request;
  private final RequestSimulator test;
  private final List<Module> modules = new ArrayList<Module>();

  public RequestBuilder(String uri, RequestSimulator test) {
    request = new MockHttpServletRequest(uri, Locale.getDefault(), false, "UTF-8", test.session);
    this.test = test;
  }

  /**
   * Sets an HTTP request parameter. This can be called multiple times with the same name it it will create a list of
   * values for the HTTP parameter.
   *
   * @param name  The name of the parameter.
   * @param value The parameter value.
   * @return This.
   */
  public RequestBuilder withParameter(String name, String value) {
    request.setParameter(name, value);
    return this;
  }

  /**
   * Sets the locale that will be used.
   *
   * @param locale The locale.
   * @return This.
   */
  public RequestBuilder withLocale(Locale locale) {
    request.addLocale(locale);
    return this;
  }

  /**
   * Sets the body content.
   *
   * @param bytes The bytes.
   * @return This.
   */
  public RequestBuilder withBody(byte[] bytes) {
    request.setInputStream(new MockServletInputStream(bytes));
    return this;
  }

  /**
   * Sets the content type.
   *
   * @param contentType The content type.
   * @return This.
   */
  public RequestBuilder withContentType(String contentType) {
    request.setContentType(contentType);
    return this;
  }

  /**
   * Adds a header to the request.
   *
   * @param name  The name of the header.
   * @param value The value of the header.
   * @return This.
   */
  public RequestBuilder withHeader(String name, String value) {
    request.addHeader(name, value);
    return this;
  }

  /**
   * Sets the context path.
   *
   * @param contextPath The context path.
   * @return This.
   */
  public RequestBuilder withContextPath(String contextPath) {
    request.setContextPath(contextPath);
    return this;
  }

  /**
   * Sets the encoding.
   *
   * @param encoding The encoding.
   * @return This.
   */
  public RequestBuilder withEncoding(String encoding) {
    request.setEncoding(encoding);
    return this;
  }

  /**
   * Adds a file.
   *
   * @param name        The name of the file form field.
   * @param file        The file.
   * @param contentType The content type.
   * @return This.
   */
  public RequestBuilder withFile(String name, File file, String contentType) {
    request.addFile(name, file, contentType);
    return this;
  }

  /**
   * Sets the method as HTTPS and server port as 443.
   *
   * @return This.
   */
  public RequestBuilder usingHTTPS() {
    request.setScheme("HTTPS");
    request.setServerPort(443);
    return this;
  }

  /**
   * Adds a mocked out service, action, etc to this request. This helps if the action needs to be tested with different
   * configuration or for error handling.
   *
   * @param iface The interface to mock out.
   * @param impl  The mocked out implementation of the interface.
   * @return This.
   */
  public <T> RequestBuilder withMock(final Class<T> iface, final T impl) {
    modules.add(new Module() {
      public void configure(Binder binder) {
        binder.bind(iface).toInstance(impl);
      }
    });

    return this;
  }

  /**
   * Sends the HTTP request to the MVC as a POST.
   *
   * @throws IOException      If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public void post() throws IOException, ServletException {
    request.setPost(true);
    test.run(this);
  }

  /**
   * Sends the HTTP request to the MVC as a GET.
   *
   * @throws IOException      If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public void get() throws IOException, ServletException {
    request.setPost(false);
    test.run(this);
  }

  /**
   * Sends the HTTP request to the MVC as a HEAD.
   *
   * @throws IOException      If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public void head() throws IOException, ServletException {
    request.setMethod(Method.HEAD);
    test.run(this);
  }

  /**
   * Sends the HTTP request to the MVC as a PUT.
   *
   * @throws IOException      If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public void put() throws IOException, ServletException {
    request.setMethod(Method.PUT);
    test.run(this);
  }

  /**
   * Sends the HTTP request to the MVC as a DELETE.
   *
   * @throws IOException      If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public void delete() throws IOException, ServletException {
    request.setMethod(Method.DELETE);
    test.run(this);
  }

  /**
   * Sends the HTTP request to the MVC as a OPTIONS.
   *
   * @throws IOException      If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public void options() throws IOException, ServletException {
    request.setMethod(Method.OPTIONS);
    test.run(this);
  }

  /**
   * Sends the HTTP request to the MVC as a TRACE.
   *
   * @throws IOException      If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public void trace() throws IOException, ServletException {
    request.setMethod(Method.TRACE);
    test.run(this);
  }

  /**
   * Sends the HTTP request to the MVC as a CONNECT.
   *
   * @throws IOException      If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public void connect() throws IOException, ServletException {
    request.setMethod(Method.CONNECT);
    test.run(this);
  }

  public MockHttpServletRequest getRequest() {
    return request;
  }

  public List<Module> getModules() {
    return modules;
  }
}

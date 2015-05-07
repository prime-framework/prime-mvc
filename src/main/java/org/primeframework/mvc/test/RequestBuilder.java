/*
 * Copyright (c) 2012-2015, Inversoft Inc., All Rights Reserved
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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Consumer;

import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletRequest.Method;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mock.servlet.MockHttpSession;
import org.primeframework.mock.servlet.MockServletInputStream;
import org.primeframework.mvc.parameter.DefaultParameterParser;
import org.primeframework.mvc.servlet.PrimeFilter;
import org.primeframework.mvc.servlet.ServletObjectsHolder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;

/**
 * This class is a builder that helps create a test HTTP request that is sent to the MVC.
 *
 * @author Brian Pontarelli
 */
public class RequestBuilder {
  public final PrimeFilter filter;

  public final Injector injector;

  public final MockHttpServletRequest request;

  public final MockHttpServletResponse response;

  public RequestBuilder(String uri, MockHttpSession session, PrimeFilter filter, Injector injector) {
    this.request = new MockHttpServletRequest(uri, Locale.getDefault(), false, "UTF-8", session);
    this.response = new MockHttpServletResponse();
    this.filter = filter;
    this.injector = injector;
  }

  /**
   * Sends the HTTP request to the MVC as a CONNECT.
   *
   * @return The response.
   * @throws IOException If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public RequestResult connect() throws IOException, ServletException {
    request.setMethod(Method.CONNECT);
    run();
    return new RequestResult(request, response, injector);
  }

  /**
   * Sends the HTTP request to the MVC as a DELETE.
   *
   * @return The response.
   * @throws IOException If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public RequestResult delete() throws IOException, ServletException {
    request.setMethod(Method.DELETE);
    run();
    return new RequestResult(request, response, injector);
  }

  /**
   * Sends the HTTP request to the MVC as a GET.
   *
   * @return The response.
   * @throws IOException If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public RequestResult get() throws IOException, ServletException {
    request.setPost(false);
    run();
    return new RequestResult(request, response, injector);
  }

  public MockHttpServletRequest getRequest() {
    return request;
  }

  /**
   * Sends the HTTP request to the MVC as a HEAD.
   *
   * @return The response.
   * @throws IOException If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public RequestResult head() throws IOException, ServletException {
    request.setMethod(Method.HEAD);
    run();
    return new RequestResult(request, response, injector);
  }

  /**
   * Sends the HTTP request to the MVC as a OPTIONS.
   *
   * @return The response.
   * @throws IOException If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public RequestResult options() throws IOException, ServletException {
    request.setMethod(Method.OPTIONS);
    run();
    return new RequestResult(request, response, injector);
  }

  /**
   * Sends the HTTP request to the MVC as a POST.
   *
   * @return The response.
   * @throws IOException If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public RequestResult post() throws IOException, ServletException {
    request.setPost(true);
    run();
    return new RequestResult(request, response, injector);
  }

  /**
   * Sends the HTTP request to the MVC as a PUT.
   *
   * @return The response.
   * @throws IOException If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public RequestResult put() throws IOException, ServletException {
    request.setMethod(Method.PUT);
    run();
    return new RequestResult(request, response, injector);
  }

  /**
   * Provides the ability to setup the MockHttpServletRequest object before making the request.
   *
   * @param consumer A consumer that takes the MockHttpServletRequest.
   * @return This.
   */
  public RequestBuilder setup(Consumer<MockHttpServletRequest> consumer) {
    consumer.accept(request);
    return this;
  }

  /**
   * Sends the HTTP request to the MVC as a TRACE.
   *
   * @return The response.
   * @throws IOException If the MVC throws an exception.
   * @throws ServletException If the MVC throws an exception.
   */
  public RequestResult trace() throws IOException, ServletException {
    request.setMethod(Method.TRACE);
    run();
    return new RequestResult(request, response, injector);
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
   * Sets the body content.
   *
   * @param body The body as a String.
   * @return This.
   */
  public RequestBuilder withBody(String body) {
    try {
      return withBody(body.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Sets the body content.
   *
   * @param body   The body as a {@link Path} to the JSON file.
   * @param values key value pairs of replacement values for use in the JSON file.
   * @return
   * @throws IOException
   */
  public RequestBuilder withBodyFile(Path body, Object... values) throws IOException {
    if (values.length == 0) {
      return withBody(Files.readAllBytes(body));
    }
    return withBody(BodyTools.processTemplate(body, values));
  }

  /**
   * Sets an HTTP request parameter as a Prime MVC checkbox widget. This can be called multiple times with the same name
   * it it will create a list of values for the HTTP parameter.
   *
   * @param name           The name of the parameter.
   * @param checkedValue   The checked value of the checkbox.
   * @param uncheckedValue The checked value of the checkbox.
   * @param checked        If the checkbox is checked.
   * @return This.
   */
  public RequestBuilder withCheckbox(String name, String checkedValue, String uncheckedValue, boolean checked) {
    if (checked) {
      request.setParameter(name, checkedValue);
    }

    request.setParameter(DefaultParameterParser.CHECKBOX_PREFIX + name, uncheckedValue);
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
   * Uses the given object as the JSON body for the request. This object is converted into JSON using Jackson.
   *
   * @param object The object to send in the request.
   * @return This.
   * @throws JsonProcessingException If the Jackson marshalling failed.
   */
  public RequestBuilder withJSON(Object object) throws JsonProcessingException {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    byte[] json = objectMapper.writeValueAsBytes(object);
    return withContentType("application/json").withBody(json);
  }

  /**
   * Uses the given string as the JSON body for the request.
   *
   * @param json The string representation of the JSON to send in the request.
   * @return This.
   */
  public RequestBuilder withJSON(String json) throws JsonProcessingException {
    return withContentType("application/json").withBody(json);
  }

  /**
   * Uses the given {@Path} object to a JSON file as the JSON body for the request.
   *
   * @param jsonFile The string representation of the JSON to send in the request.
   * @param values   key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException
   */
  public RequestBuilder withJSONFile(Path jsonFile, Object... values) throws IOException {
    return withContentType("application/json").withBodyFile(jsonFile, values);
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
   * Sets an HTTP request parameter. This can be called multiple times with the same name it it will create a list of
   * values for the HTTP parameter.
   *
   * @param name  The name of the parameter.
   * @param value The parameter value. This is an object so toString is called on it to convert it to a String.
   * @return This.
   */
  public RequestBuilder withParameter(String name, Object value) {
    request.setParameter(name, value.toString());
    return this;
  }

  /**
   * Sets an HTTP request parameter as a Prime MVC radio button widget. This can be called multiple times with the same
   * name it it will create a list of values for the HTTP parameter.
   *
   * @param name           The name of the parameter.
   * @param checkedValue   The checked value of the checkbox.
   * @param uncheckedValue The checked value of the checkbox.
   * @param checked        If the checkbox is checked.
   * @return This.
   */
  public RequestBuilder withRadio(String name, String checkedValue, String uncheckedValue, boolean checked) {
    if (checked) {
      request.setParameter(name, checkedValue);
    }

    request.setParameter(DefaultParameterParser.RADIOBUTTON_PREFIX + name, uncheckedValue);
    return this;
  }

  void run() throws IOException, ServletException {
    // Remove the web objects if this instance is being used across multiple invocations
    ServletObjectsHolder.clearServletRequest();
    ServletObjectsHolder.clearServletResponse();

    // Build the request and response for this pass
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

}

/*
 * Copyright (c) 2012-2020, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import org.primeframework.mock.servlet.MockContainer;
import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletRequest.Method;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mock.servlet.MockServletInputStream;
import org.primeframework.mvc.parameter.DefaultParameterParser;
import org.primeframework.mvc.security.CSRF;
import org.primeframework.mvc.servlet.PrimeFilter;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.primeframework.mvc.util.QueryStringTools;
import org.primeframework.mvc.util.URITools;

/**
 * This class is a builder that helps create a test HTTP request that is sent to the MVC.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unused")
public class RequestBuilder {
  public final MockContainer container;

  public final PrimeFilter filter;

  public final Injector injector;

  public final MockHttpServletRequest request;

  public final MockHttpServletResponse response;

  private Class<? extends Throwable> expectedException;

  public RequestBuilder(String uri, MockContainer container, PrimeFilter filter, Injector injector) {
    this.container = container;
    this.filter = filter;
    this.injector = injector;
    this.request = container.newServletRequest(uri, Locale.getDefault(), false, "UTF-8");
    this.response = container.newServletResponse();
  }

  /**
   * Sends the HTTP request to the MVC as a CONNECT.
   *
   * @return The response.
   */
  public RequestResult connect() {
    request.setMethod(Method.CONNECT);
    run();
    return new RequestResult(container, filter, request, response, injector);
  }

  /**
   * Sends the HTTP request to the MVC as a DELETE.
   *
   * @return The response.
   */
  public RequestResult delete() {
    request.setMethod(Method.DELETE);
    run();
    return new RequestResult(container, filter, request, response, injector);
  }

  /**
   * Indicates then when the HTTP method is called an exception is expected to be thrown.
   * <p>
   * An {@link AssertionError} will be thrown if the exception is not thrown.
   *
   * @param expectedException The expected exception.
   * @return This.
   */
  public RequestBuilder expectException(Class<? extends Throwable> expectedException) {
    this.expectedException = expectedException;
    return this;
  }

  /**
   * Sends the HTTP request to the MVC as a GET.
   *
   * @return The response.
   */
  public RequestResult get() {
    request.setPost(false);
    run();
    return new RequestResult(container, filter, request, response, injector);
  }

  public MockHttpServletRequest getRequest() {
    return request;
  }

  /**
   * Sends the HTTP request to the MVC as a HEAD.
   *
   * @return The response.
   */
  public RequestResult head() {
    request.setMethod(Method.HEAD);
    run();
    return new RequestResult(container, filter, request, response, injector);
  }

  /**
   * If the provided test is false invoke the consumer.
   *
   * @param test     the test
   * @param consumer the consumer of this request builder
   * @return This.
   */
  public RequestBuilder ifFalse(boolean test, Consumer<RequestBuilder> consumer) {
    return ifTrue(!test, consumer);
  }

  /**
   * If the provided test is true invoke the consumer.
   *
   * @param test     the test
   * @param consumer the consumer of this request builder
   * @return This.
   */
  public RequestBuilder ifTrue(boolean test, Consumer<RequestBuilder> consumer) {
    if (test) {
      consumer.accept(this);
    }

    return this;
  }

  /**
   * Overrides the HTTP Method from that set by calling {@link #get()} or {@link #post()} for example.
   *
   * @param method the string value of an HTTP method, this does not have to be a real HTTP method
   * @return This.
   */
  public RequestResult method(String method) {
    request.setOverrideMethod(method);
    run();
    return new RequestResult(container, filter, request, response, injector);
  }

  /**
   * Sends the HTTP request to the MVC as a OPTIONS.
   *
   * @return The response.
   */
  public RequestResult options() {
    request.setMethod(Method.OPTIONS);
    run();
    return new RequestResult(container, filter, request, response, injector);
  }

  /**
   * Sends the HTTP request to the MVC as a PATCH.
   *
   * @return The response.
   */
  public RequestResult patch() {
    request.setMethod(Method.PATCH);
    run();
    return new RequestResult(container, filter, request, response, injector);
  }

  /**
   * Sends the HTTP request to the MVC as a POST.
   *
   * @return The response.
   */
  public RequestResult post() {
    request.setPost(true);
    run();
    return new RequestResult(container, filter, request, response, injector);
  }

  /**
   * Sends the HTTP request to the MVC as a PUT.
   *
   * @return The response.
   */
  public RequestResult put() {
    request.setMethod(Method.PUT);
    run();
    return new RequestResult(container, filter, request, response, injector);
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
   */
  public RequestResult trace() {
    request.setMethod(Method.TRACE);
    run();
    return new RequestResult(container, filter, request, response, injector);
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
   * Adds an Authorization header to the request using the specified value.
   * <p>Shorthand for calling
   * <pre>
   *   withHeader("Authorization", value)
   * </pre>
   *
   * @param value The value of the <code>Authorization</code> header
   * @return This.
   */
  public RequestBuilder withAuthorizationHeader(Object value) {
    request.addHeader("Authorization", value.toString());
    return this;
  }

  /**
   * Adds a Basic Authorization header to the request using the specified value.
   * <p>Shorthand for calling
   * <pre>
   *   String basic = username + (password != null ? ":" + password : "");
   *   withHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(basic.getBytes());)
   * </pre>
   *
   * @param username The username used to build the basic authorization scheme
   * @param password The password used to build the basic authorization scheme
   * @return This.
   */
  public RequestBuilder withBasicAuthorizationHeader(String username, String password) {
    String basic = username + (password != null ? ":" + password : "");
    request.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(basic.getBytes()));
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
    return withBody(body.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Sets the body content. This processes the file using FreeMarker. Use {@link #withBodyFileRaw(Path)} to skip
   * FreeMarker processing.
   *
   * @param body   The body as a {@link Path} to the file.
   * @param values key value pairs of replacement values for use in the file.
   * @return This.
   * @throws IOException If the file could not be loaded.
   */
  public RequestBuilder withBodyFile(Path body, Object... values) throws IOException {
    return withBody(BodyTools.processTemplate(body, values));
  }

  /**
   * Sets the body content.
   *
   * @param body The body as a {@link Path} to the raw file.
   * @return This.
   * @throws IOException If the file could not be loaded.
   */
  public RequestBuilder withBodyFileRaw(Path body) throws IOException {
    return withBody(Files.readAllBytes(body));
  }

  /**
   * Adds a CSRF token to the request parameters.
   *
   * @param token The token.
   * @return This.
   */
  public RequestBuilder withCSRFToken(String token) {
    request.removeParameter(CSRF.CSRF_PARAMETER_KEY);
    return withParameter(CSRF.CSRF_PARAMETER_KEY, token);
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
   * Add a cookie to the request.
   *
   * @param name  The name of the cookie.
   * @param value The value of the cookie.
   * @return This.
   */
  public RequestBuilder withCookie(String name, String value) {
    if (name != null) {
      request.addCookie(new Cookie(name, value));
    }
    return this;
  }

  /**
   * Add a cookie to the request.
   *
   * @param cookie The cookie.
   * @return This.
   */
  public RequestBuilder withCookie(Cookie cookie) {
    if (cookie != null) {
      request.addCookie(cookie);
    }
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
  public RequestBuilder withHeader(String name, Object value) {
    request.addHeader(name, value.toString());
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
  public RequestBuilder withJSON(String json) {
    return withContentType("application/json").withBody(json);
  }

  /**
   * Uses the given {@link Path} object to a JSON file as the JSON body for the request.
   *
   * @param jsonFile The string representation of the JSON to send in the request.
   * @param values   key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException If the file could not be loaded.
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
   * Sets HTTP request parameters. This takes the provided collection and adds a request parameter for each item in the
   * collection.
   *
   * @param name   The name of the parameter.
   * @param values The collection of parameter values.
   * @return This.
   */
  public RequestBuilder withParameters(String name, Collection<?> values) {
    for (Object value : values) {
      request.setParameter(name, value.toString());
    }
    return this;
  }

  /**
   * Sets HTTP request parameters from a query String that is already encoded and might contain multiple parameters.
   * This will split the parameters up and decode them.
   *
   * @param query The query string.
   * @return This.
   */
  public RequestBuilder withQueryString(String query) {
    QueryStringTools.parseQueryString(query).forEach(this::withParameters);
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

  public RequestBuilder withSingleHeader(String name, String value) {
    request.removeHeader(name);
    request.addHeader(name, value);
    return this;
  }

  /**
   * Append a url path segment to the current request URI.
   * <p>
   * For Example:
   * <pre>
   *   .simulator.test("/user/delete")
   *             .withUrlSegment("bar")
   *   </pre>
   * This will result in a url of <code>/user/delete/bar</code>, this is equivalent to the following code:
   * <pre>
   *   .simulator.test("/user/delete/" + "bar")
   *   </pre>
   *
   * @param value The url path segment. A null value will be ignored.
   * @return This.
   */
  public RequestBuilder withURLSegment(Object value) {
    if (value != null) {
      String uri = request.getRequestURI();
      if (uri.charAt(uri.length() - 1) != '/') {
        uri += ('/');
      }

      request.setUri(uri + URITools.encodeURIPathSegment(value));
    }
    return this;
  }

  /**
   * Bad name, use the correct URL method.
   *
   * @deprecated
   */
  public RequestBuilder withUrlSegment(Object value) {
    return withURLSegment(value);
  }

  void run() {
    // Remove the web objects if this instance is being used across multiple invocations
    ServletObjectsHolder.clearServletRequest();
    ServletObjectsHolder.clearServletResponse();

    // If the CSRF token is enabled and the parameter isn't set, we set it to be consistent.
    if (CSRF.getParameterToken(request) == null) {
      String token = CSRF.getSessionToken(request);
      if (token != null) {
        request.setParameter(CSRF.CSRF_PARAMETER_KEY, token);
      }
    }

    try {
      // Build the request and response for this pass
      filter.doFilter(this.request, this.response, (req, resp) -> {
        throw new UnsupportedOperationException("The RequestSimulator class doesn't support testing " +
            "URIs that don't map to Prime resources");
      });
    } catch (Throwable e) {
      Class clazz = e.getClass();
      if (expectedException == null || !expectedException.equals(clazz)) {
        throw new AssertionError("\n\tUnexpected Exception thrown: [" + clazz.getCanonicalName() + "]", e);
      }
      expectedException = null;
    }

    if (expectedException != null) {
      throw new AssertionError("Expected Exception were not thrown: [" + expectedException.getCanonicalName() + "]");
    }

    // Add these back so that anything that needs them can be retrieved from the Injector after
    // the run has completed (i.e. MessageStore for the MVC and such)
    ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper(this.request));
    ServletObjectsHolder.setServletResponse(this.response);
  }
}
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import org.primeframework.mock.servlet.MockContainer;
import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletRequest.Method;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mock.servlet.MockServletInputStream;
import org.primeframework.mvc.parameter.DefaultParameterParser;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.servlet.PrimeFilter;
import org.primeframework.mvc.servlet.ServletObjectsHolder;
import org.primeframework.mvc.test.RequestResult.ThrowingConsumer;
import org.primeframework.mvc.util.QueryStringTools;
import org.primeframework.mvc.util.URITools;
import static org.testng.Assert.fail;

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

  public RequestBuilder(String uri, MockContainer container, PrimeFilter filter, Injector injector) {
    this.container = container;
    this.filter = filter;
    this.injector = injector;
    this.request = container.newServletRequest(uri, Locale.getDefault(), false, "UTF-8");
    this.response = container.newServletResponse();
  }

  /**
   * Allows the builder to be passed to other code to add parameters and set things up.
   *
   * @param consumer The consumer of this request builder.
   * @return This.
   */
  public RequestBuilder build(Consumer<RequestBuilder> consumer) {
    consumer.accept(this);
    return this;
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
   * If the provided test is true invoke the <code>ifConsumer</code>, else.. you guessed it.
   *
   * @param test         the test
   * @param ifConsumer   the consumer used if the test is true
   * @param elseConsumer the consumer used if the test is false
   * @return This.
   */
  public RequestBuilder ifElse(boolean test, ThrowingConsumer<RequestBuilder> ifConsumer,
                               ThrowingConsumer<RequestBuilder> elseConsumer) throws Exception {
    if (test) {
      ifConsumer.accept(this);
    } else {
      elseConsumer.accept(this);
    }

    return this;
  }

  /**
   * If the provided test is false invoke the consumer.
   *
   * @param test     the test
   * @param consumer the consumer of this request builder
   * @return This.
   */
  public RequestBuilder ifFalse(boolean test, ThrowingConsumer<RequestBuilder> consumer) throws Exception {
    return ifTrue(!test, consumer);
  }

  /**
   * If the provided test is true invoke the consumer.
   *
   * @param test     the test
   * @param consumer the consumer of this request builder
   * @return This.
   */
  public RequestBuilder ifTrue(boolean test, ThrowingConsumer<RequestBuilder> consumer) throws Exception {
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
   * Sets the body content. This processes the file using FreeMarker. Use {@link #withBodyFileRaw(Path)} to skip
   * FreeMarker processing.
   *
   * @param body     The body as a {@link Path} to the file.
   * @param consumer an optional consumer of a JSON node used to mutate the JSON body
   * @param values   key value pairs of replacement values for use in the file.
   * @return This.
   * @throws IOException If the file could not be loaded.
   */
  public RequestBuilder withBodyFile(Path body, Consumer<JSONBodyHelper> consumer, Object... values)
      throws IOException {
    String result = BodyTools.processTemplate(body, values);

    if (consumer != null) {
      ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
      JsonNode node = objectMapper.readTree(result);
      consumer.accept(new JSONBodyHelper(node));
      result = objectMapper.writeValueAsString(node);
    }

    return withBody(result);
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
    request.removeParameter(CSRFProvider.CSRF_PARAMETER_KEY);
    return withParameter(CSRFProvider.CSRF_PARAMETER_KEY, token);
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
      container.getUserAgent().addCookie(request, new Cookie(name, value));
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
      container.getUserAgent().addCookie(request, cookie);
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
    return withJSONFile(jsonFile, null, values);
  }

  /**
   * Uses the given {@link Path} object to a JSON file as the JSON body for the request.
   *
   * @param jsonFile The string representation of the JSON to send in the request.
   * @param consumer a JSON node consumer used to mutate the request before it is used.
   * @param values   key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException If the file could not be loaded.
   */
  public RequestBuilder withJSONFile(Path jsonFile, Consumer<JSONBodyHelper> consumer, Object... values)
      throws IOException {
    return withContentType("application/json").withBodyFile(jsonFile, consumer, values);
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
    CSRFProvider csrfProvider = injector.getInstance(CSRFProvider.class);
    if (csrfProvider.getTokenFromRequest(request) == null) {
      String token = csrfProvider.getToken(request);
      if (token != null) {
        request.setParameter(CSRFProvider.CSRF_PARAMETER_KEY, token);
      }
    }

    // Copy cookies from the response back to the request so we can be ready for the next request.
    container.getRequest().copyCookiesFromUserAgent();

    try {
      // Build the request and response for this pass
      filter.doFilter(this.request, this.response, (req, resp) -> {
        throw new UnsupportedOperationException("The RequestSimulator class doesn't support testing " +
            "URIs that don't map to Prime resources");
      });
    } catch (Throwable t) {
      fail("The exception should have been caught by the PrimeFilter", t);
    }

    // Add these back so that anything that needs them can be retrieved from the Injector after
    // the run has completed (i.e. MessageStore for the MVC and such)
    ServletObjectsHolder.setServletRequest(new HttpServletRequestWrapper(this.request));
    ServletObjectsHolder.setServletResponse(this.response);
  }
}

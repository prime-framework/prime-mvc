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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import com.inversoft.http.Cookie.SameSite;
import com.inversoft.http.FileUpload;
import com.inversoft.rest.ByteArrayBodyHandler;
import com.inversoft.rest.ByteArrayResponseHandler;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.MultipartBodyHandler;
import com.inversoft.rest.MultipartBodyHandler.Multiparts;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.RESTClient.BodyHandler;
import org.primeframework.mock.MockUserAgent;
import org.primeframework.mvc.http.Cookie;
import org.primeframework.mvc.http.DefaultHTTPRequest;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.MutableHTTPRequest;
import org.primeframework.mvc.message.TestMessageObserver;
import org.primeframework.mvc.parameter.DefaultParameterParser;
import org.primeframework.mvc.parameter.fileupload.FileInfo;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.test.RequestResult.ThrowingConsumer;
import org.primeframework.mvc.util.QueryStringTools;
import org.primeframework.mvc.util.URITools;

/**
 * This class is a builder that helps create a test HTTP request that is sent to the MVC.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class RequestBuilder {
  public final Injector injector;

  public final TestMessageObserver messageObserver;

  public final MutableHTTPRequest request;

  public final MockUserAgent userAgent;

  public RequestBuilder(int port, String path, Injector injector, MockUserAgent userAgent,
                        TestMessageObserver messageObserver) {
    this.injector = injector;
    this.userAgent = userAgent;
    this.messageObserver = messageObserver;
    this.request = new DefaultHTTPRequest().with(r -> r.addLocales(Locale.US))
                                           .with(r -> r.setPath(path))
                                           .with(r -> r.setPort(port));
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
    request.setMethod(HTTPMethod.CONNECT);
    ClientResponse<byte[], byte[]> response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver);
  }

  /**
   * Sends the HTTP request to the MVC as a DELETE.
   *
   * @return The response.
   */
  public RequestResult delete() {
    request.setMethod(HTTPMethod.DELETE);
    ClientResponse<byte[], byte[]> response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver);
  }

  /**
   * Sends the HTTP request to the MVC as a GET.
   *
   * @return The response.
   */
  public RequestResult get() {
    request.setMethod(HTTPMethod.GET);
    ClientResponse<byte[], byte[]> response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver);
  }

  public HTTPRequest getRequest() {
    return request;
  }

  /**
   * Sends the HTTP request to the MVC as a HEAD.
   *
   * @return The response.
   */
  public RequestResult head() {
    request.setMethod(HTTPMethod.HEAD);
    ClientResponse<byte[], byte[]> response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver);
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
    request.setMethod(HTTPMethod.of(method));
    ClientResponse<byte[], byte[]> response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver);
  }

  /**
   * Sends the HTTP request to the MVC as a OPTIONS.
   *
   * @return The response.
   */
  public RequestResult options() {
    request.setMethod(HTTPMethod.OPTIONS);
    ClientResponse<byte[], byte[]> response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver);
  }

  /**
   * Sends the HTTP request to the MVC as a PATCH.
   *
   * @return The response.
   */
  public RequestResult patch() {
    request.setMethod(HTTPMethod.PATCH);
    ClientResponse<byte[], byte[]> response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver);
  }

  /**
   * Sends the HTTP request to the MVC as a POST.
   *
   * @return The response.
   */
  public RequestResult post() {
    request.setMethod(HTTPMethod.POST);
    ClientResponse<byte[], byte[]> response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver);
  }

  /**
   * Sends the HTTP request to the MVC as a PUT.
   *
   * @return The response.
   */
  public RequestResult put() {
    request.setMethod(HTTPMethod.PUT);
    ClientResponse<byte[], byte[]> response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver);
  }

  /**
   * Provides the ability to setup the MockHttpServletRequest object before making the request.
   *
   * @param consumer A consumer that takes the MockHttpServletRequest.
   * @return This.
   */
  public RequestBuilder setup(Consumer<HTTPRequest> consumer) {
    consumer.accept(request);
    return this;
  }

  /**
   * Sends the HTTP request to the MVC as a TRACE.
   *
   * @return The response.
   */
  public RequestResult trace() {
    request.setMethod(HTTPMethod.TRACE);
    ClientResponse<byte[], byte[]> response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver);
  }

  /**
   * Sets the method as HTTPS and server port as 443.
   *
   * @return This.
   */
  public RequestBuilder usingHTTPS() {
    throw new IllegalStateException("This handling is not implemented yet");
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
    request.setHeader("Authorization", value.toString());
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
    request.setHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(basic.getBytes()));
    return this;
  }

  /**
   * Sets the body content.
   *
   * @param bytes The bytes.
   * @return This.
   */
  public RequestBuilder withBody(byte[] bytes) {
    request.setBody(bytes);
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
    request.setParameter(CSRFProvider.CSRF_PARAMETER_KEY, token);
    return this;
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
      Cookie cookie = new Cookie(name, value);
      request.addCookies(cookie);
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
      request.addCookies(cookie);
    }
    return this;
  }

  /**
   * Sets the encoding.
   *
   * @param encoding The encoding.
   * @return This.
   */
  public RequestBuilder withEncoding(Charset encoding) {
    request.setCharacterEncoding(encoding);
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
  public RequestBuilder withFile(String name, Path file, String contentType) {
    request.addFile(new FileInfo(file, null, name, contentType));
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

  public RequestBuilder withJSONBuilder(ThrowingConsumer<JSONBuilder> consumer) throws Exception {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    JSONBuilder builder = new JSONBuilder(objectMapper);
    consumer.accept(builder);
    withJSON(builder.build());
    return this;
  }

  /**
   * Uses the given {@link Path} object to a JSON file as the JSON body for the request.
   *
   * @param jsonFile The string representation of the JSON to send in the request.
   * @param values   key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException If the file could not be loaded.
   */
  public RequestBuilder withJSONFile(Path jsonFile, Object... values)
      throws IOException {
    return withContentType("application/json").withBodyFile(jsonFile, values);
  }

  /**
   * Sets the locale that will be used.
   *
   * @param locale The locale.
   * @return This.
   */
  public RequestBuilder withLocale(Locale locale) {
    request.addLocales(locale);
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
    request.addParameter(name, value.toString());
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
    request.addParameters(name, values.stream().map(Object::toString).collect(Collectors.toList()));
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
    request.setHeader(name, value);
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
      String uri = request.getPath();
      if (uri.charAt(uri.length() - 1) != '/') {
        uri += ('/');
      }

      request.setPath(uri + URITools.encodeURIPathSegment(value));
    }
    return this;
  }

  /**
   * Bad name, use the correct URL method.
   *
   * @deprecated
   */
  @Deprecated
  public RequestBuilder withUrlSegment(Object value) {
    return withURLSegment(value);
  }

  ClientResponse<byte[], byte[]> run() {
    // Set the new request in so we can inject things below
    HTTPObjectsHolder.clearRequest();
    HTTPObjectsHolder.setRequest(request);

    // Clear the messages for each run
    messageObserver.clear();

    // New cookies were added to the request and not the user-agent, so we need to sync them to the user-agent here.
    // Then we can add all cookies form the user-agent to the request.
    userAgent.addCookies(request.getCookies());
    request.getCookies().clear();
    request.addCookies(userAgent.getCookies(request));
    var restCookies = request.getCookies()
                             .stream()
                             .map(c -> new com.inversoft.http.Cookie().with(c1 -> c1.domain = c.domain)
                                                                      .with(c1 -> c1.expires = c.expires)
                                                                      .with(c1 -> c1.httpOnly = c.httpOnly)
                                                                      .with(c1 -> c1.maxAge = c.maxAge)
                                                                      .with(c1 -> c1.name = c.name)
                                                                      .with(c1 -> c1.path = c.path)
                                                                      .with(c1 -> c1.sameSite = c.sameSite != null ? SameSite.valueOf(c.sameSite.name()) : null)
                                                                      .with(c1 -> c1.secure = c.secure)
                                                                      .with(c1 -> c1.value = c.value))
                             .collect(Collectors.toList());

    // Referer for CSRF checks must be provided
    if (request.getHeader("Referer") == null) {
      request.setHeader("Referer", "http://localhost:" + request.getPort() + request.getPath());
    }

    // Now that the cookies are ready, if the CSRF token is enabled and the parameter isn't set, we set it to be consistent
    // since the [@control.form] would normally set that into the form and into the request.
    CSRFProvider csrfProvider = injector.getInstance(CSRFProvider.class);
    if (csrfProvider.getTokenFromRequest(request) == null) {
      String token = csrfProvider.getToken(request);
      if (token != null) {
        request.setParameter(CSRFProvider.CSRF_PARAMETER_KEY, token);
      }
    }

    List<Locale> locales = request.getLocales();
    String contentType = request.getContentType();
    Charset characterEncoding = request.getCharacterEncoding();
    HTTPMethod method = request.getMethod();

    // Handle URL parameters and body
    Map<String, List<String>> parameters = request.getParameters();
    List<FileInfo> files = request.getFiles();
    byte[] body = request.getBody();
    Map<String, Object> urlParameters = null;
    BodyHandler bodyHandler = null;
    if (files != null && files.size() > 0) {
      List<FileUpload> fileUploads = files.stream()
                                          .map(fi -> new FileUpload(fi.contentType, fi.file, fi.fileName, fi.name))
                                          .collect(Collectors.toList());
      bodyHandler = new MultipartBodyHandler(new Multiparts(fileUploads, parameters));
    } else if (body != null) {
      bodyHandler = new ByteArrayBodyHandler(body);

      // Send these are URL parameters
      if (parameters.size() > 0) {
        urlParameters = new HashMap<>(parameters);
      }
    } else if (parameters.size() > 0) {
      // Making the assumption here that the parameters are on the URL for GET, HEAD and DELETE and in the body for everything else
      if (HTTPMethod.GET.is(method) || HTTPMethod.HEAD.is(method) || HTTPMethod.DELETE.is(method)) {
        urlParameters = new HashMap<>(parameters);
      } else {
        bodyHandler = new FormDataBodyHandler(parameters);
      }
    }

    ClientResponse<byte[], byte[]> response = new RESTClient<>(byte[].class, byte[].class)
        .bodyHandler(bodyHandler)
        .connectTimeout(0)
        .cookies(restCookies)
        .errorResponseHandler(new ByteArrayResponseHandler())
        .followRedirects(false)
        .header("Accept-Language", locales.size() > 0 ? locales.stream().map(Locale::toLanguageTag).collect(Collectors.joining(", ")) : null)
        .header("Content-Type", contentType != null ? (contentType + (characterEncoding != null ? "; charset=" + characterEncoding : "")) : null)
        .headersMap(request.getHeadersMap())
        .method(request.getMethod().name())
        .readTimeout(0)
        .successResponseHandler(new ByteArrayResponseHandler())
        .url("http://localhost:" + request.getPort() + request.getPath())
        .urlParameters(urlParameters)
        .go();

    // Extract the cookies and put them in the UserAgent
    userAgent.addCookies(response.getCookies()
                                 .stream()
                                 .map(c -> new Cookie().with(c1 -> c1.domain = c.domain)
                                                       .with(c1 -> c1.expires = c.expires)
                                                       .with(c1 -> c1.httpOnly = c.httpOnly)
                                                       .with(c1 -> c1.maxAge = c.maxAge)
                                                       .with(c1 -> c1.name = c.name)
                                                       .with(c1 -> c1.path = c.path)
                                                       .with(c1 -> c1.sameSite = c.sameSite != null ? Cookie.SameSite.valueOf(c.sameSite.name()) : null)
                                                       .with(c1 -> c1.secure = c.secure)
                                                       .with(c1 -> c1.value = c.value))
                                 .collect(Collectors.toList()));

    return response;
  }
}

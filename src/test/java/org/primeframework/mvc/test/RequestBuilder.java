/*
 * Copyright (c) 2012-2024, Inversoft Inc., All Rights Reserved
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.Cookie.SameSite;
import io.fusionauth.http.FileInfo;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.HTTPValues;
import io.fusionauth.http.io.BlockingByteBufferOutputStream;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mock.MockUserAgent;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.http.FormDataBodyHandler;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.http.MultipartBodyHandler;
import org.primeframework.mvc.http.MultipartBodyHandler.Multiparts;
import org.primeframework.mvc.http.MultipartFileUpload;
import org.primeframework.mvc.message.TestMessageObserver;
import org.primeframework.mvc.parameter.DefaultParameterParser;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.test.RequestResult.ThrowingConsumer;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.util.QueryStringTools;
import org.primeframework.mvc.util.URITools;

/**
 * This class is a builder that helps create a test HTTP request that is sent to the MVC.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class RequestBuilder {
  public static HttpClient HttpClientInstance = newHttpClient();

  public final Injector injector;

  public final TestMessageObserver messageObserver;

  public final int port;

  public final HTTPRequest request;

  public final Map<String, List<String>> requestBodyParameters = new LinkedHashMap<>();

  public final MockUserAgent userAgent;

  public boolean useTLS;

  private byte[] body;

  public RequestBuilder(String path, Injector injector, MockUserAgent userAgent, TestMessageObserver messageObserver,
                        int port) {
    this.injector = injector;
    this.userAgent = userAgent;
    this.messageObserver = messageObserver;
    this.request = new HTTPRequest().with(r -> r.addLocales(Locale.US))
                                    .with(r -> r.setPath(path));
    this.port = port;
  }

  public static HttpClient newHttpClient() {
    return HttpClient.newBuilder()
                     // Setting this too low will cause TLS connections to timeout
                     // - We used to set it to 0 in our own REST client, but 0 is not supported.
                     // - 50 works fine for non TLS tests.
                     .connectTimeout(Duration.ofMillis(250))
                     .followRedirects(Redirect.NEVER)
                     .build();
  }

  public static void resetHttpClientInstance() {
    HttpClientInstance = newHttpClient();
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
    HTTPResponseWrapper response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver, port);
  }

  /**
   * Sends the HTTP request to the MVC as a DELETE.
   *
   * @return The response.
   */
  public RequestResult delete() {
    request.setMethod(HTTPMethod.DELETE);
    HTTPResponseWrapper response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver, port);
  }

  /**
   * Sends the HTTP request to the MVC as a GET.
   *
   * @return The response.
   */
  public RequestResult get() {
    request.setMethod(HTTPMethod.GET);
    HTTPResponseWrapper response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver, port);
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
    HTTPResponseWrapper response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver, port);
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
   * @param method the  HTTP method
   * @return This.
   */
  public RequestResult method(HTTPMethod method) {
    request.setMethod(method);
    HTTPResponseWrapper response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver, port);
  }

  /**
   * Overrides the HTTP Method from that set by calling {@link #get()} or {@link #post()} for example.
   *
   * @param method the string value of an HTTP method, this does not have to be a real HTTP method
   * @return This.
   */
  public RequestResult method(String method) {
    return method(HTTPMethod.of(method));
  }

  /**
   * Sends the HTTP request to the MVC as a OPTIONS.
   *
   * @return The response.
   */
  public RequestResult options() {
    request.setMethod(HTTPMethod.OPTIONS);
    HTTPResponseWrapper response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver, port);
  }

  /**
   * Sends the HTTP request to the MVC as a PATCH.
   *
   * @return The response.
   */
  public RequestResult patch() {
    request.setMethod(HTTPMethod.PATCH);
    HTTPResponseWrapper response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver, port);
  }

  /**
   * Sends the HTTP request to the MVC as a POST.
   *
   * @return The response.
   */
  public RequestResult post() {
    request.setMethod(HTTPMethod.POST);
    HTTPResponseWrapper response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver, port);
  }

  /**
   * Sends the HTTP request to the MVC as a PUT.
   *
   * @return The response.
   */
  public RequestResult put() {
    request.setMethod(HTTPMethod.PUT);
    HTTPResponseWrapper response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver, port);
  }

  /**
   * Provides the ability to set up the HTTPRequest object before making the request.
   *
   * @param consumer A consumer that takes the MutableHTTPRequest.
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
    HTTPResponseWrapper response = run();
    return new RequestResult(injector, request, response, userAgent, messageObserver, port);
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
    this.body = bytes;
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
   * Sets the body content. This processes the file using FreeMarker. Use {@link #withBodyFileRaw(Path)} to skip FreeMarker processing.
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
    String parameterName = injector.getInstance(CSRFProvider.class).getParameterName();
    request.addURLParameter(parameterName, token);
    return this;
  }

  /**
   * Sets an HTTP request body parameter as a Prime MVC checkbox widget. This can be called multiple times with the same name, and it will create a
   * list of values for the HTTP parameter.
   *
   * @param name           The name of the parameter.
   * @param checkedValue   The checked value of the checkbox.
   * @param uncheckedValue The checked value of the checkbox.
   * @param checked        If the checkbox is checked.
   * @return This.
   */
  public RequestBuilder withCheckbox(String name, String checkedValue, String uncheckedValue, boolean checked) {
    if (checked) {
      withParameter(name, checkedValue);
    }

    withParameter(DefaultParameterParser.CHECKBOX_PREFIX + name, uncheckedValue);
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
    request.getFiles().add(new FileInfo(file, null, name, contentType, StandardCharsets.UTF_8));
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
   * Uses the given object as the JSON body for the request with the {@code application/json} content type. This object is converted into JSON using
   * Jackson.
   *
   * @param object The object to send in the request.
   * @return This.
   * @throws JsonProcessingException If the Jackson marshalling failed.
   */
  public RequestBuilder withJSON(Object object) throws JsonProcessingException {
    return withJSON("application/json", object);
  }

  /**
   * Uses the given object as the JSON body for the request with the given content type. This object is converted into JSON using Jackson.
   *
   * @param contentType The Content-Type header value
   * @param object      The object to send in the request.
   * @return This.
   * @throws JsonProcessingException If the Jackson marshalling failed.
   */
  public RequestBuilder withJSON(String contentType, Object object) throws JsonProcessingException {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    byte[] json = objectMapper.writeValueAsBytes(object);
    return withContentType(contentType).withBody(json);
  }

  /**
   * Uses the given string as the JSON body for the request with the {@code application/json} content type.
   *
   * @param json The string representation of the JSON to send in the request.
   * @return This.
   */
  public RequestBuilder withJSON(String json) {
    return withJSON("application/json", json);
  }

  /**
   * Uses the given string as the JSON body for the request with the given content type.
   *
   * @param contentType The Content-Type header value
   * @param json        The string representation of the JSON to send in the request.
   * @return This.
   */
  public RequestBuilder withJSON(String contentType, String json) {
    return withContentType(contentType).withBody(json);
  }

  /**
   * Applies the given consumer to build a JSON body for the request with the {@code application/json} content type.
   *
   * @param consumer The {@link JSONBuilder} consumer to build request
   * @return This.
   * @throws Exception If the Jackson marshalling failed or consumer threw an exception.
   */
  public RequestBuilder withJSONBuilder(ThrowingConsumer<JSONBuilder> consumer) throws Exception {
    return withJSONBuilder("application/json", consumer);
  }

  /**
   * Applies the given consumer to build a JSON body for the request with the given content type.
   *
   * @param contentType The Content-Type header value
   * @param consumer    The {@link JSONBuilder} consumer to build request
   * @return This.
   * @throws Exception If the Jackson marshalling failed or consumer threw an exception.
   */
  public RequestBuilder withJSONBuilder(String contentType, ThrowingConsumer<JSONBuilder> consumer) throws Exception {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    JSONBuilder builder = new JSONBuilder(objectMapper);
    consumer.accept(builder);
    withJSON(contentType, builder.build());
    return this;
  }

  /**
   * Uses the given {@link Path} object to a JSON file as the JSON body for the request with the {@code application/json} content type.
   *
   * @param jsonFile The string representation of the JSON to send in the request.
   * @param values   key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException If the file could not be loaded.
   */
  public RequestBuilder withJSONFile(Path jsonFile, Object... values)
      throws IOException {
    return withJSONFile("application/json", jsonFile, values);
  }

  /**
   * Uses the given {@link Path} object to a JSON file as the JSON body for the request with the given content type.
   *
   * @param contentType The Content-Type header value
   * @param jsonFile    The string representation of the JSON to send in the request.
   * @param values      key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException If the file could not be loaded.
   */
  public RequestBuilder withJSONFile(String contentType, Path jsonFile, Object... values)
      throws IOException {
    return withContentType(contentType).withBodyFile(jsonFile, values);
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
   * Sets an HTTP request body parameter. This can be called multiple times with the same name it will create a list of values for the HTTP
   * parameter.
   *
   * @param name  The name of the parameter.
   * @param value The parameter value. This is an object so toString is called on it to convert it to a String.
   * @return This.
   */
  public RequestBuilder withParameter(String name, Object value) {
    requestBodyParameters.computeIfAbsent(name, key -> new ArrayList<>()).add(value != null ? value.toString() : null);
    return this;
  }

  /**
   * Sets HTTP request body parameters. This takes the provided collection and adds a request parameter for each item in the collection.
   *
   * @param name   The name of the parameter.
   * @param values The collection of parameter values.
   * @return This.
   */
  public RequestBuilder withParameters(String name, Collection<?> values) {
    requestBodyParameters.computeIfAbsent(name, key -> new ArrayList<>()).addAll(values.stream().map(Object::toString).toList());
    return this;
  }

  /**
   * Sets HTTP request parameters from a query String that is already encoded and might contain multiple parameters. This will split the parameters up
   * and decode them.
   *
   * @param query The query string.
   * @return This.
   */
  public RequestBuilder withQueryString(String query) {
    QueryStringTools.parseQueryString(query).forEach(this::withURLParameters);
    return this;
  }

  /**
   * Sets an HTTP request body parameter as a Prime MVC radio button widget. This can be called multiple times with the same name it will create a
   * list of values for the HTTP parameter.
   *
   * @param name           The name of the parameter.
   * @param checkedValue   The checked value of the checkbox.
   * @param uncheckedValue The checked value of the checkbox.
   * @param checked        If the checkbox is checked.
   * @return This.
   */
  public RequestBuilder withRadio(String name, String checkedValue, String uncheckedValue, boolean checked) {
    if (checked) {
      setRequestBodyParameter(name, checkedValue);
    }

    setRequestBodyParameter(DefaultParameterParser.RADIOBUTTON_PREFIX + name, uncheckedValue);
    return this;
  }

  /**
   * Add a single request header. Calling this sequentially will just overwrite the previous value. If you wish to add a header multiple times, use
   * {@link #withHeader(String, Object)} instead.
   *
   * @param name  The name of the header.
   * @param value The value of the header.
   * @return This.
   */
  public RequestBuilder withSingleHeader(String name, String value) {
    request.setHeader(name, value);
    return this;
  }

  /**
   * Add a single URL query string parameter.
   *
   * @param name  the name of the parameter.
   * @param value the value of the parameter.
   * @return This.
   */
  public RequestBuilder withURLParameter(String name, Object value) {
    request.addURLParameter(name, value.toString());
    return this;
  }

  /**
   * Add all URL query string parameters.
   *
   * @param name   the name of the parameter.
   * @param values The collection of parameter values.
   * @return This.
   */
  public RequestBuilder withURLParameters(String name, Collection<?> values) {
    request.addURLParameters(name, values.stream().map(Object::toString).collect(Collectors.toList()));
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
        uri += '/';
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

  HTTPResponseWrapper run() {
    // Set the new request & response so that we can inject things below
    HTTPObjectsHolder.clearRequest();
    HTTPObjectsHolder.setRequest(request);
    HTTPObjectsHolder.clearResponse();
    HTTPObjectsHolder.setResponse(new HTTPResponse(new BlockingByteBufferOutputStream(null, 1024, 32), request));

    // Add a unique Id so that we can identify messages for this request in the observer.
    String messageStoreId = UUID.randomUUID().toString();
    messageObserver.setMessageStoreId(messageStoreId);
    request.addHeader(TestMessageObserver.ObserverMessageStoreId, messageStoreId);

    // New cookies were added to the request and not the user-agent, so we need to sync them to the user-agent here.
    // Then we can add all cookies form the user-agent to the request.
    userAgent.addCookies(request.getCookies());
    request.getCookies().clear();
    request.addCookies(userAgent.getCookies(request));
    var restCookies = request.getCookies()
                             .stream()
                             .map(c -> new Cookie().with(c1 -> c1.domain = c.domain)
                                                   .with(c1 -> c1.expires = c.expires)
                                                   .with(c1 -> c1.httpOnly = c.httpOnly)
                                                   .with(c1 -> c1.maxAge = c.maxAge)
                                                   .with(c1 -> c1.name = c.name)
                                                   .with(c1 -> c1.path = c.path)
                                                   .with(c1 -> c1.sameSite = c.sameSite != null ? SameSite.valueOf(c.sameSite.name()) : null)
                                                   .with(c1 -> c1.secure = c.secure)
                                                   .with(c1 -> c1.value = c.value))
                             .toList();

    String scheme = useTLS ? "https://" : "http://";
    URI requestURI = URI.create(scheme + "localhost:" + port + request.getPath());

    // Referer for CSRF checks must be provided
    if (request.getHeader("Referer") == null) {
      request.setHeader("Referer", requestURI.toString());
    }

    request.setPort(port);
    request.setHost(requestURI.getHost());
    request.setScheme(requestURI.getScheme());

    // Now that the cookies are ready, if the CSRF token is enabled and the parameter isn't set, we set it to be consistent
    // since the [@control.form] would normally set that into the form and into the request.
    MVCConfiguration configuration = injector.getInstance(MVCConfiguration.class);
    if (configuration.csrfEnabled()) {
      CSRFProvider csrfProvider = injector.getInstance(CSRFProvider.class);
      if (csrfProvider.getTokenFromRequest(request) == null) {
        String token = csrfProvider.getToken(request);
        if (token != null) {
          String parameterName = csrfProvider.getParameterName();
          request.addURLParameter(parameterName, token);
        }
      }
    }

    List<Locale> locales = request.getLocales();
    String contentType = request.getContentType();
    Charset characterEncoding = request.getCharacterEncoding();
    HTTPMethod method = request.getMethod();

    Map<String, List<String>> urlParameters = request.getParameters();
    List<FileInfo> files = request.getFiles();
    BodyPublisher bodyPublisher = BodyPublishers.noBody();
    // We'll use this to keep the java-http HTTP Request in sync
    InputStream inputStream = null;

    if (files != null && !files.isEmpty()) {
      List<MultipartFileUpload> fileUploads = files.stream()
                                                   .map(fi -> new MultipartFileUpload(fi.contentType, fi.file, fi.fileName, fi.name))
                                                   .collect(Collectors.toList());
      MultipartBodyHandler multipartBodyHandler = new MultipartBodyHandler(new Multiparts(fileUploads, requestBodyParameters));
      bodyPublisher = BodyPublishers.ofByteArray(multipartBodyHandler.getBody());
      if (contentType == null) {
        contentType = "multipart/form-data; boundary=" + multipartBodyHandler.boundary;
      }
      inputStream = new ByteArrayInputStream(multipartBodyHandler.getBody());
    } else if (body != null) {
      bodyPublisher = BodyPublishers.ofByteArray(body);
      inputStream = new ByteArrayInputStream(body);
    } else if (!requestBodyParameters.isEmpty()) {
      byte[] formBody = new FormDataBodyHandler(requestBodyParameters).getBody();
      bodyPublisher = BodyPublishers.ofByteArray(formBody);
      if (contentType == null) {
        contentType = "application/x-www-form-urlencoded";
      }
      inputStream = (new ByteArrayInputStream(formBody));
    }

    // Keep this HTTP request in sync so that we can optionally use it in various test use cases.
    if (inputStream != null) {
      request.setInputStream(inputStream);
    }

    var requestBuilder = HttpRequest.newBuilder()
                                    .method(request.getMethod().name(), bodyPublisher);

    if (!locales.isEmpty()) {
      requestBuilder.setHeader("Accept-Language", locales.stream().map(Locale::toLanguageTag).collect(Collectors.joining(", ")));
    }

    if (contentType != null) {
      requestBuilder.setHeader(HTTPValues.Headers.ContentType, contentType + (characterEncoding != null ? "; charset=" + characterEncoding : ""));

      // Keep this HTTP request in sync so that we can optionally use it in various test use cases.
      request.setContentType(contentType);
    }

    // Copy over headers
    request.getHeaders().forEach((name, values) ->
                                     values.forEach(value -> requestBuilder.setHeader(name, value)));

    // Set cookies
    if (request.getHeaders().keySet().stream().noneMatch(name -> name.equalsIgnoreCase(HTTPValues.Headers.Cookie)) && !request.getCookies().isEmpty()) {
      String header = request.getCookies()
                             .stream()
                             .map(io.fusionauth.http.Cookie::toRequestHeader)
                             .collect(Collectors.joining("; "));
      requestBuilder.setHeader(HTTPValues.Headers.Cookie, header);
    }

    // Set the UserAgent header if not already set
    // - Do this because the default UserAgent string will include the Java version string which is annoying to update in tests.
    if (request.getHeaders().keySet().stream().noneMatch(name -> name.equalsIgnoreCase(HTTPValues.Headers.UserAgent))) {
      requestBuilder.setHeader(HTTPValues.Headers.UserAgent, "Java HttpClient");
    }

    QueryStringBuilder queryStringBuilder = QueryStringBuilder.builder();
    urlParameters.forEach((name, values) -> values.forEach(value -> queryStringBuilder.with(name, value)));

    URI fullURI = urlParameters.isEmpty()
        ? requestURI
        : URI.create(requestURI + "?" + queryStringBuilder.build());

    requestBuilder.uri(fullURI);

    HTTPResponseWrapper result = new HTTPResponseWrapper();
    try {
      result.response = HttpClientInstance.send(requestBuilder.build(),
                                                BodyHandlers.ofByteArray());
    } catch (Exception e) {
      result.exception = e;
      result.init();
      return result;
    }

    // Extract the cookies and put them in the UserAgent
    userAgent.addCookies(getCookies(result.response)
                             .stream()
                             .map(c -> new Cookie().with(c1 -> c1.domain = c.domain)
                                                   .with(c1 -> c1.expires = c.expires)
                                                   .with(c1 -> c1.httpOnly = c.httpOnly)
                                                   .with(c1 -> c1.maxAge = c.maxAge)
                                                   .with(c1 -> c1.name = c.name)
                                                   .with(c1 -> c1.path = c.path)
                                                   .with(c1 -> c1.sameSite = c.sameSite != null ? Cookie.SameSite.valueOf(c.sameSite.name()) : null)
                                                   .with(c1 -> c1.secure = c.secure).with(c1 -> c1.value = c.value))
                             .collect(Collectors.toList()));

    result.init();
    return result;
  }

  private List<io.fusionauth.http.Cookie> getCookies(HttpResponse<byte[]> response) {
    List<String> cookies = response.headers().allValues(HTTPValues.Headers.SetCookie.toLowerCase());
    if (cookies != null && !cookies.isEmpty()) {
      return cookies.stream().map(io.fusionauth.http.Cookie::fromResponseHeader).filter(Objects::nonNull).collect(Collectors.toList());
    }

    return List.of();
  }

  private void setRequestBodyParameter(String name, Object value) {
    List<String> values = new ArrayList<>();
    values.add(value.toString());
    requestBodyParameters.put(name, values);
  }
}

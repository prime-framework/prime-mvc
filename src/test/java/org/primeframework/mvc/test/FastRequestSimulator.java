/*
 * Copyright (c) 2025, Inversoft Inc., All Rights Reserved
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

import javax.net.ssl.SSLSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.inject.Injector;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.HTTPValues.Headers;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mock.MockUserAgent;
import org.primeframework.mvc.BasePrimeMain;
import org.primeframework.mvc.PrimeMVCRequestHandler;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.message.TestMessageObserver;

public class FastRequestSimulator extends RequestSimulator {
  private final PrimeMVCRequestHandler handler;

  public FastRequestSimulator(BasePrimeMain main, TestMessageObserver messageObserver) {
    super(main, messageObserver);
    handler = new PrimeMVCRequestHandler(getInjector());
  }

  @Override
  public RequestBuilder test(String path) {
    actualPort = useTLS ? tlsPort : port;
    RequestBuilder builder = new FastRequestBuilder(path, main.getInjector(), userAgent,
                                                    messageObserver, actualPort,
                                                    handler);
    builder.useTLS = useTLS;
    return builder;
  }

  private static class FastRequestBuilder extends RequestBuilder {
    private final PrimeMVCRequestHandler handler;

    public FastRequestBuilder(String path, Injector injector, MockUserAgent userAgent,
                              TestMessageObserver messageObserver, int port, PrimeMVCRequestHandler handler) {
      super(path, injector, userAgent, messageObserver, port);
      this.handler = handler;
    }

    private static Map<String, List<String>> getHeadersWithoutCookie(Map<String, List<String>> javaHttpHeaders) {
      return javaHttpHeaders.entrySet()
                            .stream()
                            .filter(kv -> !kv.getKey().equalsIgnoreCase("cookie"))
                            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private static List<Cookie> getParsedCookies(Map<String, List<String>> javaHttpHeaders) {
      List<String> rawCookies = javaHttpHeaders.get("Cookie");
      return Optional.ofNullable(rawCookies)
                     .orElse(List.of())
                     .stream()
                     .flatMap(c -> Cookie.fromRequestHeader(c).stream())
                     .toList();
    }

    @Override
    protected HttpResponse<byte[]> executeHttpRequest(HttpRequest javaHttpRequest) throws IOException, InterruptedException {
      var primeHttpRequest = HTTPObjectsHolder.getRequest();
      populatePrimeHttpRequest(javaHttpRequest, primeHttpRequest);

      HTTPObjectsHolder.clearResponse();
      // we want to use a simpler stream
      ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
      HTTPResponse primeHttpResponse = new HTTPResponse(responseStream, primeHttpRequest);
      HTTPObjectsHolder.setResponse(primeHttpResponse);

      // may not need this, working around ThreadLocal
      Thread requestThread = new Thread(() -> handler.handle(primeHttpRequest, primeHttpResponse));
      requestThread.start();
      requestThread.join();

      return new ResponseWrapper(primeHttpResponse, javaHttpRequest, responseStream);
    }

    private void populatePrimeHttpRequest(HttpRequest javaHttpRequest, HTTPRequest primeHttpRequest) {
      Map<String, List<String>> javaHttpHeaders = javaHttpRequest.headers().map();
      Map<String, List<String>> headersWithoutCookie = getHeadersWithoutCookie(javaHttpHeaders);
      primeHttpRequest.setHeaders(headersWithoutCookie);
      List<Cookie> parsedCookies = getParsedCookies(javaHttpHeaders);
      primeHttpRequest.addCookies(parsedCookies);
      primeHttpRequest.setMethod(HTTPMethod.of(javaHttpRequest.method()));
      URI uri = javaHttpRequest.uri();
      primeHttpRequest.setHost(uri.getHost());
      primeHttpRequest.setPath(uri.getPath());
    }
  }

  private static class ResponseWrapper implements HttpResponse<byte[]> {
    private final HTTPResponse primeMvcResponse;

    private final HttpRequest request;

    private final ByteArrayOutputStream responseStream;

    private ResponseWrapper(HTTPResponse primeMvcResponse, HttpRequest request, ByteArrayOutputStream responseStream) {
      this.primeMvcResponse = primeMvcResponse;
      this.request = request;
      this.responseStream = responseStream;
    }

    @Override
    public byte[] body() {
      return responseStream.toByteArray();
    }

    @Override
    public HttpHeaders headers() {
      Map<String, List<String>> headersMap = primeMvcResponse.getHeadersMap();
      // cookies are not in headers yet so we need to put them in there
      headersMap.put(Headers.SetCookie, primeMvcResponse.getCookies().stream().map(Cookie::toResponseHeader).toList());
      return HttpHeaders.of(headersMap, (a, b) -> true);
    }

    @Override
    public Optional<HttpResponse<byte[]>> previousResponse() {
      return Optional.empty();
    }

    @Override
    public HttpRequest request() {
      return request;
    }

    @Override
    public Optional<SSLSession> sslSession() {
      return Optional.empty();
    }

    @Override
    public int statusCode() {
      return primeMvcResponse.getStatus();
    }

    @Override
    public URI uri() {
      return request.uri();
    }

    @Override
    public Version version() {
      return Version.HTTP_1_1;
    }
  }
}

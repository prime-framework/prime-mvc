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
import java.util.Optional;

import com.google.inject.Injector;
import io.fusionauth.http.HTTPMethod;
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

    @Override
    protected HttpResponse<byte[]> executeHttpRequest(HttpRequest request) throws IOException, InterruptedException {
      var realRequest = HTTPObjectsHolder.getRequest();
      HTTPObjectsHolder.clearResponse();
      ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
      HTTPObjectsHolder.setResponse(new HTTPResponse(responseStream, realRequest));
      realRequest.setHeaders(request.headers().map());
      realRequest.setMethod(HTTPMethod.of(request.method()));
      URI uri = request.uri();
      realRequest.setHost(uri.getHost());
      realRequest.setPath(uri.getPath());
      // inputStream is already set
      HTTPResponse realResponse = HTTPObjectsHolder.getResponse();
      // may not need this, working around ThreadLocal
      Thread requestThread = new Thread(() -> handler.handle(realRequest, realResponse));
      requestThread.start();
      requestThread.join();
      realResponse.close();
      return new ResponseWrapper(realResponse, request, responseStream);
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
      return HttpHeaders.of(primeMvcResponse.getHeadersMap(), (a, b) -> true);
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

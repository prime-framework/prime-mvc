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
package org.primeframework.mvc.validation.jsr303.validator;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import static org.testng.Assert.*;

/**
 * @author James Humphrey
 */
public class URLConnectivityValidatorTest extends BaseValidationTest {

  public static final int port = 6000;
  private static final String url = "http://localhost:" + port;
  
  @Test
  public void validWithNull() {
    URLConnectivityValidator validator = new URLConnectivityValidator();
    assertTrue(validator.isValid(null, null));
  }

  @Test
  public void validWithEmptyString() {
    URLConnectivityValidator validator = new URLConnectivityValidator();
    assertTrue(validator.isValid(null, null));
  }

  @Test
  public void validWithMalformedURL() {
    URLConnectivityValidator validator = new URLConnectivityValidator();
    assertTrue(validator.isValid("foo", null));
  }

  @Test
  public void invalidURLConnectivity() {
    URLConnectivityValidator validator = new URLConnectivityValidator();
    assertFalse(validator.isValid(url, null));
  }

  @Test
  public void validURLConnectivity() throws IOException {
    
    MyHttpHandler handler = new MyHttpHandler(null, "GET", new ArrayList<String>(), 200, null);
    HttpServer httpServer =  startServer(port, "/", handler);

    try {
      URLConnectivityValidator validator = new URLConnectivityValidator();
      assertTrue(validator.isValid(url, null));
    } finally {
      httpServer.stop(0);
    }
  }

  /**
   * Makes an HTTP server.
   *
   * @param port    The port to use.
   * @param uri     The URI.
   * @param handler The HttpHandler to use with the server.
   * @return The server.
   * @throws IOException If the server creation failed.
   */
  protected HttpServer startServer(int port, String uri, MyHttpHandler handler) throws IOException {
    InetSocketAddress addr = new InetSocketAddress(port);
    HttpServer server = HttpServer.create(addr, 0);
    server.createContext(uri, handler);
    server.start();
    return server;
  }

  protected static class MyHttpHandler implements HttpHandler {
    public String contentType;
    public String method;

    public List<String> bodies;
    public int responseCode;
    public byte[] response;
    public int count;

    public MyHttpHandler(String contentType, String method, List<String> bodies, int responseCode, byte[] response) {
      this.contentType = contentType;
      this.method = method;
      this.bodies = bodies;
      this.responseCode = responseCode;
      this.response = response;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
      if (contentType != null) {
        assertEquals(httpExchange.getRequestHeaders().get("Content-Type").get(0), contentType);
      }

      assertEquals(httpExchange.getRequestMethod(), method);

      // Read the request and save it
      String requestBody = IOUtils.toString(httpExchange.getRequestBody(), "UTF-8");
      bodies.add(requestBody);

      httpExchange.sendResponseHeaders(responseCode, response == null ? 0 : response.length);

      if (response != null) {
        httpExchange.getResponseBody().write(response);
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
      }

      count++;
    }
  }
}
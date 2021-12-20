/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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
 *
 */
package org.primeframework.mvc.http;

import java.net.URI;
import java.util.List;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * HTTPTools test.
 *
 * @author James Humphrey
 */
public class HTTPToolsTest {
  @Test
  public void buildBaseURI() {
    HTTPRequest req = new DefaultHTTPRequest().with(r -> r.host = "www.example.com")
                                              .with(r -> r.path = "/foo/bar")
                                              .with(r -> r.port = 9011)
                                              .with(r -> r.scheme = "http");
    URI uri = HTTPTools.getBaseURI(req);
    assertEquals(uri.toString(), "http://www.example.com:9011");

    // http w/ port 80
    req = new DefaultHTTPRequest().with(r -> r.host = "www.example.com")
                                  .with(r -> r.path = "/foo/bar")
                                  .with(r -> r.port = 80)
                                  .with(r -> r.scheme = "http");
    uri = HTTPTools.getBaseURI(req);
    assertEquals(uri.toString(), "http://www.example.com");

    // http w/ port 80 behind an https proxy
    req = new DefaultHTTPRequest().with(r -> r.headers.put("X-Forwarded-Proto", List.of("https")))
                                  .with(r -> r.host = "www.example.com")
                                  .with(r -> r.path = "/foo/bar")
                                  .with(r -> r.port = 443)
                                  .with(r -> r.scheme = "http");
    uri = HTTPTools.getBaseURI(req);
    assertEquals(uri.toString(), "https://www.example.com");

    // https w/ port 443 behind an http proxy
    req = new DefaultHTTPRequest().with(r -> r.headers.put("X-Forwarded-Proto", List.of("http")))
                                  .with(r -> r.host = "www.example.com")
                                  .with(r -> r.path = "/foo/bar")
                                  .with(r -> r.port = 443)
                                  .with(r -> r.scheme = "https");
    uri = HTTPTools.getBaseURI(req);
    assertEquals(uri.toString(), "http://www.example.com:443");

    // https w/ port 443 behind an https proxy
    req = new DefaultHTTPRequest().with(r -> r.headers.put("X-Forwarded-Proto", List.of("https")))
                                  .with(r -> r.host = "www.example.com")
                                  .with(r -> r.path = "/foo/bar")
                                  .with(r -> r.port = 443)
                                  .with(r -> r.scheme = "https");
    uri = HTTPTools.getBaseURI(req);
    assertEquals(uri.toString(), "https://www.example.com");

    // https w/ port 80
    req = new DefaultHTTPRequest().with(r -> r.host = "www.example.com")
                                  .with(r -> r.path = "/foo/bar")
                                  .with(r -> r.port = 80)
                                  .with(r -> r.scheme = "https");
    uri = HTTPTools.getBaseURI(req);
    assertEquals(uri.toString(), "https://www.example.com:80");

    // https w/ port 443
    req = new DefaultHTTPRequest().with(r -> r.host = "www.example.com")
                                  .with(r -> r.path = "/foo/bar")
                                  .with(r -> r.port = 443)
                                  .with(r -> r.scheme = "https");
    uri = HTTPTools.getBaseURI(req);
    assertEquals(uri.toString(), "https://www.example.com");

    req = new DefaultHTTPRequest().with(r -> r.headers.put("X-Forwarded-Host", List.of("foobar.com")))
                                  .with(r -> r.host = "www.example.com")
                                  .with(r -> r.path = "/foo/bar")
                                  .with(r -> r.port = 443)
                                  .with(r -> r.scheme = "https");
    uri = HTTPTools.getBaseURI(req);
    assertEquals(uri.toString(), "https://foobar.com");

    req = new DefaultHTTPRequest().with(r -> r.headers.put("X-Forwarded-Host", List.of("foobar.com")))
                                  .with(r -> r.headers.put("X-Forwarded-Proto", List.of("http")))
                                  .with(r -> r.host = "www.example.com")
                                  .with(r -> r.path = "/foo/bar")
                                  .with(r -> r.port = 443)
                                  .with(r -> r.scheme = "https");
    uri = HTTPTools.getBaseURI(req);
    assertEquals(uri.toString(), "http://foobar.com:443");

    req = new DefaultHTTPRequest().with(r -> r.headers.put("X-Forwarded-Host", List.of("foobar.com")))
                                  .with(r -> r.headers.put("X-Forwarded-Proto", List.of("http")))
                                  .with(r -> r.headers.put("X-Forwarded-Port", List.of("80")))
                                  .with(r -> r.host = "www.example.com")
                                  .with(r -> r.path = "/foo/bar")
                                  .with(r -> r.port = 443)
                                  .with(r -> r.scheme = "https");
    uri = HTTPTools.getBaseURI(req);
    assertEquals(uri.toString(), "http://foobar.com");
  }
}

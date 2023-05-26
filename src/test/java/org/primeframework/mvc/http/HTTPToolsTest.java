/*
 * Copyright (c) 2001-2023, Inversoft Inc., All Rights Reserved
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

import io.fusionauth.http.server.HTTPRequest;
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
    HTTPRequest req = new HTTPRequest().with(r -> r.setHost("www.example.com"))
                                       .with(r -> r.setPath("/foo/bar"))
                                       .with(r -> r.setPort(9011))
                                       .with(r -> r.setScheme("http"));
    URI uri = URI.create(req.getBaseURL());
    assertEquals(uri.toString(), "http://www.example.com:9011");

    // http w/ port 80
    req = new HTTPRequest().with(r -> r.setHost("www.example.com"))
                           .with(r -> r.setPath("/foo/bar"))
                           .with(r -> r.setPort(80))
                           .with(r -> r.setScheme("http"));
    uri = URI.create(req.getBaseURL());
    assertEquals(uri.toString(), "http://www.example.com");

    // http w/ port 80 behind an https proxy
    req = new HTTPRequest().with(r -> r.addHeader("X-Forwarded-Proto", "https"))
                           .with(r -> r.setHost("www.example.com"))
                           .with(r -> r.setPath("/foo/bar"))
                           .with(r -> r.setPort(443))
                           .with(r -> r.setScheme("http"));
    uri = URI.create(req.getBaseURL());
    assertEquals(uri.toString(), "https://www.example.com");

    // https w/ port 443 behind an http proxy
    req = new HTTPRequest().with(r -> r.addHeader("X-Forwarded-Proto", "http"))
                           .with(r -> r.setHost("www.example.com"))
                           .with(r -> r.setPath("/foo/bar"))
                           .with(r -> r.setPort(443))
                           .with(r -> r.setScheme("https"));
    uri = URI.create(req.getBaseURL());
    assertEquals(uri.toString(), "http://www.example.com:443");

    // https w/ port 443 behind an https proxy
    req = new HTTPRequest().with(r -> r.addHeader("X-Forwarded-Proto", "https"))
                           .with(r -> r.setHost("www.example.com"))
                           .with(r -> r.setPath("/foo/bar"))
                           .with(r -> r.setPort(443))
                           .with(r -> r.setScheme("https"));
    uri = URI.create(req.getBaseURL());
    assertEquals(uri.toString(), "https://www.example.com");

    // https w/ port 80
    req = new HTTPRequest().with(r -> r.setHost("www.example.com"))
                           .with(r -> r.setPath("/foo/bar"))
                           .with(r -> r.setPort(80))
                           .with(r -> r.setScheme("https"));
    uri = URI.create(req.getBaseURL());
    assertEquals(uri.toString(), "https://www.example.com:80");

    // https w/ port 443
    req = new HTTPRequest().with(r -> r.setHost("www.example.com"))
                           .with(r -> r.setPath("/foo/bar"))
                           .with(r -> r.setPort(443))
                           .with(r -> r.setScheme("https"));
    uri = URI.create(req.getBaseURL());
    assertEquals(uri.toString(), "https://www.example.com");

    req = new HTTPRequest().with(r -> r.addHeader("X-Forwarded-Host", "foobar.com"))
                           .with(r -> r.setHost("www.example.com"))
                           .with(r -> r.setPath("/foo/bar"))
                           .with(r -> r.setPort(443))
                           .with(r -> r.setScheme("https"));
    uri = URI.create(req.getBaseURL());
    assertEquals(uri.toString(), "https://foobar.com");

    req = new HTTPRequest().with(r -> r.addHeader("X-Forwarded-Host", "foobar.com"))
                           .with(r -> r.addHeader("X-Forwarded-Proto", "http"))
                           .with(r -> r.setHost("www.example.com"))
                           .with(r -> r.setPath("/foo/bar"))
                           .with(r -> r.setPort(443))
                           .with(r -> r.setScheme("https"));
    uri = URI.create(req.getBaseURL());
    assertEquals(uri.toString(), "http://foobar.com:443");

    req = new HTTPRequest().with(r -> r.addHeader("X-Forwarded-Host", "foobar.com"))
                           .with(r -> r.addHeader("X-Forwarded-Proto", "http"))
                           .with(r -> r.addHeader("X-Forwarded-port", "80"))
                           .with(r -> r.setHost("www.example.com"))
                           .with(r -> r.setPath("/foo/bar"))
                           .with(r -> r.setPort(443))
                           .with(r -> r.setScheme("https"));
    uri = URI.create(req.getBaseURL());
    assertEquals(uri.toString(), "http://foobar.com");
  }

  @Test
  public void getRequestURI() {
    // Bad
    assertBadRequestURI("../../../foo.png", "foo.png");
    assertBadRequestURI("/../../../foo.png", "/foo.png");
    assertBadRequestURI("/css/../../../foo.png", "/css/foo.png");
    assertBadRequestURI("../css/../../../foo.png", "css/foo.png");
    assertBadRequestURI("/../css/../../../foo.png", "/css/foo.png");

    assertBadRequestURI("/a/./.../....//b/foo.png", "/a/b/foo.png");
    assertBadRequestURI(".../....///.../....///.../....///.../....///.../....///.../....//foo.png", "/foo.png");
    assertBadRequestURI("...foo.png", "foo.png");
    assertBadRequestURI("..../foo.png", "foo.png");
    assertBadRequestURI("/....foo.png", "foo.png");
    assertBadRequestURI("/..../foo.png", "/foo.png");

    // Ok
    assertOkRequestURI("/.well-known/openid-configuration");
    assertOkRequestURI("/foo/.well-known/openid-configuration");
    assertOkRequestURI("/foo/.well-known/.openid-configuration");
  }

  private void assertBadRequestURI(String path, String expectedResult) {
    assertEquals(HTTPTools.getRequestURI(new HTTPRequest().with(r -> r.setPath(path))), expectedResult);
  }

  private void assertOkRequestURI(String path) {
    assertEquals(HTTPTools.getRequestURI(new HTTPRequest().with(r -> r.setPath(path))), path);
  }
}

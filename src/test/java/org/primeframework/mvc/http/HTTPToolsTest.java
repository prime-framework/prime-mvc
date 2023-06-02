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
import static org.testng.Assert.assertNull;

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
    // Not ideal, but not escaping root. These are literal equivalent paths that do not go back further than they go forward.
    assertBadRequestURI("/foo/../foo.png", "/foo.png");
    assertBadRequestURI("/./foo/../foo.png", "/foo.png");
    assertBadRequestURI("/././foo.png", "/foo.png");

    assertBadRequestURI("/foo/bar/../../foo.png", "/foo.png");
    assertBadRequestURI("/foo/bar/../.././foo.png", "/foo.png");
    assertBadRequestURI("/foo/bar/././foo.png", "/foo/bar/foo.png");

    // Not root escaped, but with encoded dots, should interpret the same as a dot. %2E
    // - https://www.rfc-editor.org/rfc/rfc3986#section-2.3
    //
    // > For consistency, percent-encoded octets in the ranges of ALPHA
    //   (%41-%5A and %61-%7A), DIGIT (%30-%39), hyphen (%2D), period (%2E),
    //   underscore (%5F), or tilde (%7E) should not be created by URI
    //   producers and, when found in a URI, should be decoded to their
    //   corresponding unreserved characters by URI normalizers.
    //
    // This thread https://stackoverflow.com/a/24867425/3892636 quotes the above and interprets this to mean
    // an encoded dot (.) - %2E should be treated as the literal character.

    assertBadRequestURI("/foo/%2E%2E/foo%2Epng", "/foo.png");
    assertBadRequestURI("/%2E/foo/%2E%2E/foo%2Epng", "/foo.png");

    // Bad, escaping, we will return null for these values when sanitized.
    assertEscapedURI("/../foo.png");
    assertEscapedURI("../../../foo.png");
    assertEscapedURI("/../../../foo.png");
    assertEscapedURI("/css/../../../foo.png");
    assertEscapedURI("../css/../../../foo.png");
    assertEscapedURI("/../css/../../../foo.png");

    // Lots of dots! But this ok as long as they aren't double dots. You can make a directory named '...'
    assertBadRequestURI("/a/./.../....//b/foo.png", "/a/.../..../b/foo.png");
    assertBadRequestURI(".../....///.../....///.../....///.../....///.../....///.../....//foo.png", ".../..../.../..../.../..../.../..../.../..../.../..../foo.png");
    assertBadRequestURI("...foo.png", "...foo.png");

    // Strange, but ok.
    assertOkRequestURI("..../foo.png");
    assertOkRequestURI("/....foo.png");
    assertOkRequestURI("/..../foo.png");
    assertOkRequestURI("/..../foo.png..");
    assertOkRequestURI("/..../foo.png...");

    // Ok. These have a dot, but are valid.
    assertOkRequestURI("/.well-known/openid-configuration");
    assertOkRequestURI("/foo/.well-known/openid-configuration");
    assertOkRequestURI("/foo/.well-known/.openid-configuration");

    // Lots of dot separators in a fileName are ok
    assertOkRequestURI("/foo/version/1.0/foo.png");
    assertOkRequestURI("/foo/version/1.0.x/foo.png");
    assertOkRequestURI("/foo/version/1.2/foo.png");
    assertOkRequestURI("/foo/version/1.2.0/foo.png");
    assertOkRequestURI("/foo/version/1.2.0.x/foo.png");
    assertOkRequestURI("/foo/version/1.2.0.1.9.2.22.1.x/foo.png");
    assertOkRequestURI("/foo/version/1.2.0.1.9.2.22.1.x./foo.png");
    assertOkRequestURI("/foo/version/1.2.0.1.9.2.22.1.x./foo..png");
    assertOkRequestURI("/foo/version/1.2.0.1.9.2.22.1.x./foo....png");
    assertOkRequestURI("/foo/version/1.2.0.1.9.2.22.1.x./foo.......png");
    assertOkRequestURI("/f.o.o/b.a.r/1.2.0.1.9.2.22.1.x./f.o.o.png");
  }

  private void assertBadRequestURI(String path, String expectedResult) {
    assertEquals(HTTPTools.sanitizeURI(HTTPTools.getRequestURI(new HTTPRequest().with(r -> r.setPath(path)))), expectedResult);
  }

  private void assertBadURI(String path) {
    assertNull(HTTPTools.sanitizeURI(HTTPTools.getRequestURI(new HTTPRequest().with(r -> r.setPath(path)))));
  }

  private void assertEscapedURI(String path) {
    assertNull(HTTPTools.sanitizeURI(HTTPTools.getRequestURI(new HTTPRequest().with(r -> r.setPath(path)))));
  }

  private void assertOkRequestURI(String path) {
    assertEquals(HTTPTools.sanitizeURI(HTTPTools.getRequestURI(new HTTPRequest().with(r -> r.setPath(path)))), path);
  }
}

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
package org.primeframework.mvc.servlet;

import java.net.URI;

import org.primeframework.mock.servlet.MockContainer;
import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * ServletTools test.
 *
 * @author James Humphrey
 */
public class ServletToolsTest {
//  @Test
//  public void buildBaseUrlWithPortZero() {
//    HttpServletRequest req = createStrictMock(HttpServletRequest.class);
//    expect(req.getScheme()).andReturn("http");
//    expect(req.getServerName()).andReturn("www.Inversoft Inc.");
//    expect(req.getServerPort()).andReturn(0);
//    replay(req);
//
//    URL url = ServletTools.getBaseUrl(req);
//
//    assertEquals(url.toString(), "http://www.Inversoft Inc./");
//  }
//
//  @Test
//  public void buildBaseUrlWithPort() {
//    HttpServletRequest req = createStrictMock(HttpServletRequest.class);
//    expect(req.getScheme()).andReturn("http");
//    expect(req.getServerName()).andReturn("www.inversoft.com");
//    expect(req.getServerPort()).andReturn(8080);
//    replay(req);
//
//    URL url = ServletTools.getBaseUrl(req);
//
//    assertEquals(url.toString(), "http://www.inversoft.com:8080/");
//  }

  @Test
  public void buildBaseURI() {
    MockContainer container = new MockContainer();
    MockHttpServletRequest req = container.newServletRequest("http://www.example.com:9011/foo/bar");
    req.setScheme("http");
    req.setServerPort(9011);
    req.setServerName("www.example.com");

    URI uri = ServletTools.getBaseURI(req);
    assertEquals(uri.toString(), "http://www.example.com:9011");

    // http w/ port 80
    req.setScheme("http");
    req.setServerPort(80);
    uri = ServletTools.getBaseURI(req);
    assertEquals(uri.toString(), "http://www.example.com");

    // http w/ port 80 behind an https proxy
    req.setScheme("http");
    req.setServerPort(80);
    req.addHeader("X-Forwarded-Proto", "https");
    uri = ServletTools.getBaseURI(req);
    assertEquals(uri.toString(), "https://www.example.com");

    // Reset header
    req.removeHeader("X-Forwarded-Proto");

    // https w/ port 443 behind an http proxy
    req.setScheme("https");
    req.setServerPort(443);
    req.addHeader("X-Forwarded-Proto", "http");
    uri = ServletTools.getBaseURI(req);
    assertEquals(uri.toString(), "http://www.example.com:443");

    // Reset header
    req.removeHeader("X-Forwarded-Proto");

    // https w/ port 443 behind an https proxy
    req.setScheme("https");
    req.setServerPort(443);
    req.addHeader("X-Forwarded-Proto", "https");
    uri = ServletTools.getBaseURI(req);
    assertEquals(uri.toString(), "https://www.example.com");

    // Reset header
    req.removeHeader("X-Forwarded-Proto");

    // https w/ port 80
    req.setScheme("https");
    req.setServerPort(80);
    uri = ServletTools.getBaseURI(req);
    assertEquals(uri.toString(), "https://www.example.com:80");

    // https w/ port 443
    req.setScheme("https");
    req.setServerPort(443);
    uri = ServletTools.getBaseURI(req);
    assertEquals(uri.toString(), "https://www.example.com");

    req.addHeader("X-Forwarded-Host", "foobar.com");
    uri = ServletTools.getBaseURI(req);
    assertEquals(uri.toString(), "https://foobar.com");

    req.addHeader("X-Forwarded-Proto", "http");
    uri = ServletTools.getBaseURI(req);
    assertEquals(uri.toString(), "http://foobar.com:443");

    req.addHeader("X-Forwarded-Port", "80");
    uri = ServletTools.getBaseURI(req);
    assertEquals(uri.toString(), "http://foobar.com");
  }

  @Test
  public void requestURI() {
    MockContainer container = new MockContainer();
    MockHttpServletRequest request = container.newServletRequest("/login;jsessionid=C35A2D9557C051F2854845305B1AB911");
    assertEquals(ServletTools.getRequestURI(request), "/login");

    request = container.newServletRequest("/;jsessionid=C35A2D9557C051F2854845305B1AB911");
    assertEquals(ServletTools.getRequestURI(request), "/");
  }

  @Test
  public void SessionId() {
    MockContainer container = new MockContainer();
    MockHttpServletRequest request = container.newServletRequest("/login;jsessionid=C35A2D9557C051F2854845305B1AB911");
    assertEquals(ServletTools.getSessionId(request), ";jsessionid=C35A2D9557C051F2854845305B1AB911");

    request = container.newServletRequest("/;jsessionid=C35A2D9557C051F2854845305B1AB911");
    assertEquals(ServletTools.getSessionId(request), ";jsessionid=C35A2D9557C051F2854845305B1AB911");
  }
}

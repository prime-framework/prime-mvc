/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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

import javax.servlet.http.HttpServletRequest;
import java.net.URL;

import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpSession;
import org.primeframework.mock.servlet.MockServletContext;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.testng.Assert.assertEquals;

/**
 * ServletTools test.
 *
 * @author James Humphrey
 */
public class ServletToolsTest {

  @Test
  public void buildBaseUrlWithPortZero() {
    HttpServletRequest req = createStrictMock(HttpServletRequest.class);
    expect(req.getScheme()).andReturn("http");
    expect(req.getServerName()).andReturn("www.Inversoft Inc.");
    expect(req.getServerPort()).andReturn(0);
    replay(req);

    URL url = ServletTools.getBaseUrl(req);

    assertEquals(url.toString(), "http://www.Inversoft Inc./");
  }

  @Test
  public void buildBaseUrlWithPort() {
    HttpServletRequest req = createStrictMock(HttpServletRequest.class);
    expect(req.getScheme()).andReturn("http");
    expect(req.getServerName()).andReturn("www.Inversoft Inc.");
    expect(req.getServerPort()).andReturn(8080);
    replay(req);

    URL url = ServletTools.getBaseUrl(req);

    assertEquals(url.toString(), "http://www.Inversoft Inc.:8080/");
  }

  @Test
  public void requestURI() {
    MockHttpServletRequest request = new MockHttpServletRequest("/login;jsessionid=C35A2D9557C051F2854845305B1AB911", new MockHttpSession(new MockServletContext()));
    assertEquals(ServletTools.getRequestURI(request), "/login");

    request = new MockHttpServletRequest("/;jsessionid=C35A2D9557C051F2854845305B1AB911", new MockHttpSession(new MockServletContext()));
    assertEquals(ServletTools.getRequestURI(request), "/");
  }

  @Test
  public void SessionId() {
    MockHttpServletRequest request = new MockHttpServletRequest("/login;jsessionid=C35A2D9557C051F2854845305B1AB911", new MockHttpSession(new MockServletContext()));
    assertEquals(ServletTools.getSessionId(request), ";jsessionid=C35A2D9557C051F2854845305B1AB911");

    request = new MockHttpServletRequest("/;jsessionid=C35A2D9557C051F2854845305B1AB911", new MockHttpSession(new MockServletContext()));
    assertEquals(ServletTools.getSessionId(request), ";jsessionid=C35A2D9557C051F2854845305B1AB911");
  }
}

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

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User: jhumphrey Date: Apr 23, 2008
 */
public class ServletToolsTest {

  @Test
  public void testBuildBaseUrlWithPortZero() {

    HttpServletRequest req = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(req.getScheme()).andReturn("http");
    EasyMock.expect(req.getServerName()).andReturn("www.Inversoft Inc.");
    EasyMock.expect(req.getServerPort()).andReturn(0);
    EasyMock.replay(req);

    URL url = ServletTools.getBaseUrl(req);

    Assert.assertEquals("http://www.Inversoft Inc./", url.toString());
  }

  @Test
  public void testBuildBaseUrlWithPort() {

    HttpServletRequest req = EasyMock.createStrictMock(HttpServletRequest.class);
    EasyMock.expect(req.getScheme()).andReturn("http");
    EasyMock.expect(req.getServerName()).andReturn("www.Inversoft Inc.");
    EasyMock.expect(req.getServerPort()).andReturn(8080);
    EasyMock.replay(req);

    URL url = ServletTools.getBaseUrl(req);

    Assert.assertEquals("http://www.Inversoft Inc.:8080/", url.toString());
  }
}

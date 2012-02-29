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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.primeframework.mock.servlet.MockServletContext;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p> This class tests the complex methods on the mock servlet context. </p>
 *
 * @author Brian Pontarelli
 */
public class MockServletContextTest {
  @Test
  public void testResource() throws MalformedURLException {
    MockServletContext context = new MockServletContext(new File("src/java/test/unit/org/jcatapult/servlet"));
    URL url = context.getResource("/MockServletContextTest.java");
    assertNotNull(url);
    assertEquals(new File("src/java/test/unit/org/jcatapult/servlet/MockServletContextTest.java").toURI().toURL(), url);
  }

  @Test
  public void testResourcePath() throws MalformedURLException {
    MockServletContext context = new MockServletContext(new File("src/java/test/unit/org/jcatapult/servlet"));
    Set<String> urls = context.getResourcePaths("/WEB-INF/lib");
    assertNotNull(urls);
    assertTrue(urls.size() > 1);

    boolean found = false;
    for (String url : urls) {
      System.out.println("Checking URL " + url);
      found |= url.contains("java-net-commons");

      assertTrue(url.startsWith("/WEB-INF/lib"));
      assertNotNull(context.getResource(url));
      assertNotNull(context.getResourceAsStream(url));
    }

    assertTrue(found);
  }
}
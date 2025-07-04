/*
 * Copyright (c) 2022-2023, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import org.primeframework.mvc.security.MockStaticClasspathResourceFilter;
import org.primeframework.mvc.security.MockStaticResourceFilter;
import org.testng.annotations.Test;

/**
 * @author Brian Pontarelli
 */
public class StaticResourceTest extends PrimeBaseTest {
  @Test
  public void get_classpath_resolution() {
    // The default filter will deny all requests for class path resolution.
    // - Unless this exists in the configured static directory, it will always be a 404.
    simulator.test("/control-templates/button.ftl")
             .get()
             .assertStatusCode(404);

    // Allow *.ftl resources through the filter
    MockStaticClasspathResourceFilter.TestFunction = uri -> uri.endsWith(".ftl");

    simulator.test("/control-templates/button.ftl")
             .get()
             .assertStatusCode(200)
             .assertContentType("content/unknown")
             .assertBodyContains("[#ftl/]")
             .assertContentLength(368);

    // We used to ignore .class URIs by default. We still do, but no longer explicitly. The class path resolution rejects everything by default.
    // - Ensure we can't look this up w/out modifying the filter.
    simulator.test("/org/primeframework/mvc/PrimeMVCRequestHandler.class")
             .get()
             .assertStatusCode(404);

    // Allow *.class resources through the filter
    MockStaticClasspathResourceFilter.TestFunction = uri -> uri.endsWith(".class");

    // Not sure why you'd want to do this, but it is up to the user to configure the filter.
    simulator.test("/org/primeframework/mvc/PrimeMVCRequestHandler.class")
             .get()
             .assertStatusCode(200)
             .assertContentLength(3188);
  }

  @Test
  public void get_large_resource() {
    simulator.test("/css/fusionauth-style.css")
             .get()
             .assertStatusCode(200)
             .assertContentType("text/css")
             .assertContentLength(162726);
  }

  @Test
  public void get_preventEscape() {
    // Literal period
    simulator.test("/../../resources/logging.properties")
             .get()
             .assertStatusCode(404);

    // Unicode period
    simulator.test("/\u002e\u002e/\u002e\u002e/resources/logging.properties")
             .get()
             .assertStatusCode(404);
  }

  @Test
  public void get_resource() {
    simulator.test("/js/test.js")
             .get()
             .assertStatusCode(200)
             .assertContentType("text/javascript")
             .assertBodyContains("{};")
             .assertContentLength(3);

    // Disable .js resolution in the static resource directory
    MockStaticResourceFilter.TestFunction = uri -> !uri.endsWith(".js");

    simulator.test("/js/test.js")
             .get()
             .assertStatusCode(404);
  }
}

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
 */
package org.primeframework.mvc.action.config;

import javax.servlet.ServletContext;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.example.action.KitchenSink;
import org.example.action.Simple;
import org.example.action.user.Index;
import org.primeframework.mvc.action.result.ForwardResult;
import org.primeframework.mvc.action.result.RedirectResult;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.util.DefaultURIBuilder;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This class tests the default action configuration provider.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionConfigurationProviderTest {
  @Test
  public void configure() throws Exception {
    ServletContext context = EasyMock.createStrictMock(ServletContext.class);
    Capture<Map<String, ActionConfiguration>> c = new Capture<Map<String, ActionConfiguration>>();
    context.setAttribute(eq(DefaultActionConfigurationProvider.ACTION_CONFIGURATION_KEY), capture(c));
    EasyMock.replay(context);

    new DefaultActionConfigurationProvider(context, new DefaultURIBuilder());

    Map<String, ActionConfiguration> config = c.getValue();
    assertSame(config.get("/simple").actionClass, Simple.class);
    assertNotNull(config.get("/simple").annotation);
    assertEquals(config.get("/simple").executeMethods.size(), 8);
    assertEquals(config.get("/simple").executeMethods.get(HTTPMethod.GET).method, Simple.class.getMethod("execute"));
    assertEquals(config.get("/simple").executeMethods.get(HTTPMethod.POST).method, Simple.class.getMethod("execute"));
    assertEquals(config.get("/simple").executeMethods.get(HTTPMethod.PUT).method, Simple.class.getMethod("execute"));
    assertEquals(config.get("/simple").executeMethods.get(HTTPMethod.HEAD).method, Simple.class.getMethod("execute"));
    assertEquals(config.get("/simple").executeMethods.get(HTTPMethod.DELETE).method, Simple.class.getMethod("execute"));
    assertEquals(config.get("/simple").resultConfigurations.size(), 0);
    assertEquals(config.get("/simple").pattern, "");
    assertEquals(config.get("/simple").patternParts.length, 0);
    assertEquals(config.get("/simple").uri, "/simple");
    assertEquals(config.get("/simple").validationMethods.size(), 0);

    assertNotNull(config.get("/user/index"));
    assertSame(Index.class, config.get("/user/index").actionClass);
    assertEquals(config.get("/user/index").uri, "/user/index");

    assertSame(config.get("/kitchen-sink").actionClass, KitchenSink.class);
    assertNotNull(config.get("/kitchen-sink").annotation);
    assertEquals(config.get("/kitchen-sink").executeMethods.size(), 3);
    assertEquals(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.GET).method, KitchenSink.class.getMethod("get"));
    assertEquals(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.HEAD).method, KitchenSink.class.getMethod("get"));
    assertEquals(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.POST).method, KitchenSink.class.getMethod("post"));
    assertNull(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.PUT));
    assertNull(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.DELETE));
    assertEquals(config.get("/kitchen-sink").resultConfigurations.size(), 5);
    assertSame(config.get("/kitchen-sink").resultConfigurations.get("forward1").resultType, ForwardResult.class);
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward1").annotation).code(), "forward1");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward1").annotation).contentType(), "text");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward1").annotation).page(), "/WEB-INF/forward1.ftl");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward1").annotation).status(), 200);
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward1").annotation).statusStr(), "");
    assertSame(config.get("/kitchen-sink").resultConfigurations.get("forward2").resultType, ForwardResult.class);
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward2").annotation).code(), "forward2");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward2").annotation).contentType(), "bin");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward2").annotation).page(), "/WEB-INF/forward2.ftl");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward2").annotation).status(), 300);
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward2").annotation).statusStr(), "foo");
    assertSame(config.get("/kitchen-sink").resultConfigurations.get("redirect1").resultType, RedirectResult.class);
    assertEquals(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect1").annotation).code(), "redirect1");
    assertEquals(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect1").annotation).uri(), "/redirect1");
    assertTrue(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect1").annotation).perm());
    assertSame(config.get("/kitchen-sink").resultConfigurations.get("redirect2").resultType, RedirectResult.class);
    assertEquals(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect2").annotation).code(), "redirect2");
    assertEquals(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect2").annotation).uri(), "/redirect2");
    assertFalse(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect2").annotation).perm());
    assertEquals(config.get("/kitchen-sink").pattern, "{name}/{value}/static/{foo}");
    assertEquals(config.get("/kitchen-sink").patternParts.length, 4);
    assertEquals(config.get("/kitchen-sink").uri, "/kitchen-sink");
    assertEquals(config.get("/kitchen-sink").validationMethods.size(), 1);
    assertEquals(config.get("/kitchen-sink").validationMethods.get(0), KitchenSink.class.getMethod("validate"));
  }
}
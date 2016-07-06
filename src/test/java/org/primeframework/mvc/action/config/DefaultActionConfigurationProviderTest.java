/*
 * Copyright (c) 2001-2016, Inversoft Inc., All Rights Reserved
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.example.action.KitchenSink;
import org.example.action.Simple;
import org.example.action.TestAnnotation;
import org.example.action.user.Index;
import org.example.domain.UserField;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.content.binary.BinaryActionConfiguration;
import org.primeframework.mvc.content.binary.BinaryActionConfigurator;
import org.primeframework.mvc.content.json.JacksonActionConfiguration;
import org.primeframework.mvc.content.json.JacksonActionConfigurator;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.util.DefaultURIBuilder;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the default action configuration provider.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionConfigurationProviderTest {
  @Test
  public void configure() throws Exception {
    ServletContext context = EasyMock.createStrictMock(ServletContext.class);
    Capture<Map<String, ActionConfiguration>> c = new Capture<>();
    context.setAttribute(eq(DefaultActionConfigurationProvider.ACTION_CONFIGURATION_KEY), capture(c));
    EasyMock.replay(context);

    new DefaultActionConfigurationProvider(context, new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(), new BinaryActionConfigurator()))));

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
    assertNotNull(config.get("/kitchen-sink").annotations.get(TestAnnotation.class));
    assertEquals(config.get("/kitchen-sink").executeMethods.size(), 3);
    assertEquals(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.GET).method, KitchenSink.class.getMethod("get"));
    assertNotNull(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.GET).annotations.get(TestAnnotation.class));
    assertEquals(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.HEAD).method, KitchenSink.class.getMethod("get"));
    assertEquals(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.POST).method, KitchenSink.class.getMethod("post"));
    assertNull(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.PUT));
    assertNull(config.get("/kitchen-sink").executeMethods.get(HTTPMethod.DELETE));
    assertEquals(config.get("/kitchen-sink").resultConfigurations.size(), 8);
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward1")).code(), "forward1");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward1")).contentType(), "text");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward1")).page(), "/WEB-INF/forward1.ftl");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward1")).status(), 200);
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward1")).statusStr(), "");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward2")).code(), "forward2");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward2")).contentType(), "bin");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward2")).page(), "/WEB-INF/forward2.ftl");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward2")).status(), 300);
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward2")).statusStr(), "foo");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward-superclass")).code(), "forward-superclass");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward-superclass")).contentType(), "text/html; charset=UTF-8");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward-superclass")).page(), "forward-superclass.ftl");
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward-superclass")).status(), 200);
    assertEquals(((Forward) config.get("/kitchen-sink").resultConfigurations.get("forward-superclass")).statusStr(), "");
    assertEquals(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect1")).code(), "redirect1");
    assertEquals(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect1")).uri(), "/redirect1");
    assertTrue(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect1")).perm());
    assertEquals(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect2")).code(), "redirect2");
    assertEquals(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect2")).uri(), "/redirect2");
    assertFalse(((Redirect) config.get("/kitchen-sink").resultConfigurations.get("redirect2")).perm());
    assertEquals(((Status) config.get("/kitchen-sink").resultConfigurations.get("status")).code(), "status");
    assertEquals(((Status) config.get("/kitchen-sink").resultConfigurations.get("status")).status(), 300);
    assertEquals(((Status) config.get("/kitchen-sink").resultConfigurations.get("status")).statusStr(), "hello world");
    assertEquals(((Status) config.get("/kitchen-sink").resultConfigurations.get("status")).headers()[0].name(), "foo");
    assertEquals(((Status) config.get("/kitchen-sink").resultConfigurations.get("status")).headers()[0].value(), "bar");
    assertEquals(((Status) config.get("/kitchen-sink").resultConfigurations.get("status")).headers()[1].name(), "baz");
    assertEquals(((Status) config.get("/kitchen-sink").resultConfigurations.get("status")).headers()[1].value(), "fred");
    assertEquals(((JSON) config.get("/kitchen-sink").resultConfigurations.get("json")).code(), "json");
    assertEquals(((JSON) config.get("/kitchen-sink").resultConfigurations.get("json")).status(), 201);
    assertEquals(config.get("/kitchen-sink").pattern, "{name}/{value}/static/{foo}");
    assertEquals(config.get("/kitchen-sink").patternParts.length, 4);
    assertEquals(config.get("/kitchen-sink").uri, "/kitchen-sink");
    assertEquals(config.get("/kitchen-sink").preParameterMethods.size(), 1);
    assertEquals(config.get("/kitchen-sink").postParameterMethods.size(), 1);
    assertEquals(config.get("/kitchen-sink").preValidationMethods.size(), 1);
    assertEquals(config.get("/kitchen-sink").postValidationMethods.size(), 1);
    assertEquals(config.get("/kitchen-sink").validationMethods.size(), 1);
    assertEquals(config.get("/kitchen-sink").validationMethods.get(0).method, KitchenSink.class.getMethod("validate"));
    assertEquals(((JacksonActionConfiguration) config.get("/kitchen-sink").additionalConfiguration.get(JacksonActionConfiguration.class)).requestMember, "jsonRequest");
    assertEquals(((JacksonActionConfiguration) config.get("/kitchen-sink").additionalConfiguration.get(JacksonActionConfiguration.class)).requestMemberType, UserField.class);
    assertEquals(((JacksonActionConfiguration) config.get("/kitchen-sink").additionalConfiguration.get(JacksonActionConfiguration.class)).responseMember, "jsonResponse");
    assertEquals(((BinaryActionConfiguration) config.get("/kitchen-sink").additionalConfiguration.get(BinaryActionConfiguration.class)).responseMember, "binaryResponse");
    assertEquals(((BinaryActionConfiguration) config.get("/kitchen-sink").additionalConfiguration.get(BinaryActionConfiguration.class)).requestMember, "binaryRequest");

    // Verify inheritance results
    assertSame(config.get("/extension-inheritance").resultConfigurations.get("success").annotationType(), Forward.class);
  }
}
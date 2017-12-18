/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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

import java.util.Arrays;
import java.util.HashSet;

import org.example.action.KitchenSinkAction;
import org.example.action.SimpleAction;
import org.example.action.TestAnnotation;
import org.example.action.nested.FooAction;
import org.example.action.nested.FooReduxAction;
import org.example.action.nested.ParameterAction;
import org.example.action.nested.treeCollisions.SecondAction;
import org.example.action.user.IndexAction;
import org.example.domain.UserField;
import org.primeframework.mvc.action.ActionInvocation;
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

import static java.util.Collections.singletonList;
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
    DefaultActionConfigurationProvider provider = new DefaultActionConfigurationProvider
        (new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(),
            new BinaryActionConfigurator())))
    );

    ActionInvocation invocation = provider.lookup("/simple");
    assertSame(invocation.configuration.actionClass, SimpleAction.class);
    assertNotNull(invocation.configuration.annotation);
    assertEquals(invocation.configuration.executeMethods.size(), 8);
    assertEquals(invocation.configuration.executeMethods.get(HTTPMethod.GET).method, SimpleAction.class.getMethod("execute"));
    assertEquals(invocation.configuration.executeMethods.get(HTTPMethod.POST).method, SimpleAction.class.getMethod("execute"));
    assertEquals(invocation.configuration.executeMethods.get(HTTPMethod.PUT).method, SimpleAction.class.getMethod("execute"));
    assertEquals(invocation.configuration.executeMethods.get(HTTPMethod.HEAD).method, SimpleAction.class.getMethod("execute"));
    assertEquals(invocation.configuration.executeMethods.get(HTTPMethod.DELETE).method, SimpleAction.class.getMethod("execute"));
    assertEquals(invocation.configuration.resultConfigurations.size(), 0);
    assertEquals(invocation.configuration.pattern, "");
    assertEquals(invocation.configuration.patternParts.length, 0);
    assertEquals(invocation.configuration.uri, "/simple");
    assertEquals(invocation.configuration.validationMethods.size(), 0);

    invocation = provider.lookup("/simple.rss");
    assertSame(invocation.configuration.actionClass, SimpleAction.class);
    assertEquals(invocation.configuration.uri, "/simple");
    assertEquals(invocation.extension, "rss");

    invocation = provider.lookup("/simple.htm");
    assertSame(invocation.configuration.actionClass, SimpleAction.class);
    assertEquals(invocation.configuration.uri, "/simple");
    assertEquals(invocation.extension, "htm");

    invocation = provider.lookup("/user/index");
    assertSame(invocation.configuration.actionClass, IndexAction.class);
    assertEquals(invocation.configuration.uri, "/user/index");

    invocation = provider.lookup("/user/index.xml");
    assertSame(invocation.configuration.actionClass, IndexAction.class);
    assertEquals(invocation.configuration.uri, "/user/index");
    assertEquals(invocation.extension, "xml");

    invocation = provider.lookup("/kitchen-sink");
    assertSame(invocation.configuration.actionClass, KitchenSinkAction.class);
    assertNotNull(invocation.configuration.annotation);
    assertNotNull(invocation.configuration.annotations.get(TestAnnotation.class));
    assertEquals(invocation.configuration.executeMethods.size(), 3);
    assertEquals(invocation.configuration.executeMethods.get(HTTPMethod.GET).method, KitchenSinkAction.class.getMethod("get"));
    assertNotNull(invocation.configuration.executeMethods.get(HTTPMethod.GET).annotations.get(TestAnnotation.class));
    assertEquals(invocation.configuration.executeMethods.get(HTTPMethod.HEAD).method, KitchenSinkAction.class.getMethod("get"));
    assertEquals(invocation.configuration.executeMethods.get(HTTPMethod.POST).method, KitchenSinkAction.class.getMethod("post"));
    assertNull(invocation.configuration.executeMethods.get(HTTPMethod.PUT));
    assertNull(invocation.configuration.executeMethods.get(HTTPMethod.DELETE));
    assertEquals(invocation.configuration.resultConfigurations.size(), 8);
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward1")).code(), "forward1");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward1")).contentType(), "text");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward1")).page(), "/WEB-INF/forward1.ftl");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward1")).status(), 200);
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward1")).statusStr(), "");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward2")).code(), "forward2");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward2")).contentType(), "bin");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward2")).page(), "/WEB-INF/forward2.ftl");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward2")).status(), 300);
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward2")).statusStr(), "foo");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward-superclass")).code(), "forward-superclass");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward-superclass")).contentType(), "text/html; charset=UTF-8");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward-superclass")).page(), "forward-superclass.ftl");
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward-superclass")).status(), 200);
    assertEquals(((Forward) invocation.configuration.resultConfigurations.get("forward-superclass")).statusStr(), "");
    assertEquals(((Redirect) invocation.configuration.resultConfigurations.get("redirect1")).code(), "redirect1");
    assertEquals(((Redirect) invocation.configuration.resultConfigurations.get("redirect1")).uri(), "/redirect1");
    assertTrue(((Redirect) invocation.configuration.resultConfigurations.get("redirect1")).perm());
    assertEquals(((Redirect) invocation.configuration.resultConfigurations.get("redirect2")).code(), "redirect2");
    assertEquals(((Redirect) invocation.configuration.resultConfigurations.get("redirect2")).uri(), "/redirect2");
    assertFalse(((Redirect) invocation.configuration.resultConfigurations.get("redirect2")).perm());
    assertEquals(((Status) invocation.configuration.resultConfigurations.get("status")).code(), "status");
    assertEquals(((Status) invocation.configuration.resultConfigurations.get("status")).status(), 300);
    assertEquals(((Status) invocation.configuration.resultConfigurations.get("status")).statusStr(), "hello world");
    assertEquals(((Status) invocation.configuration.resultConfigurations.get("status")).headers()[0].name(), "foo");
    assertEquals(((Status) invocation.configuration.resultConfigurations.get("status")).headers()[0].value(), "bar");
    assertEquals(((Status) invocation.configuration.resultConfigurations.get("status")).headers()[1].name(), "baz");
    assertEquals(((Status) invocation.configuration.resultConfigurations.get("status")).headers()[1].value(), "fred");
    assertEquals(((JSON) invocation.configuration.resultConfigurations.get("json")).code(), "json");
    assertEquals(((JSON) invocation.configuration.resultConfigurations.get("json")).status(), 201);
    assertEquals(invocation.configuration.pattern, "{name}/{value}/static/{foo}");
    assertEquals(invocation.configuration.patternParts.length, 4);
    assertEquals(invocation.configuration.uri, "/kitchen-sink");
    assertEquals(invocation.configuration.preParameterMethods.size(), 1);
    assertEquals(invocation.configuration.postParameterMethods.size(), 1);
    assertEquals(invocation.configuration.preValidationMethods.size(), 1);
    assertEquals(invocation.configuration.postValidationMethods.size(), 1);
    assertEquals(invocation.configuration.validationMethods.size(), 1);
    assertEquals(invocation.configuration.validationMethods.get(HTTPMethod.POST).get(0).method, KitchenSinkAction.class.getMethod("validate"));
    JacksonActionConfiguration jacksonActionConfiguration = (JacksonActionConfiguration) invocation.configuration.additionalConfiguration.get(JacksonActionConfiguration.class);
    assertTrue(jacksonActionConfiguration.requestMembers.containsKey(HTTPMethod.POST));
    assertEquals(jacksonActionConfiguration.requestMembers.get(HTTPMethod.POST).name, "jsonRequest");
    assertEquals(jacksonActionConfiguration.requestMembers.get(HTTPMethod.POST).type, UserField.class);
    assertEquals(jacksonActionConfiguration.responseMember, "jsonResponse");
    assertEquals(((BinaryActionConfiguration) invocation.configuration.additionalConfiguration.get(BinaryActionConfiguration.class)).responseMember, "binaryResponse");
    assertEquals(((BinaryActionConfiguration) invocation.configuration.additionalConfiguration.get(BinaryActionConfiguration.class)).requestMember, "binaryRequest");

    // Verify inheritance results
    invocation = provider.lookup("/extension-inheritance");
    assertSame(invocation.configuration.resultConfigurations.get("success").annotationType(), Forward.class);
  }

  @Test
  public void postParametersOnly() throws Exception {
    DefaultActionConfigurationProvider provider = new DefaultActionConfigurationProvider(
        new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(),
            new BinaryActionConfigurator())))
    );

    ActionInvocation invocation = provider.lookup("/kitchen-sink/foo/bar/static/baz");
    assertSame(invocation.configuration.actionClass, KitchenSinkAction.class);
    assertEquals(invocation.uriParameters.get("name"), singletonList("foo"));
    assertEquals(invocation.uriParameters.get("value"), singletonList("bar"));
    assertEquals(invocation.uriParameters.get("foo"), singletonList("baz"));
  }

  @Test
  public void lookupPrefixParameters() throws Exception {
    DefaultActionConfigurationProvider provider = new DefaultActionConfigurationProvider(
        new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(),
            new BinaryActionConfigurator())))
    );

    ActionInvocation invocation = provider.lookup("/nested/000/preParam2/parameter/42/postParam2");
    assertEquals(invocation.configuration.actionClass, ParameterAction.class);
    assertEquals(invocation.uriParameters.size(), 4);
    assertEquals(invocation.uriParameters.get("preParam1"), singletonList("000"));
    assertEquals(invocation.uriParameters.get("preParam2"), singletonList("preParam2"));
    assertEquals(invocation.uriParameters.get("endParam1"), singletonList("42"));
    assertEquals(invocation.uriParameters.get("endParam2"), singletonList("postParam2"));
  }

  @Test
  public void lookupMultipleTreePaths() throws Exception {
    DefaultActionConfigurationProvider provider = new DefaultActionConfigurationProvider(
        new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(),
            new BinaryActionConfigurator())))
    );

    // This test is a beast. It is checking that during recursion, we are correctly cleaning up the ActionInvocation.uriParameters.
    // Essentially, the FirstAction is adding a 3 parameters as it walks the tree and calls "canHandle". These should all be removed
    // when the recursion later checks SecondAction.
    ActionInvocation invocation = provider.lookup("/nested/tree-collisions/12/second/first/bar/baz");
    assertEquals(invocation.configuration.actionClass, SecondAction.class);
    assertEquals(invocation.uriParameters.size(), 1);
    assertEquals(invocation.uriParameters.get("pre3"), singletonList("12"));
  }

  @Test
  public void index() throws Exception {
    DefaultActionConfigurationProvider provider = new DefaultActionConfigurationProvider(
        new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(),
            new BinaryActionConfigurator())))
    );

    ActionInvocation invocation = provider.lookup("/nested/one/two/index");
    assertEquals(invocation.configuration.actionClass, org.example.action.nested.IndexAction.class);
    assertEquals(invocation.uriParameters.size(), 2);
    assertEquals(invocation.uriParameters.get("param1"), singletonList("one"));
    assertEquals(invocation.uriParameters.get("param2"), singletonList("two"));

//    invocation = provider.lookup("/nested/one/two/index");
//    assertEquals(invocation.configuration.actionClass, org.example.action.nested.IndexAction.class);
//    assertEquals(invocation.uriParameters.size(), 2);
//    assertEquals(invocation.uriParameters.get("param1"), singletonList("one"));
//    assertEquals(invocation.uriParameters.get("param2"), singletonList("two"));
  }

//  @Test
//  public void indexRedirect() throws Exception {
//    MockContainer container = new MockContainer();
//    DefaultActionConfigurationProvider provider = new DefaultActionConfigurationProvider(
//        new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(), new BinaryActionConfigurator())))
//    );
//
//    ActionInvocation invocation = provider.lookup("/nested/one/two");
//    assertEquals(invocation.configuration.actionClass, org.example.action.nested.IndexAction.class);
//    assertEquals(invocation.actionURI, "/nested/one/two/");
//    assertEquals(invocation.uriParameters.size(), 2);
//    assertEquals(invocation.uriParameters.get("param1"), singletonList("one"));
//    assertEquals(invocation.uriParameters.get("param2"), singletonList("two"));
//  }

  @Test
  public void recursion() throws Exception {
    DefaultActionConfigurationProvider provider = new DefaultActionConfigurationProvider(
        new DefaultActionConfigurationBuilder(new DefaultURIBuilder(), new HashSet<>(Arrays.asList(new JacksonActionConfigurator(),
            new BinaryActionConfigurator())))
    );

    ActionInvocation invocation = provider.lookup("/nested/12/foo");
    assertEquals(invocation.configuration.actionClass, FooAction.class);
    assertEquals(invocation.uriParameters.size(), 1);
    assertEquals(invocation.uriParameters.get("fooParam"), singletonList("12"));

    invocation = provider.lookup("/nested/12/foo-redux");
    assertEquals(invocation.configuration.actionClass, FooReduxAction.class);
    assertEquals(invocation.uriParameters.size(), 1);
    assertEquals(invocation.uriParameters.get("fooParam"), singletonList("12"));
  }
}
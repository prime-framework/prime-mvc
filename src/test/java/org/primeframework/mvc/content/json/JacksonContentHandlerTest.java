/*
 * Copyright (c) 2001-2025, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPRequest;
import org.example.action.KitchenSinkAction;
import org.example.domain.UserField;
import org.example.domain.UserType;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.RequestMember;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.validation.ValidationException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests the jackson configurator test.
 *
 * @author Brian Pontarelli
 */
public class JacksonContentHandlerTest extends PrimeBaseTest {
  @Inject public ExpressionEvaluator expressionEvaluator;

  @DataProvider(name = "trueFalse")
  private static Object[][] getTrueFalse() {
    return new Object[][]{
        {true},
        {false}
    };
  }

  @Test(dataProvider = "trueFalse")
  public void enum_values(boolean nested) throws IOException {
    Map<Class<?>, Object> additionalConfig = new HashMap<>();
    Map<HTTPMethod, RequestMember> requestMembers = new HashMap<>();
    requestMembers.put(HTTPMethod.POST, new RequestMember("jsonRequest", UserField.class));
    additionalConfig.put(JacksonActionConfiguration.class, new JacksonActionConfiguration(requestMembers, null, null));

    KitchenSinkAction action = new KitchenSinkAction(null);
    ActionConfiguration config = new ActionConfiguration(KitchenSinkAction.class, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, additionalConfig, null, null, null, null, null);
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(
        new ActionInvocation(action, new ExecuteMethodConfiguration(HTTPMethod.POST, null, null), "/action", null, config));
    replay(store);

    String expected = nested ? """
        {
          "nested": {
            "fruit": "bar"
          }
        }
        """ : """
        {
          "fruit": "foo"
        }
        """;

    HTTPRequest request = new HTTPRequest();
    request.setInputStream(new ByteArrayInputStream(expected.getBytes()));
    request.setContentLength((long) expected.getBytes().length);
    request.setContentType("application/json");

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    expect(messageProvider.getMessage(eq(nested ? "[invalidEnum]nested.fruit" : "[invalidEnum]fruit"),
                                      eq(nested ? "bar" : "foo"),
                                      eq("Apple, Orange"))).andReturn("Bad value");
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR,
                                            nested ? "nested.fruit" : "fruit",
                                            nested ? "[invalidEnum]nested.fruit" : "[invalidEnum]fruit",
                                            "Bad value"));
    replay(messageStore);

    JacksonContentHandler handler = new JacksonContentHandler(request, store, new ObjectMapper(), expressionEvaluator, messageProvider, messageStore);
    try {
      handler.handle();
      fail("Should have thrown");
    } catch (ValidationException e) {
      // Expected
    }

    assertNull(action.jsonRequest);

    verify(store, messageProvider, messageStore);
  }

  @Test
  public void handle() throws IOException {
    Map<Class<?>, Object> additionalConfig = new HashMap<>();
    Map<HTTPMethod, RequestMember> requestMembers = new HashMap<>();
    requestMembers.put(HTTPMethod.POST, new RequestMember("jsonRequest", UserField.class));
    additionalConfig.put(JacksonActionConfiguration.class, new JacksonActionConfiguration(requestMembers, null, null));

    KitchenSinkAction action = new KitchenSinkAction(null);
    ActionConfiguration config = new ActionConfiguration(KitchenSinkAction.class, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, additionalConfig, null, null, null, null, null);
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, new ExecuteMethodConfiguration(HTTPMethod.POST, null, null), "/action", null, config));
    replay(store);

    String expected = "{" +
                      "  \"active\":true," +
                      "  \"addresses\":{" +
                      "    \"home\":{" +
                      "      \"city\":\"Broomfield\"," +
                      "      \"state\":\"Colorado\"," +
                      "      \"zipcode\":\"80023\"" +
                      "    }," +
                      "    \"work\":{" +
                      "      \"city\":\"Denver\"," +
                      "      \"state\":\"Colorado\"," +
                      "      \"zipcode\":\"80202\"" +
                      "    }" +
                      "  }," +
                      "  \"age\":37," +
                      "  \"favoriteMonth\":5," +
                      "  \"favoriteYear\":1976," +
                      "  \"ids\":{" +
                      "    \"0\":1," +
                      "    \"1\":2" +
                      "  }," +
                      "  \"lifeStory\":\"Hello world\"," +
                      "  \"securityQuestions\":[\"one\",\"two\",\"three\",\"four\"]," +
                      "  \"siblings\":[{" +
                      "    \"active\":false," +
                      "    \"name\":\"Brett\"" +
                      "  },{" +
                      "    \"active\":false," +
                      "    \"name\":\"Beth\"" +
                      "  }]," +
                      "  \"type\":\"COOL\"" +
                      "}";

    request.setInputStream(new ByteArrayInputStream(expected.getBytes()));
    request.setContentLength((long) expected.getBytes().length);
    request.setContentType("application/json");

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    JacksonContentHandler handler = new JacksonContentHandler(request, store, new ObjectMapper(), expressionEvaluator, messageProvider, messageStore);
    handler.handle();

    assertEquals(action.jsonRequest.addresses.get("work").city, "Denver");
    assertEquals(action.jsonRequest.addresses.get("work").state, "Colorado");
    assertEquals(action.jsonRequest.addresses.get("work").zipcode, "80202");
    assertEquals(action.jsonRequest.addresses.get("home").city, "Broomfield");
    assertEquals(action.jsonRequest.addresses.get("home").state, "Colorado");
    assertEquals(action.jsonRequest.addresses.get("home").zipcode, "80023");
    assertTrue(action.jsonRequest.active);
    assertEquals((int) action.jsonRequest.age, 37);
    assertEquals((int) action.jsonRequest.favoriteMonth, 5);
    assertEquals((int) action.jsonRequest.favoriteYear, 1976);
    assertEquals((int) action.jsonRequest.ids.get(0), 1);
    assertEquals((int) action.jsonRequest.ids.get(1), 2);
    assertEquals(action.jsonRequest.lifeStory, "Hello world");
    assertEquals(action.jsonRequest.securityQuestions, new String[]{"one", "two", "three", "four"});
    assertEquals(action.jsonRequest.siblings.get(0).name, "Brett");
    assertEquals(action.jsonRequest.siblings.get(1).name, "Beth");
    assertEquals(action.jsonRequest.type, UserType.COOL);

    verify(store, messageProvider, messageStore);
  }

  @Test
  public void handleBadInArray() throws IOException {
    Map<Class<?>, Object> additionalConfig = new HashMap<>();
    Map<HTTPMethod, RequestMember> requestMembers = new HashMap<>();
    requestMembers.put(HTTPMethod.POST, new RequestMember("jsonRequest", UserField.class));
    additionalConfig.put(JacksonActionConfiguration.class, new JacksonActionConfiguration(requestMembers, null, null));

    KitchenSinkAction action = new KitchenSinkAction(null);
    ActionConfiguration config = new ActionConfiguration(KitchenSinkAction.class, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, additionalConfig, null, null, null, null, null);
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(
        new ActionInvocation(action, new ExecuteMethodConfiguration(HTTPMethod.POST, null, null), "/action", null, config));
    replay(store);

    String expected = "{" +
                      "  \"siblings\":[{" +
                      "    \"age\":\"old\"" +
                      "  }]" +
                      "}";

    HTTPRequest request = new HTTPRequest();
    request.setInputStream(new ByteArrayInputStream(expected.getBytes()));
    request.setContentLength((long) expected.getBytes().length);
    request.setContentType("application/json");

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    expect(messageProvider.getMessage(eq("[invalidJSON]"), eq("siblings.age"), eq("Possible conversion error"), isA(String.class))).andReturn(
        "Bad sibling age");
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "siblings.age", "[invalidJSON]", "Bad sibling age"));
    replay(messageStore);

    JacksonContentHandler handler = new JacksonContentHandler(request, store, new ObjectMapper(), expressionEvaluator, messageProvider, messageStore);
    try {
      handler.handle();
      fail("Should have thrown");
    } catch (ValidationException e) {
      // Expected
    }

    assertNull(action.jsonRequest);

    verify(store, messageProvider, messageStore);
  }

  @Test
  public void handleBadInMap() throws IOException {
    Map<Class<?>, Object> additionalConfig = new HashMap<>();
    Map<HTTPMethod, RequestMember> requestMembers = new HashMap<>();
    requestMembers.put(HTTPMethod.POST, new RequestMember("jsonRequest", UserField.class));
    additionalConfig.put(JacksonActionConfiguration.class, new JacksonActionConfiguration(requestMembers, null, null));

    KitchenSinkAction action = new KitchenSinkAction(null);
    ActionConfiguration config = new ActionConfiguration(KitchenSinkAction.class, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, additionalConfig, null, null, null, null, null);
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(
        new ActionInvocation(action, new ExecuteMethodConfiguration(HTTPMethod.POST, null, null), "/action", null, config));
    replay(store);

    String expected = "{" +
                      "  \"addresses\":{" +
                      "    \"home\":{" +
                      "      \"age\":\"old\"" +
                      "    }" +
                      "  }" +
                      "}";

    HTTPRequest request = new HTTPRequest();
    request.setInputStream(new ByteArrayInputStream(expected.getBytes()));
    request.setContentLength((long) expected.getBytes().length);
    request.setContentType("application/json");

    MessageProvider messageProvider = createNiceMock(MessageProvider.class);
    expect(messageProvider.getMessage(eq("[invalidJSON]"), eq("addresses.home.age"), eq("Possible conversion error"), isA(String.class))).andReturn(
        "Bad age");
    replay(messageProvider);

    MessageStore messageStore = createNiceMock(MessageStore.class);
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "addresses.home.age", "[invalidJSON]", "Bad age"));
    replay(messageStore);

    JacksonContentHandler handler = new JacksonContentHandler(request, store, new ObjectMapper(), expressionEvaluator, messageProvider, messageStore);
    try {
      handler.handle();
      fail("Should have thrown");
    } catch (ValidationException e) {
      // Expected
    }

    assertNull(action.jsonRequest);

    verify(store, messageProvider, messageStore);
  }

  @Test
  public void handleBadJSON() throws IOException {
    Map<Class<?>, Object> additionalConfig = new HashMap<>();
    Map<HTTPMethod, RequestMember> requestMembers = new HashMap<>();
    requestMembers.put(HTTPMethod.POST, new RequestMember("jsonRequest", UserField.class));
    additionalConfig.put(JacksonActionConfiguration.class, new JacksonActionConfiguration(requestMembers, null, null));

    KitchenSinkAction action = new KitchenSinkAction(null);
    ActionConfiguration config = new ActionConfiguration(KitchenSinkAction.class, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, additionalConfig, null, null, null, null, null);
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(
        new ActionInvocation(action, new ExecuteMethodConfiguration(HTTPMethod.POST, null, null), "/action", null, config));
    replay(store);

    String expected = "{" +
                      "  \"bad-active\":true" +
                      "}";

    HTTPRequest request = new HTTPRequest();
    request.setInputStream(new ByteArrayInputStream(expected.getBytes()));
    request.setContentLength((long) expected.getBytes().length);
    request.setContentType("application/json");

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    expect(messageProvider.getMessage(eq("[invalidJSON]"), eq("bad-active"), eq("Unrecognized property"), isA(String.class))).andReturn("foo");
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(new SimpleMessage(MessageType.ERROR, "[invalidJSON]", "foo"));
    replay(messageStore);

    JacksonContentHandler handler = new JacksonContentHandler(request, store, new ObjectMapper(), expressionEvaluator, messageProvider, messageStore);
    try {
      handler.handle();
      fail("Should have thrown");
    } catch (ValidationException e) {
      // Expected
    }

    assertNull(action.jsonRequest);

    verify(store, messageProvider, messageStore);
  }

  @Test
  public void handleBadRoot() throws IOException {
    Map<Class<?>, Object> additionalConfig = new HashMap<>();
    Map<HTTPMethod, RequestMember> requestMembers = new HashMap<>();
    requestMembers.put(HTTPMethod.POST, new RequestMember("jsonRequest", UserField.class));
    additionalConfig.put(JacksonActionConfiguration.class, new JacksonActionConfiguration(requestMembers, null, null));

    KitchenSinkAction action = new KitchenSinkAction(null);
    ActionConfiguration config = new ActionConfiguration(KitchenSinkAction.class, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, additionalConfig, null, null, null, null, null);
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(
        new ActionInvocation(action, new ExecuteMethodConfiguration(HTTPMethod.POST, null, null), "/action", null, config));
    replay(store);

    String expected = "{" +
                      "  \"active\":\"bad\"" +
                      "}";

    HTTPRequest request = new HTTPRequest();
    request.setInputStream(new ByteArrayInputStream(expected.getBytes()));
    request.setContentLength((long) expected.getBytes().length);
    request.setContentType("application/json");

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    expect(messageProvider.getMessage(eq("[invalidJSON]"), eq("active"), eq("Possible conversion error"), isA(String.class))).andReturn("Bad active");
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, "active", "[invalidJSON]", "Bad active"));
    replay(messageStore);

    JacksonContentHandler handler = new JacksonContentHandler(request, store, new ObjectMapper(), expressionEvaluator, messageProvider, messageStore);
    try {
      handler.handle();
      fail("Should have thrown");
    } catch (ValidationException e) {
      // Expected
    }

    assertNull(action.jsonRequest);

    verify(store, messageProvider, messageStore);
  }

  @Test
  public void handleNoAction() throws IOException {
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(null, null, "/action", null, null));
    replay(store);

    String expected = "{" +
                      "  \"active\":true," +
                      "  \"addresses\":{" +
                      "    \"home\":{" +
                      "      \"city\":\"Broomfield\"," +
                      "      \"state\":\"Colorado\"," +
                      "      \"zipcode\":\"80023\"" +
                      "    }," +
                      "    \"work\":{" +
                      "      \"city\":\"Denver\"," +
                      "      \"state\":\"Colorado\"," +
                      "      \"zipcode\":\"80202\"" +
                      "    }" +
                      "  }," +
                      "  \"age\":37," +
                      "  \"favoriteMonth\":5," +
                      "  \"favoriteYear\":1976," +
                      "  \"ids\":{" +
                      "    \"0\":1," +
                      "    \"1\":2" +
                      "  }," +
                      "  \"lifeStory\":\"Hello world\"," +
                      "  \"securityQuestions\":[\"one\",\"two\",\"three\",\"four\"]," +
                      "  \"siblings\":[{" +
                      "    \"active\":false," +
                      "    \"name\":\"Brett\"" +
                      "  },{" +
                      "    \"active\":false," +
                      "    \"name\":\"Beth\"" +
                      "  }]," +
                      "  \"type\":\"COOL\"" +
                      "}";

    HTTPRequest request = new HTTPRequest();
    request.setInputStream(new ByteArrayInputStream(expected.getBytes()));
    request.setContentLength((long) expected.getBytes().length);
    request.setContentType("application/json");

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    JacksonContentHandler handler = new JacksonContentHandler(request, store, new ObjectMapper(), expressionEvaluator, messageProvider, messageStore);
    handler.handle();

    verify(store, messageProvider, messageStore);
  }

  @Test
  public void handleNoConfig() throws IOException {
    Map<Class<?>, Object> additionalConfig = new HashMap<>();

    KitchenSinkAction action = new KitchenSinkAction(null);
    ActionConfiguration config = new ActionConfiguration(KitchenSinkAction.class, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, additionalConfig, null, null, null, null, null);
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, null, "/action", null, config));
    replay(store);

    String expected = "{" +
                      "  \"active\":true," +
                      "  \"addresses\":{" +
                      "    \"home\":{" +
                      "      \"city\":\"Broomfield\"," +
                      "      \"state\":\"Colorado\"," +
                      "      \"zipcode\":\"80023\"" +
                      "    }," +
                      "    \"work\":{" +
                      "      \"city\":\"Denver\"," +
                      "      \"state\":\"Colorado\"," +
                      "      \"zipcode\":\"80202\"" +
                      "    }" +
                      "  }," +
                      "  \"age\":37," +
                      "  \"favoriteMonth\":5," +
                      "  \"favoriteYear\":1976," +
                      "  \"ids\":{" +
                      "    \"0\":1," +
                      "    \"1\":2" +
                      "  }," +
                      "  \"lifeStory\":\"Hello world\"," +
                      "  \"securityQuestions\":[\"one\",\"two\",\"three\",\"four\"]," +
                      "  \"siblings\":[{" +
                      "    \"active\":false," +
                      "    \"name\":\"Brett\"" +
                      "  },{" +
                      "    \"active\":false," +
                      "    \"name\":\"Beth\"" +
                      "  }]," +
                      "  \"type\":\"COOL\"" +
                      "}";

    HTTPRequest request = new HTTPRequest();
    request.setInputStream(new ByteArrayInputStream(expected.getBytes()));
    request.setContentLength((long) expected.getBytes().length);
    request.setContentType("application/json");

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    JacksonContentHandler handler = new JacksonContentHandler(request, store, new ObjectMapper(), expressionEvaluator, messageProvider, messageStore);
    handler.handle();

    assertNull(action.jsonRequest);

    verify(store, messageProvider, messageStore);
  }
}

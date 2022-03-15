/*
 * Copyright (c) 2013-2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.example.action.PostAction;
import org.example.domain.AddressField;
import org.example.domain.UserField;
import org.example.domain.UserType;
import org.primeframework.mock.servlet.MockServletOutputStream;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ExecuteMethodConfiguration;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.XMLStream;
import org.primeframework.mvc.content.json.JacksonActionConfiguration;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.ResponseMember;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * This class tests the JSON result.
 *
 * @author Brian Pontarelli
 */
public class JSONResultTest extends PrimeBaseTest {
  private static JSONResponse JSON_RESPONSE_ANNOTATION = new JSONResponse() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return null;
    }

    @Override
    public boolean prettyPrint() {
      return false;
    }

    @Override
    public Class<?> view() {
      return null;
    }
  };

  @Inject public ObjectMapper objectMapper;

  @Test(dataProvider = "httpMethod")
  public void all(HTTPMethod httpMethod) throws IOException {
    UserField userField = new UserField();
    userField.addresses.put("work", new AddressField());
    userField.addresses.get("work").age = 100;
    userField.addresses.get("work").city = "Denver";
    userField.addresses.get("work").state = "Colorado";
    userField.addresses.get("work").zipcode = "80202";
    userField.addresses.put("home", new AddressField());
    userField.addresses.get("home").age = 100;
    userField.addresses.get("home").city = "Broomfield";
    userField.addresses.get("home").state = "Colorado";
    userField.addresses.get("home").zipcode = "80023";
    userField.active = true;
    userField.age = 37;
    userField.favoriteMonth = 5;
    userField.favoriteYear = 1976;
    userField.ids.put(0, 1);
    userField.ids.put(1, 2);
    userField.lifeStory = "Hello world";
    userField.locale = Locale.US;
    userField.securityQuestions = new String[]{"one", "two", "three", "four"};
    userField.siblings.add(new UserField("Brett"));
    userField.siblings.add(new UserField("Beth"));
    userField.type = UserType.COOL;

    PostAction action = new PostAction();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.getValue("user", action)).andReturn(userField);
    replay(ee);

    MockServletOutputStream sos = new MockServletOutputStream();
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(200);
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.setContentLength(538);
    response.setHeader("Cache-Control", "no-cache");
    if (httpMethod == HTTPMethod.GET) {
      expect(response.getOutputStream()).andReturn(sos);
    }
    replay(response);

    Map<Class<?>, Object> additionalConfiguration = new HashMap<>();
    additionalConfiguration.put(JacksonActionConfiguration.class, new JacksonActionConfiguration(null, new ResponseMember(JSON_RESPONSE_ANNOTATION, "user")));
    ActionConfiguration config = new ActionConfiguration(PostAction.class, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, additionalConfiguration, null, null, null);
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, new ExecuteMethodConfiguration(httpMethod, null, null), "/foo", "", config));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(new ArrayList<>());
    replay(messageStore);

    JSON annotation = new JSONResultTest.JSONImpl("success", 200);
    JSONResult result = new JSONResult(ee, store, messageStore, objectMapper, response);
    result.execute(annotation);

    String expected = "{" +
        "  \"active\":true," +
        "  \"addresses\":{" +
        "    \"home\":{" +
        "      \"age\":100," +
        "      \"city\":\"Broomfield\"," +
        "      \"state\":\"Colorado\"," +
        "      \"zipcode\":\"80023\"" +
        "    }," +
        "    \"work\":{" +
        "      \"age\":100," +
        "      \"city\":\"Denver\"," +
        "      \"state\":\"Colorado\"," +
        "      \"zipcode\":\"80202\"" +
        "    }" +
        "  }," +
        "  \"age\":37," +
        "  \"bar\":false," +
        "  \"favoriteMonth\":5," +
        "  \"favoriteYear\":1976," +
        "  \"ids\":{" +
        "    \"0\":1," +
        "    \"1\":2" +
        "  }," +
        "  \"lifeStory\":\"Hello world\"," +
        "  \"locale\":\"en_US\"," +
        "  \"securityQuestions\":[\"one\",\"two\",\"three\",\"four\"]," +
        "  \"siblings\":[{" +
        "    \"active\":false," +
        "    \"addresses\":{}," +
        "    \"bar\":false," +
        "    \"ids\":{}," +
        "    \"name\":\"Brett\"," +
        "    \"siblings\":[]" +
        "  },{" +
        "    \"active\":false," +
        "    \"addresses\":{}," +
        "    \"bar\":false," +
        "    \"ids\":{}," +
        "    \"name\":\"Beth\"," +
        "    \"siblings\":[]" +
        "  }]," +
        "  \"type\":\"COOL\"" +
        "}";
    assertEquals(sos.toString(), httpMethod == HTTPMethod.GET ? expected.replace("  ", "") : ""); // Un-indent

    verify(ee, messageStore, response);
  }

  @Test(dataProvider = "httpMethod")
  public void errors(HTTPMethod httpMethod) throws IOException {
    PostAction action = new PostAction();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    replay(ee);

    MockServletOutputStream sos = new MockServletOutputStream();
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(400);
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.setContentLength(359);
    response.setHeader("Cache-Control", "no-cache");
    if (httpMethod == HTTPMethod.GET) {
      expect(response.getOutputStream()).andReturn(sos);
    }
    replay(response);

    Map<Class<?>, Object> additionalConfiguration = new HashMap<>();
    additionalConfiguration.put(JacksonActionConfiguration.class, new JacksonActionConfiguration(null, new ResponseMember(JSON_RESPONSE_ANNOTATION, "user")));
    ActionConfiguration config = new ActionConfiguration(PostAction.class, null, null, null, null, null, null, null, null, null, null, null, null, null, Collections.emptyList(), null, additionalConfiguration, null, null, null);
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);

    expect(store.getCurrent()).andReturn(new ActionInvocation(action, new ExecuteMethodConfiguration(httpMethod, null, null), "/foo", "", config));
    replay(store);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    expect(messageStore.get(MessageScope.REQUEST)).andReturn(asList(
        new SimpleMessage(MessageType.ERROR, "[invalid]", "Invalid request"),
        new SimpleMessage(MessageType.ERROR, "[bad]", "Bad request"),
        new SimpleFieldMessage(MessageType.ERROR, "user.age", "[required]user.age", "Age is required"),
        new SimpleFieldMessage(MessageType.ERROR, "user.age", "[number]user.age", "Age must be a number"),
        new SimpleFieldMessage(MessageType.ERROR, "user.favoriteMonth", "[required]user.favoriteMonth", "Favorite month is required")
    ));
    replay(messageStore);

    JSON annotation = new JSONResultTest.JSONImpl("input", 400);
    JSONResult result = new JSONResult(ee, store, messageStore, objectMapper, response);
    result.execute(annotation);

    String expected = "{" +
        "  \"fieldErrors\":{" +
        "    \"user.age\":[{\"code\":\"[required]user.age\",\"message\":\"Age is required\"},{\"code\":\"[number]user.age\",\"message\":\"Age must be a number\"}]," +
        "    \"user.favoriteMonth\":[{\"code\":\"[required]user.favoriteMonth\",\"message\":\"Favorite month is required\"}]" +
        "  }," +
        "  \"generalErrors\":[" +
        "    {\"code\":\"[invalid]\",\"message\":\"Invalid request\"},{\"code\":\"[bad]\",\"message\":\"Bad request\"}" +
        "  ]" +
        "}";
    assertEquals(sos.toString(), httpMethod == HTTPMethod.GET ? expected.replace("  ", "") : ""); // Un-indent

    verify(ee, messageStore, response);
  }

  @DataProvider(name = "httpMethod")
  public Object[][] httpMethod() {
    return new Object[][]{{HTTPMethod.GET}, {HTTPMethod.HEAD}};
  }

  /**
   * Using this test to ensure the JSONResult is fast enough even if we call writerWithDefaultPrettyPrinter or
   * writerWithView which construct new Object Writers.
   * <p>
   * This seems to be fast as balls, and not worth worrying about.
   * <p>
   * Enable the test and see for yourself. Hopefully the JVM isn't so smart to see that I am not storing the references
   * and then optimizing away the code.
   */
  @Test(enabled = false)
  public void objectWriter_performance() {
    long loopCount = 1_000_000;

    // Test performance of constructing a new objectMapper for prettyPrint
    Instant start = Instant.now();
    for (int i = 0; i < loopCount; i++) {
      objectMapper.writerWithDefaultPrettyPrinter();
    }
    Duration duration = Duration.between(start, Instant.now());
    double avg = duration.toMillis() / loopCount;
    System.out.println("Time: " + duration.toMillis());
    System.out.println("Each iteration: " + avg);

    // Test performance of constructing a new objectMapper for views
    start = Instant.now();
    for (int i = 0; i < loopCount; i++) {
      objectMapper.writerWithView(Object.class);
    }
    duration = Duration.between(start, Instant.now());
    avg = duration.toMillis() / loopCount;
    System.out.println("Time: " + duration.toMillis());
    System.out.println("Each iteration: " + avg);

    // Test performance of constructing a new objectMapper for views with pretty print
    start = Instant.now();
    for (int i = 0; i < loopCount; i++) {
      objectMapper.writerWithView(Object.class).withDefaultPrettyPrinter();
    }
    duration = Duration.between(start, Instant.now());
    avg = duration.toMillis() / loopCount;
    System.out.println("Time: " + duration.toMillis());
    System.out.println("Each iteration: " + avg);
  }

  public class JSONImpl implements JSON {
    private final String cacheControl;

    private final String code;

    private final String contentType;

    private final boolean disableCacheControl;

    private final int status;

    public JSONImpl(String code, int status) {
      this.contentType = "";
      this.cacheControl = "no-cache";
      this.code = code;
      this.disableCacheControl = false;
      this.status = status;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return XMLStream.class;
    }

    @Override
    public String cacheControl() {
      return cacheControl;
    }

    @Override
    public String code() {
      return code;
    }

    @Override
    public String contentType() {
      return contentType;
    }

    @Override
    public boolean disableCacheControl() {
      return disableCacheControl;
    }

    @Override
    public int status() {
      return status;
    }
  }
}

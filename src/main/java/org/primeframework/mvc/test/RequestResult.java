/*
 * Copyright (c) 2014-2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.test;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ActionMapper;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.l10n.MessageProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Injector;
import static java.util.Arrays.asList;

/**
 * Result of a request to the {@link org.primeframework.mvc.test.RequestSimulator}.
 *
 * @author Brian Pontarelli
 */
public class RequestResult {
  public final String body;

  public final Injector injector;

  public final String redirect;

  public final MockHttpServletRequest request;

  public final MockHttpServletResponse response;

  public final int statusCode;

  public RequestResult(MockHttpServletRequest request, MockHttpServletResponse response, Injector injector) {
    this.request = request;
    this.response = response;
    this.injector = injector;
    this.body = response.getStream().toString();
    this.redirect = response.getRedirect();
    this.statusCode = response.getCode();
  }

  /**
   * Compares two JSON objects to ensure they are equal. This is done by converting the JSON objects to Maps, Lists, and primitives and then
   * comparing them. The error is output so that IntelliJ can diff the two JSON objects in order to output the results.
   *
   * @param objectMapper The Jackson ObjectMapper used to convert the JSON strings to Maps.
   * @param actual       The actual JSON.
   * @param expected     The expected JSON.
   * @throws IOException If the ObjectMapper fails.
   */
  public static void assertJSONEquals(ObjectMapper objectMapper, String actual, String expected) throws IOException {
    JsonNode response = objectMapper.readTree(actual);
    JsonNode file = objectMapper.readTree(expected);

    if (!response.equals(file)) {
      objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
      String bodyString = objectMapper.writeValueAsString(response);
      String fileString = objectMapper.writeValueAsString(file);
      throw new AssertionError("The body doesn't match the expected JSON output. expected [" + fileString + "] but found [" + bodyString + "]");
    }
  }

  /**
   * Verifies that the body equals the given string.
   *
   * @param string The string to compare against the body.
   * @return This.
   */
  public RequestResult assertBody(String string) {
    if (!body.equals(string)) {
      throw new AssertionError("Body didn't match [" + string + "]\nRedirect: [" + redirect + "]\nBody:\n" + body);
    }

    return this;
  }

  /**
   * Verifies that the body contains all of the given Strings.
   *
   * @param strings The strings to check.
   * @return This.
   */
  public RequestResult assertBodyContains(String... strings) {
    for (String string : strings) {
      if (!body.contains(string)) {
        throw new AssertionError("Body didn't contain [" + string + "]\nRedirect: [" + redirect + "]\nBody:\n" + body);
      }
    }

    return this;
  }

  /**
   * Verifies that the body contains the messages from the given key and optionally provided replacement values. This
   * uses the MessageProvider for the current
   * test URI and the given keys to look up the messages.
   *
   * @param key    The key.
   * @param values The replacement values.
   * @return This.
   */
  public RequestResult assertBodyContainsMessagesFromKey(String key, Object... values) {
    MessageProvider messageProvider = get(MessageProvider.class);
    ActionInvocationStore actionInvocationStore = get(ActionInvocationStore.class);
    ActionMapper actionMapper = get(ActionMapper.class);

    // Using the ActionMapper so that URL segments are properly handled and the correct URL is used for message lookups.
    ActionInvocation actionInvocation = actionMapper.map(null, request.getRequestURI(), true);
    actionInvocationStore.setCurrent(actionInvocation);

    String message = messageProvider.getMessage(key, values);
    if (!body.contains(message)) {
      throw new AssertionError("Body didn't contain [" + message + "] for the key [" + key + "]\nRedirect: [" + redirect + "]\nBody:\n" + body);
    }

    return this;
  }

  /**
   * Verifies that the body contains the messages from the given keys. This uses the MessageProvider for the current
   * test URI and the given keys to look up the messages.
   *
   * @param keys The keys.
   * @return This.
   */
  public RequestResult assertBodyContainsMessagesFromKeys(String... keys) {
    for (String key : keys) {
      assertBodyContainsMessagesFromKey(key, "foo", "bar", "baz");
    }

    return this;
  }

  /**
   * Verifies that the body does not contain any of the given Strings.
   *
   * @param strings The strings to check.
   * @return This.
   */
  public RequestResult assertBodyDoesNotContain(String... strings) {
    for (String string : strings) {
      if (body.contains(string)) {
        throw new AssertionError("Body shouldn't contain [" + string + "]\nRedirect: [" + redirect + "]\nBody:\n" + body);
      }
    }

    return this;
  }

  /**
   * Verifies that the body equals the content of the given File.
   *
   * @param path   The file to load and compare to the response.
   * @param values key value pairs of replacement values for use in the file.
   * @return This.
   */
  public RequestResult assertBodyFile(Path path, Object... values) throws IOException {
    if (values.length == 0) {
      return assertBody(new String(Files.readAllBytes(path), "UTF-8"));
    }
    return assertBody(BodyTools.processTemplate(path, values));
  }

  /**
   * Verifies that the body is empty.
   *
   * @return This
   */
  public RequestResult assertBodyIsEmpty() {
    if (!body.isEmpty()) {
      throw new AssertionError("Body is not empty.\nBody:\n" + body);
    }

    return this;
  }

  /**
   * Assert the cookie exists by name.
   *
   * @param name The cookie name.
   * @return This.
   */
  public RequestResult assertContainsCookie(String name) {
    Cookie actual = response.getCookies().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    if (actual == null) {
      throw new AssertionError("Cookie [" + name + "] was not found in the response. Cookies found [" + String.join(", ", response.getCookies().stream().map(Cookie::getName).collect(Collectors.toList())));
    }
    return this;
  }

  /**
   * Verifies that the system contains the given error message(s). The message(s) might be in the request, flash,
   * session or application scopes.
   *
   * @param messages The fully rendered error message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsErrors(String... messages) {
    return assertContainsMessages(MessageType.ERROR, messages);
  }

  /**
   * Verifies that the system has errors for the given fields. This doesn't assert the error itself, just that the
   * field
   * contains an error.
   *
   * @param fields The name of the field code(s). Not the fully rendered message(s)
   * @return This.
   */
  public RequestResult assertContainsFieldErrors(String... fields) {
    MessageStore messageStore = get(MessageStore.class);
    Map<String, List<FieldMessage>> msgs = messageStore.getFieldMessages();
    for (String field : fields) {
      List<FieldMessage> fieldMessages = msgs.get(field);
      if (fieldMessages == null) {
        StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
        msgs.keySet().stream().forEach((f) -> sb.append("\t\t" + f + "\n"));
        throw new AssertionError("The MessageStore does not contain a error for the field [" + field + "]" + sb);
      }

      boolean found = false;
      for (FieldMessage fieldMessage : fieldMessages) {
        found |= fieldMessage.getType() == MessageType.ERROR;
      }

      if (!found) {
        StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
        fieldMessages.stream().forEach((f) -> sb.append("\t\t[" + f.getType() + "]\n"));
        throw new AssertionError("The MessageStore contains messages but no errors for the field [" + field + "]" + sb);
      }
    }

    return this;
  }

  /**
   * Verifies that the system has general errors. This doesn't assert the error itself, just that the
   * general error code.
   *
   * @param messageCodes The name of the error code(s). Not the fully rendered message(s)
   * @return This.
   */
  public RequestResult assertContainsGeneralErrorMessageCodes(String... messageCodes) {
    return assertContainsGeneralMessageCodes(MessageType.ERROR, messageCodes);
  }

  /**
   * Verifies that the system has info errors. This doesn't assert the message itself, just that the
   * general message code.
   *
   * @param messageCodes The name of the message code(s). Not the fully rendered message(s)
   * @return This.
   */
  public RequestResult assertContainsGeneralInfoMessageCodes(String... messageCodes) {
    return assertContainsGeneralMessageCodes(MessageType.INFO, messageCodes);
  }

  /**
   * Verifies that the system has general messages. This doesn't assert the message itself, just that the
   * general message code.
   *
   * @param messageType The message type
   * @param errorCodes  The name of the message code(s). Not the fully rendered message(s)
   * @return This.
   */
  public RequestResult assertContainsGeneralMessageCodes(MessageType messageType, String... errorCodes) {
    MessageStore messageStore = get(MessageStore.class);
    List<Message> messages = messageStore.getGeneralMessages();
    for (String errorCode : errorCodes) {
      Message message = messages.stream().filter((m) -> m.getCode().equals(errorCode)).findFirst().orElse(null);
      if (message == null) {
        StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
        messages.stream().forEach((m) -> sb.append("\t\t" + m.getCode() + " Type: " + m.getType() + "\n"));
        throw new AssertionError("The MessageStore does not contain the general message [" + errorCode + "] Type: " + messageType + sb);
      }

      if (message.getType() != messageType) {
        StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
        messages.stream().forEach((m) -> sb.append("\t\t" + m.getCode() + " Type: " + m.getType() + "\n"));
        throw new AssertionError("The MessageStore contains message for code  [" + message.getCode() + "], but it is of type [" + message.getType() + "]" + sb);
      }
    }

    return this;
  }

  /**
   * Verifies that the system contains the given info message(s). The message(s) might be in the request, flash,
   * session
   * or application scopes.
   *
   * @param messages The fully rendered info message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsInfos(String... messages) {
    return assertContainsMessages(MessageType.INFO, messages);
  }

  /**
   * Verifies that the system contains the given message(s). The message(s) might be in the request, flash, session or
   * application scopes.
   *
   * @param type     The message type (ERROR, INFO, WARNING).
   * @param messages The fully rendered message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsMessages(MessageType type, String... messages) {
    Set<String> inMessageStore = new HashSet<>();
    MessageStore messageStore = get(MessageStore.class);
    List<Message> msgs = messageStore.getGeneralMessages();
    for (Message msg : msgs) {
      if (msg.getType() == type) {
        inMessageStore.add(msg.toString());
      }
    }

    if (!inMessageStore.containsAll(asList(messages))) {
      StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
      msgs.forEach((f) -> sb.append("\t\t[" + f + "]\n"));
      throw new AssertionError("The MessageStore does not contain the [" + type + "] message " + asList(messages) + sb);
    }

    return this;
  }

  /**
   * Verifies that the system has no general error messages.
   *
   * @return This.
   */
  public RequestResult assertContainsNoGeneralErrors() {
    return assertContainsNoMessages(MessageType.ERROR);
  }

  /**
   * Verifies that the system has no general messages of the specified type.
   *
   * @param messageType The message type
   * @return This.
   */
  public RequestResult assertContainsNoMessages(MessageType messageType) {
    MessageStore messageStore = get(MessageStore.class);
    List<Message> messages = messageStore.getGeneralMessages().stream().filter((m) -> m.getType() == messageType).collect(Collectors.toList());
    if (messages.isEmpty()) {
      return this;
    }

    StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
    messages.stream().forEach((m) -> sb.append("\t\t" + m.getCode() + " Type: " + m.getType() + "\n"));
    throw new AssertionError("The MessageStore contains the following errors.]" + sb);
  }

  /**
   * Verifies that the system contains the given warning message(s). The message(s) might be in the request, flash,
   * session or application scopes.
   *
   * @param messages The fully rendered warning message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsWarnings(String... messages) {
    return assertContainsMessages(MessageType.WARNING, messages);
  }

  /**
   * Verifies the response Content-Type.
   *
   * @param contentType The expected content-type
   * @return This.
   */
  public RequestResult assertContentType(String contentType) {
    String actual = response.getContentType();
    if (actual != null && !actual.equals(contentType)) {
      throw new AssertionError("Content-Type [" + actual + "] is not equal to the expected value [" + contentType + "]");
    }
    return this;
  }

  /**
   * Assert the cookie exists by name and then pass it to the provided consumer to allow the caller to assert on anything they wish.
   *
   * @param name     The cookie name.
   * @param consumer The consumer used to perform assertions.
   * @return This.
   */
  public RequestResult assertCookie(String name, Consumer<Cookie> consumer) {
    assertContainsCookie(name);
    Cookie actual = response.getCookies().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    if (consumer != null) {
      consumer.accept(actual);
    }
    return this;
  }

  /**
   * Assert the cookie exists by name and the value matches that of the provided value.
   *
   * @param name  The cookie name.
   * @param value The cookie value.
   * @return This.
   */
  public RequestResult assertCookie(String name, String value) {
    assertContainsCookie(name);
    Cookie actual = response.getCookies().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    if (actual.getValue() == null || !actual.getValue().equals(value)) {
      throw new AssertionError("Cookie [" + name + "] with value [" + actual + "] was not equal to the expected value [" + value + "]");
    }
    return this;
  }

  /**
   * Verifies the response encoding.
   *
   * @param encoding The expected content-type
   * @return This.
   */
  public RequestResult assertEncoding(String encoding) {
    String actual = response.getEncoding();
    if (actual != null && !actual.equals(encoding)) {
      throw new AssertionError("Character Encoding [" + actual + "] is not equal to the expected value [" + encoding + "]");
    }
    return this;
  }

  /**
   * Verifies that the HTTP response contains the specified header.
   *
   * @param header the name of the HTTP response header
   * @param value  the value of the header
   * @return This.
   */
  public RequestResult assertHeaderContains(String header, String value) {
    List<String> actual = response.getHeaders().get(header);
    if ((actual == null && value != null) || (actual != null && !actual.contains((value)))) {
      throw new AssertionError("Header [" + header + "] with value [" + actual + "] was not equal to the expected value [" + value + "]");
    }
    return this;
  }

  /**
   * Verifies that the response body is equal to the JSON created from the given object. The object is marshalled using
   * Jackson.
   *
   * @param object The object.
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public RequestResult assertJSON(Object object) throws IOException {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    String json = objectMapper.writeValueAsString(object);
    return assertJSON(json);
  }

  /**
   * De-serialize the JSON response using the type provided and allow the caller to assert on the result.
   *
   * @param type The object type.
   * @param type The consumer to pass the de-serialized object to.
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public <T> RequestResult assertJSON(Class<T> type, Consumer<T> consumer) throws IOException {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    T response = objectMapper.readValue(body, type);
    consumer.accept(response);
    return this;
  }

  /**
   * Verifies that the response body is equal to the given JSON text.
   *
   * @param json The JSON text.
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public RequestResult assertJSON(String json) throws IOException {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    assertJSONEquals(objectMapper, body, json);
    return this;
  }

  /**
   * Verifies that the response body is equal to the given JSON text file.
   *
   * @param jsonFile The JSON file to load and compare to the JSON response.
   * @param values   key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public RequestResult assertJSONFile(Path jsonFile, Object... values) throws IOException {
    return assertJSON(BodyTools.processTemplate(jsonFile, appendArray(values, "_to_milli", new ZonedDateTimeToMilliSeconds())));
  }

  /**
   * De-serialize the JSON response using the type provided. To use actual values in the JSON use ${actual.foo}
   * to use the property named <code>foo</code>.
   *
   * @param type     The object type of the JSON.
   * @param jsonFile The JSON file to load and compare to the JSON response.
   * @param values   key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public <T> RequestResult assertJSONFileWithActual(Class<T> type, Path jsonFile, Object... values) throws IOException {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    T actual = objectMapper.readValue(body, type);

    return assertJSONFile(jsonFile, appendArray(values, "actual", actual, "_to_milli", new ZonedDateTimeToMilliSeconds()));
  }

  /**
   * Verifies that the redirect URI is the given URI.
   *
   * @param uri The redirect URI.
   * @return This.
   */
  public RequestResult assertRedirect(String uri) {
    if (redirect == null || !redirect.equals(uri)) {
      throw new AssertionError("\nActual redirect not equal to the expected.\n Actual: \t" + redirect + "\n Expected:\t" + uri);
    }

    return this;
  }

  /**
   * Verifies that the request contains the attribute and the value is equal.
   *
   * @param name  the attribute name.
   * @param value the attribute value.
   * @return This.
   */
  public RequestResult assertRequestContainsAttribute(String name, Object value) {
    if (request.getAttribute(name) == null) {
      throw new AssertionError("Attribute [" + name + "] was not found in the request.");
    }

    if (!request.getAttribute(name).equals(value)) {
      throw new AssertionError("Attribute [" + name + "] was not equal to the expected value.\n\tActual: " + value + "\n\tExpected: " + request.getAttribute(name) + "\n");
    }

    return this;
  }

  /**
   * Verifies that the response status code is equal to the given code.
   *
   * @param statusCode The status code.
   * @return This.
   */
  public RequestResult assertStatusCode(int statusCode) {
    if (this.statusCode != statusCode) {
      throw new AssertionError("Status code [" + this.statusCode + "] was not equal to [" + statusCode + "]\nResponse body: [" + body + "]\nRedirect: [" + redirect + "]");
    }

    return this;
  }

  /**
   * Retrieves the instance of the given type from the Guice Injector.
   *
   * @param type The type.
   * @param <T>  The type.
   * @return The instance.
   */
  public <T> T get(Class<T> type) {
    return injector.getInstance(type);
  }

  /**
   * Retrieve a cookie by name. If the cookie does not exist in the response it will fail.
   *
   * @param name The name of the cookie.
   * @return the Cookie.
   */
  public Cookie getCookie(String name) {
    Cookie cookie = response.getCookies().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    if (cookie == null) {
      throw new AssertionError("Cookie [" + name + "] was not found in the response. Cookies found [" + String.join(", ", response.getCookies().stream().map(Cookie::getName).collect(Collectors.toList())));
    }

    return cookie;
  }

  /**
   * If the test is false, apply the consumer.
   * <p>
   * <pre>
   *   .ifFalse(foo.isBar(), (requestResult) -> requestResult.assertBodyDoesNotContain("bar"))
   * </pre>
   *
   * @param test     The boolean test to indicate if the consumer should be used.
   * @param consumer The consumer that accepts the RequestResult.
   * @return This.
   */
  public RequestResult ifFalse(boolean test, Consumer<RequestResult> consumer) {
    if (!test) {
      consumer.accept(this);
    }
    return this;
  }

  /**
   * If the test is true, apply the consumer. Example:
   * <pre>
   *   .ifTrue(foo.isBar(), (requestResult) -> requestResult.assertBodyContains("bar"))
   * </pre>
   *
   * @param test     The boolean test to indicate if the consumer should be used.
   * @param consumer The consumer that accepts the RequestResult.
   * @return This.
   */
  public RequestResult ifTrue(boolean test, Consumer<RequestResult> consumer) {
    if (test) {
      consumer.accept(this);
    }
    return this;
  }

  /**
   * Can be called to setup objects for assertions.
   *
   * @param consumer The consumer that accepts the RequestResult.
   * @return This.
   */
  public RequestResult setup(Consumer<RequestResult> consumer) {
    consumer.accept(this);
    return this;
  }

  private Object[] appendArray(Object[] values, Object... objects) {
    ArrayList<Object> list = new ArrayList<>(Arrays.asList(values));
    Collections.addAll(list, objects);
    return list.toArray();
  }
}

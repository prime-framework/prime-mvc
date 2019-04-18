/*
 * Copyright (c) 2014-2019, Inversoft Inc., All Rights Reserved
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Injector;
import org.primeframework.mock.servlet.MockContainer;
import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ActionMapper;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.servlet.PrimeFilter;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.util.QueryStringTools;
import static java.util.Arrays.asList;

/**
 * Result of a request to the {@link org.primeframework.mvc.test.RequestSimulator}.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class RequestResult {
  public final String body;

  public final MockContainer container;

  public final PrimeFilter filter;

  public final Injector injector;

  public final byte[] rawBody;

  public final String redirect;

  public final MockHttpServletRequest request;

  public final MockHttpServletResponse response;

  public final int statusCode;

  public RequestResult(MockContainer container, PrimeFilter filter, MockHttpServletRequest request,
                       MockHttpServletResponse response, Injector injector) {
    this.container = container;
    this.filter = filter;
    this.request = request;
    this.response = response;
    this.injector = injector;
    this.body = response.getStream().toString();
    this.rawBody = response.getStream().toByteArray();
    this.redirect = response.getRedirect();
    this.statusCode = response.getCode();
  }

  /**
   * Compares two JSON objects to ensure they are equal. This is done by converting the JSON objects to Maps, Lists, and
   * primitives and then comparing them. The error is output so that IntelliJ can diff the two JSON objects in order to
   * output the results.
   *
   * @param objectMapper The Jackson ObjectMapper used to convert the JSON strings to Maps.
   * @param actual       The actual JSON.
   * @param expected     The expected JSON.
   * @throws IOException If the ObjectMapper fails.
   */
  public static void assertJSONEquals(ObjectMapper objectMapper, String actual, String expected) throws IOException {
    if (actual == null || actual.equals("")) {
      throw new AssertionError("The actual response body is empty or is equal to an empty string without any JSON. This was "
          + "unexpected since you are trying to assert on JSON.");
    }

    Map<String, Object> response = objectMapper.readerFor(Map.class).readValue(actual);
    Map<String, Object> file = new HashMap<>();
    if (expected != null && !expected.equals("{}")) {
      file = objectMapper.readerFor(Map.class).readValue(expected);
    }

    if (response == null) {
      throw new AssertionError("The actual JSON was empty or once deserialize returned a null JsonNode object. Actual [" + actual + "]");
    }

    if (file == null) {
      throw new AssertionError("The expected JSON was empty or once deserialize returned a null JsonNode object. Expected [" + expected + "]");
    }

    objectMapper = objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true)
                               .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    response = deepSort(response, objectMapper);
    file = deepSort(file, objectMapper);

    if (!response.equals(file)) {
      String bodyString = objectMapper.writeValueAsString(response);
      String fileString = objectMapper.writeValueAsString(file);
      throw new AssertionError("The body doesn't match the expected JSON output. expected [" + fileString + "] but found [" + bodyString + "]");
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> deepSort(Map<String, Object> response, ObjectMapper objectMapper) {
    Map<String, Object> sorted = new TreeMap<>();
    response.forEach((key, value) -> {
      if (value instanceof Map) {
        sorted.put(key, deepSort((Map<String, Object>) value, objectMapper));
      } else if (value instanceof List) {
        sorted.put(key, deepSort((List<Object>) value, objectMapper));
      } else {
        sorted.put(key, value);
      }
    });

    return sorted;
  }

  @SuppressWarnings("unchecked")
  private static List<Object> deepSort(List<Object> list, ObjectMapper objectMapper) {
    List<Object> sorted = new ArrayList<>();
    list.forEach(value -> {
      if (value instanceof Map) {
        sorted.add(deepSort((Map<String, Object>) value, objectMapper));
      } else if (value instanceof List) {
        sorted.add(deepSort((List<Object>) value, objectMapper));
      } else {
        sorted.add(value);
      }
    });

    sorted.sort(Comparator.comparing(value -> {
      try {
        return objectMapper.writeValueAsString(value);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }));

    return sorted;
  }

  /**
   * Verifies that the body equals the given string.
   *
   * @param string The string to compare against the body.
   * @return This.
   */
  public RequestResult assertBody(String string) {
    if (!body.equals(string)) {
      throw new AssertionError("The body doesn't match the expected output. expected [" + string + "] but found [" + body + "]");
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
   * uses the MessageProvider for the current test URI and the given keys to look up the messages.
   *
   * @param key    The key.
   * @param values The replacement values.
   * @return This.
   */
  public RequestResult assertBodyContainsMessagesFromKey(String key, Object... values) {
    return _assertBodyContainsMessagesFromKey(true, key, values);
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
   * Verifies that the body does not contain the messages from the given key and optionally provided replacement values.
   * This uses the MessageProvider for the current test URI and the given keys to look up the messages.
   *
   * @param key    The key.
   * @param values The replacement values.
   * @return This.
   */
  public RequestResult assertBodyDoesNotContainMessagesFromKey(String key, Object... values) {
    return _assertBodyContainsMessagesFromKey(false, key, values);
  }

  /**
   * Verifies that the body does contain the messages from the given keys. This uses the MessageProvider for the current
   * test URI and the given keys to look up the messages.
   *
   * @param keys The keys.
   * @return This.
   */
  public RequestResult assertBodyDoesNotContainMessagesFromKeys(String... keys) {
    for (String key : keys) {
      assertBodyDoesNotContainMessagesFromKey(key, "foo", "bar", "baz");
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
      return assertBody(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
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
    Cookie actual = getCookie(name);
    if (actual == null) {
      throw new AssertionError("Cookie [" + name + "] was not found in the response. Cookies found [" + response.getCookies().stream().map(Cookie::getName).collect(Collectors.joining(", ")) + "]");
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
   * Verifies that the system has errors for the given fields. This doesn't assert the error itself, just that the field
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
        //noinspection StringConcatenationInsideStringBufferAppend
        msgs.keySet().forEach(f -> sb.append("\t\t" + f + "\n"));
        throw new AssertionError("The MessageStore does not contain a error for the field [" + field + "]" + sb);
      }

      boolean found = false;
      for (FieldMessage fieldMessage : fieldMessages) {
        found |= fieldMessage.getType() == MessageType.ERROR;
      }

      if (!found) {
        StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
        //noinspection StringConcatenationInsideStringBufferAppend
        fieldMessages.forEach(f -> sb.append("\t\t[" + f.getType() + "]\n"));
        throw new AssertionError("The MessageStore contains messages but no errors for the field [" + field + "]" + sb);
      }
    }

    return this;
  }

  /**
   * Verifies that the system has general errors. This doesn't assert the error itself, just that the general error
   * code.
   *
   * @param messageCodes The name of the error code(s). Not the fully rendered message(s)
   * @return This.
   */
  public RequestResult assertContainsGeneralErrorMessageCodes(String... messageCodes) {
    return assertContainsGeneralMessageCodes(MessageType.ERROR, messageCodes);
  }

  /**
   * Verifies that the system has info errors. This doesn't assert the message itself, just that the general message
   * code.
   *
   * @param messageCodes The name of the message code(s). Not the fully rendered message(s)
   * @return This.
   */
  public RequestResult assertContainsGeneralInfoMessageCodes(String... messageCodes) {
    return assertContainsGeneralMessageCodes(MessageType.INFO, messageCodes);
  }

  /**
   * Verifies that the system has general messages. This doesn't assert the message itself, just that the general
   * message code.
   *
   * @param messageType The message type
   * @param errorCodes  The name of the message code(s). Not the fully rendered message(s)
   * @return This.
   */
  public RequestResult assertContainsGeneralMessageCodes(MessageType messageType, String... errorCodes) {
    MessageStore messageStore = get(MessageStore.class);
    List<Message> messages = messageStore.getGeneralMessages();
    for (String errorCode : errorCodes) {
      Message message = messages.stream().filter(m -> m.getCode().equals(errorCode)).findFirst().orElse(null);
      if (message == null) {
        StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
        //noinspection StringConcatenationInsideStringBufferAppend
        messages.forEach(m -> sb.append("\t\t" + m.getType() + " " + m.getCode() + "\t" + ((m instanceof SimpleMessage) ? ((SimpleMessage) m).message : "") + "\n"));
        throw new AssertionError("The MessageStore does not contain the general message [" + errorCode + "] Type: " + messageType + sb);
      }

      if (message.getType() != messageType) {
        StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
        //noinspection StringConcatenationInsideStringBufferAppend
        messages.forEach(m -> sb.append("\t\t" + m.getType() + " " + m.getCode() + "\t" + ((m instanceof SimpleMessage) ? ((SimpleMessage) m).message : "") + "\n"));
        throw new AssertionError("The MessageStore contains message for code  [" + message.getCode() + "], but it is of type [" + message.getType() + "]" + sb);
      }
    }

    return this;
  }

  /**
   * Verifies that the system contains the given info message(s). The message(s) might be in the request, flash, session
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
      //noinspection StringConcatenationInsideStringBufferAppend
      msgs.forEach(f -> sb.append("\t\t[" + f + "]\n"));
      throw new AssertionError("The MessageStore does not contain the [" + type + "] message " + asList(messages) + sb);
    }

    return this;
  }

  /**
   * Verifies that the response does not contain any field error messages.
   *
   * @return This.
   */
  public RequestResult assertContainsNoFieldMessages() {
    return assertContainsNoFieldMessages(MessageType.ERROR);
  }

  /**
   * Verifies that the response does not contain any messages of the provided type.
   *
   * @param messageType the message type to look for in the response
   * @return This.
   */
  public RequestResult assertContainsNoFieldMessages(MessageType messageType) {
    MessageStore messageStore = get(MessageStore.class);
    List<FieldMessage> messages = messageStore.getFieldMessages().values().stream().flatMap(Collection::stream).filter(m -> m.getType() == messageType).collect(Collectors.toList());
    if (messages.isEmpty()) {
      return this;
    }

    StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
    //noinspection StringConcatenationInsideStringBufferAppend
    messages.forEach(m -> sb.append("\t\t" + m.getType() + "\tField: " + m.getField() + " Code: " + m.getCode() + "\t" + ((m instanceof SimpleFieldMessage) ? ((SimpleFieldMessage) m).message : "") + "\n"));
    throw new AssertionError("The MessageStore contains the following field errors.]" + sb);
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
    List<Message> messages = messageStore.getGeneralMessages().stream().filter(m -> m.getType() == messageType).collect(Collectors.toList());
    if (messages.isEmpty()) {
      return this;
    }

    StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
    //noinspection StringConcatenationInsideStringBufferAppend
    messages.forEach(m -> sb.append("\t\t" + m.getType() + "\t" + m.getCode() + "\t" + ((m instanceof SimpleMessage) ? ((SimpleMessage) m).message : "") + "\n"));
    throw new AssertionError("The MessageStore contains the following general errors.]" + sb);
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
   * Assert the cookie exists by name and then pass it to the provided consumer to allow the caller to assert on
   * anything they wish.
   *
   * @param name     The cookie name.
   * @param consumer The consumer used to perform assertions.
   * @return This.
   */
  public RequestResult assertCookie(String name, ThrowingConsumer<Cookie> consumer) throws Exception {
    assertContainsCookie(name);

    Cookie actual = getCookie(name);
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

    Cookie actual = getCookie(name);
    if (!actual.getValue().equals(value)) {
      throw new AssertionError("Cookie [" + name + "] with value [" + actual.getValue() + "] was not equal to the expected value [" + value + "]");
    }

    return this;
  }

  /**
   * Assert the cookie was deleted on the server by sending back a null value and a max age of 0.
   *
   * @param name The cookie name.
   * @return This.
   */
  public RequestResult assertCookieWasDeleted(String name) {
    assertContainsCookie(name);

    Cookie actual = getCookie(name);
    if (actual.getValue() != null && actual.getMaxAge() != 0) {
      throw new AssertionError("Cookie [" + name + "] was not deleted. The value is [" + actual.getValue() + "] and the maxAge is [" + actual.getMaxAge() + "]");
    }

    return this;
  }

  /**
   * Assert the cookie does NOT exist by name.
   *
   * @param name The cookie name.
   * @return This.
   */
  public RequestResult assertDoesNotContainsCookie(String name) {
    Cookie actual = getCookie(name);
    if (actual != null) {
      throw new AssertionError("Cookie [" + name + "] was not expected to be found in the response. Cookies found [" + response.getCookies().stream().map(Cookie::getName).collect(Collectors.joining(", ")) + "]");
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
    List<String> actual = null;
    for (String key : response.getHeaders().keySet()) {
      if (key.equalsIgnoreCase(header)) {
        actual = response.getHeaders().get(key);
        break;
      }
    }

    if ((actual == null && value != null) || (actual != null && !actual.contains((value)))) {
      StringBuilder responseHeaders = new StringBuilder();
      response.getHeaders().forEach((k, v) -> responseHeaders.append("\t").append(k).append(": ").append(v).append("\n"));
      throw new AssertionError("Header [" + header + "] with value [" + (actual == null ? null : String.join(", ", actual)) + "] was not equal to the expected value [" + value + "].\n\nResponse Headers:\n" + responseHeaders);
    }
    return this;
  }

  /**
   * Verifies that the HTTP response does not contains the specified header.
   *
   * @param header the name of the HTTP response header
   * @return This.
   */
  public RequestResult assertHeaderDoesNotContain(String header) {
    List<String> actual = null;
    for (String key : response.getHeaders().keySet()) {
      if (key.equalsIgnoreCase(header)) {
        actual = response.getHeaders().get(key);
        break;
      }
    }

    if (actual != null && !actual.isEmpty()) {
      StringBuilder responseHeaders = new StringBuilder();
      response.getHeaders().forEach((k, v) -> responseHeaders.append("\t").append(k).append(": ").append(v).append("\n"));
      throw new AssertionError("Header [" + header + "] with value [" + String.join(", ", actual) + "] was not expected in the HTTP response.\n\nResponse Headers:\n" + responseHeaders);
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
   * @param type     The object type.
   * @param consumer The consumer to pass the de-serialized object to.
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
   * De-serialize the JSON response using the type provided. To use actual values in the JSON use ${actual.foo} to use
   * the property named <code>foo</code>.
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
   * Verifies that the normalized body equals the given string.
   *
   * @param string The string to compare against the body.
   * @return This.
   */
  public RequestResult assertNormalizedBody(String string) {
    if (!normalize(body).equals(string)) {
      throw new AssertionError("The body doesn't match the expected output. expected [" + string + "] but found [" + body.trim().replace("\r\n", "\n").replace("\r", "\n") + "]");
    }

    return this;
  }

  /**
   * Verifies that the body equals the content of the given File.
   * <p>
   * This assertion will trim and normalize line returns before performing an equality check.
   *
   * @param path   The file to load and compare to the response.
   * @param values key value pairs of replacement values for use in the file.
   * @return This.
   */
  public RequestResult assertNormalizedBodyFile(Path path, Object... values) throws IOException {
    if (values.length == 0) {
      return assertNormalizedBody(normalize(new String(Files.readAllBytes(path), StandardCharsets.UTF_8)));
    }
    return assertNormalizedBody(normalize(BodyTools.processTemplate(path, values)));
  }

  /**
   * Verifies that the redirect URI is the given URI. The order of the parameters added using the builder is not
   * important.
   *
   * @param uri      The base redirect URI.
   * @param consumer The consumer to accept a URI builder to add parameters
   * @return This.
   */
  public RequestResult assertRedirect(String uri, Consumer<TestURIBuilder> consumer) {
    if (redirect == null) {
      throw new AssertionError("\nActual redirect was null. Why do you want to assert on it? Status code was [" + statusCode + "]");
    }

    TestURIBuilder builder = TestURIBuilder.builder(uri);
    consumer.accept(builder);

    String expectedUri = builder.toString();
    assertRedirectEquality(expectedUri);
    return this;
  }

  /**
   * Verifies that the redirect URI is the given URI. The parameter order on the URI is not important.
   *
   * @param expectedUri The full redirect URI include parameters
   * @return This.
   */
  public RequestResult assertRedirect(String expectedUri) {
    if (redirect == null) {
      throw new AssertionError("\nActual redirect was null. \nExpected:\t" + expectedUri);
    }

    assertRedirectEquality(expectedUri);
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
      StringBuilder sb = new StringBuilder("Status code [" + this.statusCode + "] was not equal to [" + statusCode + "]\n");
      MessageStore messageStore = get(MessageStore.class);

      // Append any General Messages to the error message to aid in debug
      List<Message> generatorMessages = messageStore.getGeneralMessages();
      if (!generatorMessages.isEmpty()) {
        sb.append("\nThe following general error messages were returned in the message store:\n\n");
      }
      generatorMessages.forEach(m -> sb.append("\t\t").append(m.getType()).append("\t").append(m.getCode()).append("\t").append((m instanceof SimpleMessage) ? ((SimpleMessage) m).message : "").append("\n"));

      // Append any Field Messages to the error message to aid in debug
      List<FieldMessage> fieldMessages = messageStore.getFieldMessages().values().stream().flatMap(Collection::stream).collect(Collectors.toList());
      if (!fieldMessages.isEmpty()) {
        sb.append("\nThe following field error messages were returned in the message store:\n\n");
      }
      //noinspection StringConcatenationInsideStringBufferAppend
      fieldMessages.forEach(m -> sb.append("\t\t" + m.getType() + "\tField: " + m.getField() + " Code: " + m.getCode() + "\t" + ((m instanceof SimpleFieldMessage) ? ((SimpleFieldMessage) m).message : "") + "\n"));

      sb.append("\nRedirect: [").append(redirect).append("]\n").append("Response body: \n").append(body);
      throw new AssertionError(sb.toString());
    }

    return this;
  }

  /**
   * Execute the redirect and accept a consumer to assert on the response.
   *
   * @param consumer The request result from following the redirect.
   * @return This.
   */
  public RequestResult executeRedirect(Consumer<RequestResult> consumer) {
    String uri = redirect.contains("?") ? redirect.substring(0, redirect.indexOf("?")) : redirect;

    RequestBuilder rb = new RequestBuilder(uri, container, filter, injector);
    if (uri.length() != redirect.length()) {
      String params = redirect.substring(redirect.indexOf("?") + 1);
      QueryStringTools.parseQueryString(params).forEach(rb::withParameters);
    }

    consumer.accept(rb.get());
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
   * Retrieve a cookie by name. If the cookie does not exist in the response, this returns null.
   *
   * @param name The name of the cookie.
   * @return The Cookie or null.
   */
  public Cookie getCookie(String name) {
    return response.getCookies().stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
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
  public RequestResult ifFalse(boolean test, ThrowingConsumer<RequestResult> consumer) throws Exception {
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
  public RequestResult ifTrue(boolean test, ThrowingConsumer<RequestResult> consumer) throws Exception {
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
  public RequestResult setup(ThrowingConsumer<RequestResult> consumer) throws Exception {
    consumer.accept(this);
    return this;
  }

  /**
   * Can be called to setup objects for assertions.
   *
   * @param runnable The runnable to stuff.
   * @return This.
   */
  public RequestResult setup(Runnable runnable) {
    runnable.run();
    return this;
  }

  private RequestResult _assertBodyContainsMessagesFromKey(boolean contains, String key, Object... values) {
    MessageProvider messageProvider = get(MessageProvider.class);
    ActionInvocationStore actionInvocationStore = get(ActionInvocationStore.class);
    ActionMapper actionMapper = get(ActionMapper.class);

    // Using the ActionMapper so that URL segments are properly handled and the correct URL is used for message lookups.
    ActionInvocation actionInvocation = actionMapper.map(null, request.getRequestURI(), true);
    actionInvocationStore.setCurrent(actionInvocation);

    String message = messageProvider.getMessage(key, values);
    if (contains != body.contains(message)) {
      String text = contains ? "didn't" : "does";
      throw new AssertionError("Body " + text + " contain [" + message + "] for the key [" + key + "]\nRedirect: [" + redirect + "]\nBody:\n" + body);
    }

    return this;
  }

  private Object[] appendArray(Object[] values, Object... objects) {
    ArrayList<Object> list = new ArrayList<>(Arrays.asList(values));
    Collections.addAll(list, objects);
    return list.toArray();
  }

  private void assertRedirectEquality(String expectedUri) {
    SortedMap<String, List<String>> actual = uriToMap(redirect);
    SortedMap<String, List<String>> expected = uriToMap(expectedUri);

    if (!actual.equals(expected)) {
      // Replace any 'actual' values requested and then try again
      boolean recheck = replaceWithActualValues(actual, expected);
      if (!recheck || !actual.equals(expected)) {
        throw new AssertionError("Actual redirect not equal to the expected. expected [" + expectedUri + "] but found [" + redirect + "]");
      }
    }
  }

  private String normalize(String input) {
    return input.trim().replace("\r\n", "\n").replace("\r", "\n");
  }

  private boolean replaceWithActualValues(SortedMap<String, List<String>> actual, Map<String, List<String>> expected) {
    boolean recheck = false;

    // Bail early they are not even the same size
    if (actual.keySet().size() != expected.keySet().size()) {
      return false;
    }

    List<String> actualKeys = new ArrayList<>(actual.keySet());
    List<String> expectedKeys = new ArrayList<>(expected.keySet());

    for (int i = 0; i < actualKeys.size(); i++) {
      if (!Objects.equals(actualKeys.get(i), expectedKeys.get(i))) {
        return false;
      }

      List<String> expectedValues = expected.get(actualKeys.get(i));
      if (expectedValues != null && expectedValues.size() > 0 && expectedValues.get(0).equals("___actual___")) {
        expectedValues.clear();
        expectedValues.addAll(actual.get(actualKeys.get(i)));
        recheck = true;
      }
    }

    return recheck;
  }

  private SortedMap<String, List<String>> uriToMap(String uri) {
    SortedMap<String, List<String>> map = new TreeMap<>();
    int queryIndex = uri.indexOf("?");
    int fragmentIndex = uri.indexOf("#");
    if (queryIndex == -1 && fragmentIndex == -1) {
      // First key will be the URI and an empty value, no parameters
      map.put(uri, Collections.emptyList());
      return map;
    }

    // First key will be the URI and a value of "?" or "#"
    if (queryIndex != -1) {
      map.put(uri.substring(0, queryIndex), Collections.singletonList("?"));
    } else {
      map.put(uri.substring(0, fragmentIndex), Collections.singletonList("#"));
    }

    String params;
    if (queryIndex != -1) {
      params = uri.substring(queryIndex + 1, (fragmentIndex == -1 ? uri.length() : fragmentIndex));
    } else {
      // Only fragment parameters exist
      params = uri.substring(fragmentIndex + 1);
    }

    map.putAll(QueryStringTools.parseQueryString(params));

    // Collect the fragment parameters now since there are both on the URI
    if (queryIndex != -1 && fragmentIndex != -1) {
      params = uri.substring(fragmentIndex + 1);
      map.putAll(QueryStringTools.parseQueryString(params));
    }

    // Sort the lists so we can check for equality
    map.values().forEach(l -> l.sort(Comparator.naturalOrder()));
    return map;
  }

  @FunctionalInterface
  public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
  }

  public static class TestURIBuilder extends QueryStringBuilder {
    private TestURIBuilder() {
    }

    private TestURIBuilder(String uri) {
      super(uri);
    }

    public static TestURIBuilder builder() {
      return new TestURIBuilder();
    }

    public static TestURIBuilder builder(String uri) {
      return new TestURIBuilder(uri);
    }

    /**
     * Add a parameter to the request that you will expect to match the expected. This may be useful if a timestamp oro
     * other random data is returned that is not important to assert on.
     *
     * <strong>This is only intended for use during testing.</strong>
     *
     * @param name the parameter name
     * @return this
     */
    @Override
    public TestURIBuilder withActual(String name) {
      with(name, "___actual___");
      return this;
    }
  }
}

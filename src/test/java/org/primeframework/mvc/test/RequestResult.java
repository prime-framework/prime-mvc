/*
 * Copyright (c) 2014-2023, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.nio.charset.Charset;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Injector;
import com.inversoft.http.HTTPStrings;
import com.inversoft.http.HTTPStrings.Headers;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.Cookie.SameSite;
import io.fusionauth.http.HTTPValues.ContentTypes;
import io.fusionauth.http.HTTPValues.Methods;
import io.fusionauth.http.io.NonBlockingByteBufferOutputStream;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.http.util.HTTPTools.HeaderValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.primeframework.mock.MockUserAgent;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ActionMapper;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.TestMessageObserver;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;
import org.primeframework.mvc.message.scope.CookieFlashScope;
import org.primeframework.mvc.security.Encryptor;
import org.primeframework.mvc.util.CookieTools;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.util.QueryStringTools;
import org.primeframework.mvc.util.ThrowingFunction;
import org.primeframework.mvc.util.ThrowingRunnable;
import static java.util.Arrays.asList;

/**
 * Result of a request to the {@link org.primeframework.mvc.test.RequestSimulator}.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class RequestResult {
  public final Injector injector;

  public final TestMessageObserver messageObserver;

  public final int port;

  public final HTTPRequest request;

  public final ClientResponse<byte[], byte[]> response;

  public final MockUserAgent userAgent;

  private String body;

  public RequestResult(Injector injector, HTTPRequest request, ClientResponse<byte[], byte[]> response,
                       MockUserAgent userAgent, TestMessageObserver messageObserver, int port) {
    this.request = request;
    this.injector = injector;
    this.response = response;
    this.userAgent = userAgent;
    this.messageObserver = messageObserver;
    this.port = port;

    // Set the request & response into the thread local so that they can be used when asserting
    HTTPObjectsHolder.clearRequest();
    HTTPObjectsHolder.setRequest(request);
    HTTPObjectsHolder.clearResponse();
    HTTPObjectsHolder.setResponse(new HTTPResponse(new NonBlockingByteBufferOutputStream(null, 1024), request));
  }

  /**
   * Compares two JSON objects to ensure they are equal. This is done by converting the JSON objects to Maps, Lists, and primitives and then comparing
   * them. The error is output so that IntelliJ can diff the two JSON objects in order to output the results.
   *
   * @param objectMapper The Jackson ObjectMapper used to convert the JSON strings to Maps.
   * @param actual       The actual JSON.
   * @param expected     The expected JSON.
   * @throws IOException If the ObjectMapper fails.
   */
  public static void assertJSONEquals(ObjectMapper objectMapper, String actual, String expected) throws IOException {
    _assertJSONEquals(objectMapper, actual, expected, true, null);
  }

  /**
   * Compares two JSON objects to ensure they are equal. This is done by converting the JSON objects to Maps, Lists, and primitives and then comparing
   * them. The error is output so that IntelliJ can diff the two JSON objects in order to output the results.
   *
   * @param objectMapper The Jackson ObjectMapper used to convert the JSON strings to Maps.
   * @param actual       The actual JSON.
   * @param expected     The expected JSON.
   * @throws IOException If the ObjectMapper fails.
   */
  public static void assertSortedJSONEquals(ObjectMapper objectMapper, String actual, String expected)
      throws IOException {
    _assertJSONEquals(objectMapper, actual, expected, false, null);
  }

  private static void _assertJSONEquals(ObjectMapper objectMapper, String actual, String expected, boolean sortArrays,
                                        Path jsonFile)
      throws IOException {
    if (actual == null || actual.equals("")) {
      throw new AssertionError("The actual response body is empty or is equal to an empty string without any JSON. This was "
          + "unexpected since you are trying to assert on JSON.");
    }

    Map<String, Object> response = objectMapper.readerFor(Map.class).readValue(actual);
    Map<String, Object> file = new HashMap<>();
    if (expected != null && !expected.equals("{}")) {
      file = objectMapper.readerFor(Map.class).readValue(expected);
    }

    // We generated this file to assist with test building.
    if (jsonFile != null && file.containsKey("prime-mvc-auto-generated")) {
      file = objectMapper.readerFor(Map.class).readValue(actual);
      Files.write(jsonFile.toAbsolutePath(), objectMapper.writerWithDefaultPrettyPrinter()
                                                         .withFeatures(SerializationFeature.INDENT_OUTPUT)
                                                         .with(new DefaultPrettyPrinter().withArrayIndenter(new DefaultIndenter()))
                                                         .writeValueAsBytes(file));
    }

    if (response == null) {
      throw new AssertionError("The actual JSON was empty or once deserialize returned a null JsonNode object. Actual [" + actual + "]");
    }

    if (file == null) {
      throw new AssertionError("The expected JSON was empty or once deserialize returned a null JsonNode object. Expected [" + expected + "]");
    }

    // Don't modify the objectMapper, create a new configuration
    ObjectMapper prettyPrinter = objectMapper.copy()
                                             .configure(SerializationFeature.INDENT_OUTPUT, true)
                                             .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    response = deepSort(response, prettyPrinter, sortArrays);
    file = deepSort(file, prettyPrinter, sortArrays);

    if (!response.equals(file)) {
      String bodyString = prettyPrinter.writeValueAsString(response);
      String fileString = prettyPrinter.writeValueAsString(file);
      throw new AssertionError("The body doesn't match the expected JSON output. Expected [" + fileString + "] but found [" + bodyString + "]");
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> deepSort(Map<String, Object> response, ObjectMapper objectMapper,
                                              boolean sortArrays) {
    Map<String, Object> sorted = new TreeMap<>();
    response.forEach((key, value) -> {
      if (value instanceof Map) {
        sorted.put(key, deepSort((Map<String, Object>) value, objectMapper, sortArrays));
      } else if (value instanceof List) {
        sorted.put(key, deepSort((List<Object>) value, objectMapper, sortArrays));
      } else {
        sorted.put(key, value);
      }
    });

    return sorted;
  }

  @SuppressWarnings("unchecked")
  private static List<Object> deepSort(List<Object> list, ObjectMapper objectMapper, boolean sortArrays) {
    List<Object> sorted = new ArrayList<>();
    list.forEach(value -> {
      if (value instanceof Map) {
        sorted.add(deepSort((Map<String, Object>) value, objectMapper, sortArrays));
      } else if (value instanceof List) {
        sorted.add(deepSort((List<Object>) value, objectMapper, sortArrays));
      } else {
        sorted.add(value);
      }
    });

    if (sortArrays) {
      sorted.sort(Comparator.comparing(value -> {
        try {
          return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }));
    }

    return sorted;
  }

  /**
   * Verifies that the body equals the given string.
   *
   * @param string The string to compare against the body.
   * @return This.
   */
  public RequestResult assertBody(String string) {
    String body = getBodyAsString();
    if (!body.equals(string)) {
      throw new AssertionError("The body doesn't match the expected output. Expected [" + string + "] but found [" + body + "]");
    }

    return this;
  }

  /**
   * Verifies that the body contains all the given Strings.
   *
   * @param strings The strings to check.
   * @return This.
   */
  public RequestResult assertBodyContains(String... strings) {
    return _assertBodyContains(false, strings);
  }

  /**
   * Verifies that the body contains all the given Strings.
   *
   * @param strings The strings to check.
   * @return This.
   */
  public RequestResult assertBodyContainsEscaped(String... strings) {
    return _assertBodyContains(true, strings);
  }

  /**
   * Verifies that the body contains the messages (escaped) from the given key and optionally provided replacement values. This uses the
   * MessageProvider for the current test URI and the given keys to look up the messages.
   *
   * @param key    The key.
   * @param values The replacement values.
   * @return This.
   */
  public RequestResult assertBodyContainsEscapedMessagesFromKey(String key, Object... values) {
    MessageProvider messageProvider = getMessageProviderToLookupMessages();
    return _assertBodyContainsMessagesFromKey(messageProvider, true, key, true, values);
  }

  /**
   * Verifies that the body contains the messages (escaped) from the given keys. This uses the MessageProvider for the current test URI and the given
   * keys to look up the messages.
   *
   * @param keys The keys.
   * @return This.
   */
  public RequestResult assertBodyContainsEscapedMessagesFromKeys(String... keys) {
    MessageProvider messageProvider = getMessageProviderToLookupMessages();
    for (String key : keys) {
      _assertBodyContainsMessagesFromKey(messageProvider, true, key, true);
    }

    return this;
  }

  /**
   * Verifies that the body contains the messages from the given key and optionally provided replacement values. This uses the MessageProvider for the
   * current test URI and the given keys to look up the messages.
   *
   * @param key    The key.
   * @param values The replacement values.
   * @return This.
   */
  public RequestResult assertBodyContainsMessagesFromKey(String key, Object... values) {
    MessageProvider messageProvider = getMessageProviderToLookupMessages();
    return _assertBodyContainsMessagesFromKey(messageProvider, true, key, false, values);
  }

  /**
   * Verifies that the body contains the messages from the given keys. This uses the MessageProvider for the current test URI and the given keys to
   * look up the messages.
   *
   * @param keys The keys.
   * @return This.
   */
  public RequestResult assertBodyContainsMessagesFromKeys(String... keys) {
    MessageProvider messageProvider = getMessageProviderToLookupMessages();
    for (String key : keys) {
      _assertBodyContainsMessagesFromKey(messageProvider, true, key, false, "foo", "bar", "baz");
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
    String body = getBodyAsString();
    for (String string : strings) {
      if (body.contains(string)) {
        throw new AssertionError("Body shouldn't contain [" + string + "]\nRedirect: [" + response.getHeader(HTTPStrings.Headers.Location) + "]\nBody:\n" + body);
      }
    }

    return this;
  }

  /**
   * Verifies that the body does not contain the messages from the given key and optionally provided replacement values. This uses the MessageProvider
   * for the current test URI and the given keys to look up the messages.
   *
   * @param key    The key.
   * @param values The replacement values.
   * @return This.
   */
  public RequestResult assertBodyDoesNotContainMessagesFromKey(String key, Object... values) {
    MessageProvider messageProvider = getMessageProviderToLookupMessages();
    return _assertBodyContainsMessagesFromKey(messageProvider, false, key, false, values);
  }

  /**
   * Verifies that the body does contain the messages from the given keys. This uses the MessageProvider for the current test URI and the given keys
   * to look up the messages.
   *
   * @param keys The keys.
   * @return This.
   */
  public RequestResult assertBodyDoesNotContainMessagesFromKeys(String... keys) {
    MessageProvider messageProvider = getMessageProviderToLookupMessages();
    for (String key : keys) {
      _assertBodyContainsMessagesFromKey(messageProvider, false, key, false, "foo", "bar", "baz");
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
      return assertBody(Files.readString(path));
    }
    return assertBody(BodyTools.processTemplateForAssertion(path, values));
  }

  /**
   * Verifies that the body is empty.
   *
   * @return This
   */
  public RequestResult assertBodyIsEmpty() {
    String body = getBodyAsString();
    if (!body.isEmpty()) {
      throw new AssertionError("Body is not empty.\nBody:\n" + body);
    }

    return this;
  }

  /**
   * Verifies that the system has errors for the given fields. This doesn't assert the error itself, just that the field contains an error.
   * <p>
   * There may be additional field errors in the messageStore than the ones requested and this will not fail.
   *
   * @param fields The name of the field code(s). Not the fully rendered message(s)
   * @return This.
   */
  @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
  public RequestResult assertContainsAtLeastTheseFieldErrors(String... fields) {
    Map<String, List<FieldMessage>> msgs = messageObserver.getFieldMessages();
    for (String field : fields) {
      List<FieldMessage> fieldMessages = msgs.get(field);
      if (fieldMessages == null) {
        StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
        msgs.keySet().stream().sorted().forEach(f -> sb.append("\t\t" + f + "\n"));
        throw new AssertionError("The MessageStore does not contain a error for the field [" + field + "]" + sb);
      }

      boolean found = false;
      for (FieldMessage fieldMessage : fieldMessages) {
        found |= fieldMessage.getType() == MessageType.ERROR;
      }

      if (!found) {
        StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
        fieldMessages.sort(Comparator.comparing(FieldMessage::getField));
        fieldMessages.forEach(f -> sb.append("\t\t[" + f.getType() + "]\n"));
        throw new AssertionError("The MessageStore contains messages but no errors for the field [" + field + "]" + sb);
      }
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
    getCookieOrThrow(name);
    return this;
  }

  /**
   * Verifies that the system contains the given error message(s). The message(s) might be in the request, flash, session or application scopes.
   *
   * @param messages The fully rendered error message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsErrors(String... messages) {
    return assertContainsMessages(MessageType.ERROR, messages);
  }

  /**
   * Verifies that the system has errors for the given fields. This doesn't assert the error itself, just that the field contains an error.
   * <p>
   * If additional field errors exist in the message store not provided here an error will occur to ensure you have accounted for all errors.
   *
   * @param fields The name of the field code(s). Not the fully rendered message(s)
   * @return This.
   */
  @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
  public RequestResult assertContainsFieldErrors(String... fields) {
    // First check to see if all the field errors exist that were requested
    assertContainsAtLeastTheseFieldErrors(fields);

    Map<String, List<FieldMessage>> msgs = messageObserver.getFieldMessages();

    // Now Ensure that all fields have been accounted for, if there are more field errors than were requested fail
    Set<String> requestedFields = new HashSet<>(Arrays.asList(fields));
    List<String> remaining = msgs.keySet().stream().filter(k -> !requestedFields.contains(k)).toList();
    if (remaining.size() > 0) {
      StringBuilder sb = new StringBuilder("The MessageStore contains additional field messages that you did not assert.\n");

      // You asserted these
      sb.append("\nYou asserted the following field errors exist:\n");
      requestedFields.forEach(f -> sb.append("\t\tField: " + f + "\n"));

      // Fields you are missing
      sb.append("\nYou are missing the following field errors from your assertion:\n");
      remaining.stream().sorted().forEach(field -> sb.append("\t\tField: ").append(field).append("\n"));

      // The message store contains
      sb.append("\nThe MessageStore contains the following field errors:\n");
      msgs.keySet().stream().sorted().forEach(f -> {
        StringBuilder fieldMessages = new StringBuilder();
        msgs.get(f).forEach(fm -> fieldMessages.append("\t\t\t[" + fm.getType() + "] " + fm.getCode()));
        sb.append("\t\tField: " + f + "\n" + fieldMessages + "\n");
      });

      throw new AssertionError(sb);
    }

    return this;
  }

  /**
   * Verifies that the system has general errors. This doesn't assert the error itself, just that the general error code.
   *
   * @param messageCodes The name of the error code(s). Not the fully rendered message(s)
   * @return This.
   */
  public RequestResult assertContainsGeneralErrorMessageCodes(String... messageCodes) {
    return assertContainsGeneralMessageCodes(MessageType.ERROR, messageCodes);
  }

  /**
   * Verifies that the system has info errors. This doesn't assert the message itself, just that the general message code.
   *
   * @param messageCodes The name of the message code(s). Not the fully rendered message(s)
   * @return This.
   */
  public RequestResult assertContainsGeneralInfoMessageCodes(String... messageCodes) {
    return assertContainsGeneralMessageCodes(MessageType.INFO, messageCodes);
  }

  /**
   * Verifies that the system has general messages. This doesn't assert the message itself, just that the general message code.
   *
   * @param messageType The message type
   * @param errorCodes  The name of the message code(s). Not the fully rendered message(s)
   * @return This.
   */
  public RequestResult assertContainsGeneralMessageCodes(MessageType messageType, String... errorCodes) {
    List<Message> messages = messageObserver.getGeneralMessages();
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

    // Ensure we have accounted for every message in the store of this type.
    long count = messages.stream().filter(m -> m.getType() == messageType).count();
    if (count != errorCodes.length) {
      StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
      //noinspection StringConcatenationInsideStringBufferAppend
      messages.stream().filter(m -> m.getType() == messageType).forEach(m -> sb.append("\t\t" + m.getType() + " " + m.getCode() + "\t" + ((m instanceof SimpleMessage) ? ((SimpleMessage) m).message : "") + "\n"));
      throw new AssertionError("The MessageStore contains additional messages of type [" + messageType + "] that were not expected." + sb);
    }

    return this;
  }

  /**
   * Verifies that the system contains the given info message(s). The message(s) might be in the request, flash, session or application scopes.
   *
   * @param messages The fully rendered info message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsInfos(String... messages) {
    return assertContainsMessages(MessageType.INFO, messages);
  }

  /**
   * Verifies that the system contains the given message(s). The message(s) might be in the request, flash, session or application scopes.
   *
   * @param type     The message type (ERROR, INFO, WARNING).
   * @param messages The fully rendered message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsMessages(MessageType type, String... messages) {
    Set<String> inMessageStore = new HashSet<>();
    List<Message> msgs = messageObserver.getGeneralMessages();
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
    List<FieldMessage> messages = messageObserver.getFieldMessages().values().stream().flatMap(Collection::stream).filter(m -> m.getType() == messageType).collect(Collectors.toList());
    if (messages.isEmpty()) {
      return this;
    }

    StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
    messages.sort(Comparator.comparing(FieldMessage::getField));
    messages.forEach(m -> sb.append("\t\t").append(m.getType()).append("\tField: ").append(m.getField()).append(" Code: ").append(m.getCode()).append("\t").append((m instanceof SimpleFieldMessage) ? ((SimpleFieldMessage) m).message : "").append("\n"));
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
   * Verifies that the system has no general INFO messages.
   *
   * @return This.
   */
  public RequestResult assertContainsNoInfos() {
    return assertContainsNoMessages(MessageType.INFO);
  }

  /**
   * Verifies that the system has no general messages of the specified type.
   *
   * @param messageType The message type
   * @return This.
   */
  public RequestResult assertContainsNoMessages(MessageType messageType) {
    List<Message> messages = messageObserver.getGeneralMessages().stream().filter(m -> m.getType() == messageType).toList();
    if (messages.isEmpty()) {
      return this;
    }

    StringBuilder sb = new StringBuilder("\n\tMessageStore contains:\n");
    //noinspection StringConcatenationInsideStringBufferAppend
    messages.forEach(m -> sb.append("\t\t" + m.getType() + "\t" + m.getCode() + "\t" + ((m instanceof SimpleMessage) ? ((SimpleMessage) m).message : "") + "\n"));
    throw new AssertionError("The MessageStore contains the following general errors.]" + sb);
  }

  /**
   * Verifies that the system has no general WARNING messages.
   *
   * @return This.
   */
  public RequestResult assertContainsNoWarnings() {
    return assertContainsNoMessages(MessageType.WARNING);
  }

  /**
   * Verifies that the system contains the given warning message(s). The message(s) might be in the request, flash, session or application scopes.
   *
   * @param messages The fully rendered warning message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsWarnings(String... messages) {
    return assertContainsMessages(MessageType.WARNING, messages);
  }

  /**
   * Verifies the response Content-Length.
   *
   * @param expected The expected Content-Length.
   * @return This.
   */
  public RequestResult assertContentLength(long expected) {
    String actual = response.getHeader(Headers.ContentLength);
    if (actual == null || Long.parseLong(actual) != expected) {
      throw new AssertionError("Content-Length [" + actual + "] is not equal to the expected value [" + expected + "]");
    }

    return this;
  }

  /**
   * Verifies the response Content-Type.
   *
   * @param contentType The expected content-type
   * @return This.
   */
  public RequestResult assertContentType(String contentType) {
    String actual = response.getHeader(Headers.ContentType);
    if (actual == null || !actual.equals(contentType)) {
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
  public RequestResult assertCookie(String name, ThrowingConsumer<CookieAsserter> consumer)
      throws Exception {
    Cookie actual = getCookieOrThrow(name);
    if (consumer != null) {
      consumer.accept(new CookieAsserter(actual));
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
    Cookie actual = getCookieOrThrow(name);
    if (!value.equals(actual.value)) {
      throw new AssertionError("Cookie [" + name + "] with value [" + actual.value + "] was not equal to the expected value [" + value + "]"
          + "\nActual cookie:\n"
          + cookieToString(actual));
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
    Cookie actual = getCookieOrThrow(name);
    if (actual.value != null && (actual.maxAge == null || actual.maxAge != 0)) {
      throw new AssertionError("Cookie [" + name + "] was not deleted. The value is [" + actual.value + "] and the maxAge is [" + actual.maxAge + "]"
          + "\nActual cookie:\n"
          + cookieToString(actual));
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
      throw new AssertionError("Cookie [" + name + "] was not expected to be found in the response.\n"
          + "Cookies found:\n"
          + response.getCookies().stream().map(this::convert).map(this::cookieToString).collect(Collectors.joining("\n")));
    }
    return this;
  }

  /**
   * Verifies the response encoding.
   *
   * @param encoding The expected content-type
   * @return This.
   */
  public RequestResult assertEncoding(Charset encoding) {
    Charset actual = getCharset();
    if (actual != null && !actual.equals(encoding)) {
      throw new AssertionError("Character Encoding [" + actual + "] is not equal to the expected value [" + encoding + "]");
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
  public RequestResult assertEncryptedCookie(String name, ThrowingConsumer<Cookie> consumer) throws Exception {
    Cookie actual = getCookieOrThrow(name);

    Encryptor encryptor = injector.getInstance(Encryptor.class);
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    ThrowingFunction<byte[], String> oldFunction = r -> objectMapper.readerFor(String.class).readValue(r);
    ThrowingFunction<byte[], String> newFunction = r -> new String(r, StandardCharsets.UTF_8);
    actual.value = CookieTools.fromCookie(actual.value, true, encryptor, oldFunction, newFunction);
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
  public RequestResult assertEncryptedCookie(String name, String value) throws Exception {
    Cookie actual = getCookieOrThrow(name);

    Encryptor encryptor = injector.getInstance(Encryptor.class);
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    ThrowingFunction<byte[], String> oldFunction = r -> objectMapper.readerFor(String.class).readValue(r);
    ThrowingFunction<byte[], String> newFunction = r -> new String(r, StandardCharsets.UTF_8);
    String actualDecrypted = CookieTools.fromCookie(actual.value, true, encryptor, oldFunction, newFunction);
    if (!Objects.equals(value, actualDecrypted)) {
      throw new AssertionError("Cookie [" + name + "] with decrypted value [" + actualDecrypted + "] was not equal to the expected value [" + value + "]"
          + "\nActual cookie:\n"
          + cookieToString(actual));
    }

    return this;
  }

  /**
   * @param consumer a consumer of the HTML asserter
   * @return return a new HTML asserter
   * @throws Exception exceptions that happen..
   */
  public RequestResult assertHTML(ThrowingConsumer<HTMLAsserter> consumer) throws Exception {
    consumer.accept(new HTMLAsserter(this));
    return this;
  }

  /**
   * Verifies that the HTTP response contains the specified header.
   *
   * @param name     the name of the HTTP response header
   * @param expected the value of the header
   * @return This.
   */
  public RequestResult assertHeaderContains(String name, String expected) {
    String actual = response.getHeader(name);
    if ((actual == null && expected != null) || (actual != null && !actual.contains((expected)))) {
      StringBuilder responseHeaders = new StringBuilder();
      response.headers.forEach((k, v) -> responseHeaders.append("\t").append(k).append(": ").append(v).append("\n"));
      throw new AssertionError("Header [" + name + "] with value [" + (actual == null ? null : String.join(", ", actual)) + "] was not equal to the expected value [" + expected + "].\n\nResponse Headers:\n" + responseHeaders);
    }
    return this;
  }

  /**
   * Verifies that the HTTP response does not contain the specified header.
   *
   * @param header the name of the HTTP response header
   * @return This.
   */
  public RequestResult assertHeaderDoesNotContain(String header) {
    List<String> actual = null;
    for (String key : response.headers.keySet()) {
      if (key.equalsIgnoreCase(header)) {
        actual = response.headers.get(key);
        break;
      }
    }

    if (actual != null && !actual.isEmpty()) {
      StringBuilder responseHeaders = new StringBuilder();
      response.headers.forEach((k, v) -> responseHeaders.append("\t").append(k).append(": ").append(v).append("\n"));
      throw new AssertionError("Header [" + header + "] with value [" + String.join(", ", actual) + "] was not expected in the HTTP response.\n\nResponse Headers:\n" + responseHeaders);
    }

    return this;
  }

  /**
   * Verifies that the HTTP response contains the specified header.
   *
   * @param header the name of the HTTP response header
   * @return This.
   */
  public RequestResult assertHeaderExists(String header) {
    List<String> actual = null;
    for (String key : response.headers.keySet()) {
      if (key.equalsIgnoreCase(header)) {
        actual = response.headers.get(key);
        break;
      }
    }

    if (actual == null) {
      StringBuilder responseHeaders = new StringBuilder();
      response.headers.forEach((k, v) -> responseHeaders.append("\t").append(k).append(": ").append(v).append("\n"));
      throw new AssertionError("Header [" + header + "] is missing from the response.\n\nResponse Headers:\n" + responseHeaders);
    }

    return this;
  }

  /**
   * Verifies that the response body is equal to the JSON created from the given object. The object is marshalled using Jackson.
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
    T response = objectMapper.readValue(getBodyAsString(), type);
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
    assertJSONEquals(objectMapper, getBodyAsString(), json);
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
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    String expected = BodyTools.processTemplateForAssertion(jsonFile, appendArray(values, "_to_milli", new ZonedDateTimeToMilliSeconds()));
    _assertJSONEquals(objectMapper, getBodyAsString(), expected, true, jsonFile);
    return this;
  }

  /**
   * De-serialize the JSON response using the type provided. To use actual values in the JSON use ${actual.foo} to use the property named
   * <code>foo</code>.
   *
   * @param type     The object type of the JSON.
   * @param jsonFile The JSON file to load and compare to the JSON response.
   * @param values   key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public <T> RequestResult assertJSONFileWithActual(Class<T> type, Path jsonFile, Object... values) throws IOException {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    T actual = objectMapper.readValue(getBodyAsString(), type);

    return assertJSONFile(jsonFile, appendArray(values, "actual", actual, "_to_milli", new ZonedDateTimeToMilliSeconds()));
  }

  /**
   * Verifies the key of each pair which is a JSON pointer has a corresponding value.
   *
   * @param pairs the key pairs
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public RequestResult assertJSONValuesAt(Object... pairs) throws IOException {
    if (pairs.length % 2 != 0) {
      String key = pairs[pairs.length - 1].toString();
      throw new IllegalArgumentException("Invalid mapping values. Must have a multiple of 2. Missing value for key [" + key + "]");
    }

    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    JsonNode actual = objectMapper.readTree(getBodyAsString());

    for (int i = 0; i < pairs.length; i = i + 2) {
      String pointer = pairs[i].toString();
      String expectedValue = pairs[i + 1].toString();
      String actualValue = actual.at(pointer).asText();
      if (!Objects.equals(actualValue, expectedValue)) {
        throw new AssertionError("Expected [" + expectedValue + "] but found [" + actualValue + "].\nActual JSON body:\n"
            + objectMapper.writerWithDefaultPrettyPrinter()
                          .withFeatures(SerializationFeature.INDENT_OUTPUT)
                          .with(new DefaultPrettyPrinter().withArrayIndenter(new DefaultIndenter()))
                          .writeValueAsString(actual));
      }
    }

    return this;
  }

  /**
   * Verifies that the normalized body equals the given string.
   *
   * @param string The string to compare against the body.
   * @return This.
   */
  public RequestResult assertNormalizedBody(String string) {
    String body = getBodyAsString();
    if (!normalize(body).equals(string)) {
      throw new AssertionError("The body doesn't match the expected output. Expected [" + string + "] but found [" + body.trim().replace("\r\n", "\n").replace("\r", "\n") + "]");
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
      return assertNormalizedBody(normalize(Files.readString(path)));
    }
    return assertNormalizedBody(normalize(BodyTools.processTemplateForAssertion(path, values)));
  }

  /**
   * Verifies that the redirect URI is the given URI. The order of the parameters added using the builder is not important.
   *
   * @param uri      The base redirect URI.
   * @param consumer The consumer to accept a URI builder to add parameters
   * @return This.
   */
  public RequestResult assertRedirect(String uri, Consumer<TestURIBuilder> consumer) {
    String redirect = response.getHeader(Headers.Location);
    if (redirect == null) {
      throw new AssertionError("\nActual redirect was null. Why do you want to assert on it? Status code was [" + response.getStatus() + "]");
    }

    TestURIBuilder builder = TestURIBuilder.builder(uri);
    consumer.accept(builder);

    String expectedUri = builder.build();
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
    String redirect = response.getHeader(Headers.Location);
    if (redirect == null) {
      throw new AssertionError("\nActual redirect was null. \nExpected:\t" + expectedUri);
    }

    assertRedirectEquality(expectedUri);
    return this;
  }

  /**
   * Verifies that the response body is equal to the JSON created from the given object,  preserving the order of JSON arrays. The object is
   * marshalled using Jackson.
   *
   * @param object The object.
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public RequestResult assertSortedJSON(Object object) throws IOException {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    String json = objectMapper.writeValueAsString(object);
    return assertSortedJSON(json);
  }

  /**
   * Verifies that the response body is equal to the given JSON text,  preserving the order of JSON arrays.
   *
   * @param json The JSON text.
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public RequestResult assertSortedJSON(String json) throws IOException {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    assertSortedJSONEquals(objectMapper, getBodyAsString(), json);
    return this;
  }

  /**
   * Verifies that the response body is equal to the given JSON text file,  preserving the order of JSON arrays.
   *
   * @param jsonFile The JSON file to load and compare to the JSON response.
   * @param values   key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public RequestResult assertSortedJSONFile(Path jsonFile, Object... values) throws IOException {
    return assertSortedJSON(BodyTools.processTemplate(jsonFile, appendArray(values, "_to_milli", new ZonedDateTimeToMilliSeconds())));
  }

  /**
   * De-serialize the JSON response using the type provided, preserving the order of JSON arrays. To use actual values in the JSON use ${actual.foo}
   * to use the property named <code>foo</code>.
   *
   * @param type     The object type of the JSON.
   * @param jsonFile The JSON file to load and compare to the JSON response.
   * @param values   key value pairs of replacement values for use in the JSON file.
   * @return This.
   * @throws IOException If the JSON marshalling failed.
   */
  public <T> RequestResult assertSortedJSONFileWithActual(Class<T> type, Path jsonFile, Object... values)
      throws IOException {
    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    T actual = objectMapper.readValue(getBodyAsString(), type);

    return assertSortedJSONFile(jsonFile, appendArray(values, "actual", actual, "_to_milli", new ZonedDateTimeToMilliSeconds()));
  }

  /**
   * Verifies that the response status code is equal to the given code.
   *
   * @param statusCode The status code.
   * @return This.
   */
  public RequestResult assertStatusCode(int statusCode) {
    if (response.getStatus() != statusCode) {
      StringBuilder sb = new StringBuilder("Status code [" + response.getStatus() + "] was not equal to [" + statusCode + "]\n");

      // Append any General Messages to the error message to aid in debug
      List<Message> generatorMessages = messageObserver.getGeneralMessages();
      if (!generatorMessages.isEmpty()) {
        sb.append("\nThe following general error messages were returned in the message store:\n\n");
      }
      generatorMessages.forEach(m -> sb.append("\t\t").append(m.getType()).append("\t").append(m.getCode()).append("\t").append((m instanceof SimpleMessage) ? ((SimpleMessage) m).message : "").append("\n"));

      // Append any Field Messages to the error message to aid in debug
      List<FieldMessage> fieldMessages = messageObserver.getFieldMessages().values().stream().flatMap(Collection::stream).toList();
      if (!fieldMessages.isEmpty()) {
        sb.append("\nThe following field error messages were returned in the message store:\n\n");
      }
      //noinspection StringConcatenationInsideStringBufferAppend
      fieldMessages.forEach(m -> sb.append("\t\t" + m.getType() + "\tField: " + m.getField() + " Code: " + m.getCode() + "\t" + ((m instanceof SimpleFieldMessage) ? ((SimpleFieldMessage) m).message : "") + "\n"));

      String redirect = response.getHeader(Headers.Location);
      sb.append("\nRedirect: [").append(redirect).append("]\n").append("Response body: \n").append(getBodyAsString());

      if (response.exception != null) {
        sb.append("\nException:\n")
          .append(response.exception);
      }

      throw new AssertionError(sb.toString());
    }

    return this;
  }

  /**
   * Can be called to perform custom assertions or business logic.
   *
   * @param consumer The consumer that accepts the RequestResult.
   * @return This.
   */
  public RequestResult custom(ThrowingConsumer<RequestResult> consumer) throws Exception {
    consumer.accept(this);
    return this;
  }

  /**
   * Can be called to perform custom assertions or business logic.
   *
   * @param runnable The runnable to stuff.
   * @return This.
   */
  public RequestResult custom(ThrowingRunnable runnable) throws Exception {
    runnable.run();
    return this;
  }

  /**
   * Attempt to submit the form found in the response body.
   *
   * @param selector The selector used to find the form in the DOM
   * @param result   A consumer for the request result from following the redirect.
   * @return This.
   */
  public RequestResult executeFormPostInResponseBody(String selector, ThrowingConsumer<RequestResult> result)
      throws Exception {
    executeFormPost(selector, null, result);
    return this;
  }

  /**
   * Attempt to submit the form found in the response body.
   *
   * @param selector  The selector used to find the form in the DOM
   * @param domHelper A consumer for the DOM Helper
   * @param result    A consumer for the request result from following the redirect.
   * @return This.
   */
  public RequestResult executeFormPostInResponseBody(String selector, ThrowingConsumer<DOMHelper> domHelper,
                                                     ThrowingConsumer<RequestResult> result)
      throws Exception {
    executeFormPost(selector, domHelper, result);
    return this;
  }

  /**
   * Attempt to submit the form found in the response body.
   *
   * @param selector The selector used to find the form in the DOM
   * @param result   A consumer for the request result from following the redirect.
   * @return This.
   */
  public RequestResult executeFormPostInResponseBodyReturnPostResult(String selector,
                                                                     ThrowingConsumer<RequestResult> result)
      throws Exception {
    return executeFormPost(selector, null, result);
  }

  /**
   * Attempt to submit the form found in the response body.
   *
   * @param selector  The selector used to find the form in the DOM
   * @param domHelper A consumer for the DOM Helper
   * @param result    A consumer for the request result from following the redirect.
   * @return This.
   */
  public RequestResult executeFormPostInResponseBodyReturnPostResult(String selector,
                                                                     ThrowingConsumer<DOMHelper> domHelper,
                                                                     ThrowingConsumer<RequestResult> result)
      throws Exception {
    return executeFormPost(selector, domHelper, result);
  }

  /**
   * Execute the redirect and accept a consumer to assert on the response.
   *
   * @param consumer The request result from following the redirect.
   * @return This.
   */
  public RequestResult executeRedirect(ThrowingConsumer<RequestResult> consumer) throws Exception {
    executeRedirectReturningResult(consumer);
    return this;
  }

  /**
   * Execute the redirect and accept a consumer to assert on the response.
   *
   * @param consumer The request result from following the redirect.
   * @return This.
   */
  public RequestResult executeRedirectReturnResult(ThrowingConsumer<RequestResult> consumer) throws Exception {
    return executeRedirectReturningResult(consumer);
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
   * @return the body as a byte array.
   */
  public byte[] getBody() {
    return response.wasSuccessful() ? response.successResponse : response.errorResponse;
  }

  /**
   * @return the HTTP response body as a string or empty string if there is no response body.
   */
  public String getBodyAsString() {
    if (body == null) {
      byte[] bytes = getBody();
      if (bytes != null && bytes.length > 0) {
        Charset encoding = getCharset();
        body = new String(bytes, encoding != null ? encoding : StandardCharsets.UTF_8);
      } else {
        body = "";
      }
    }

    return body;
  }

  /**
   * Retrieve a cookie by name. If the cookie does not exist in the response, this returns null.
   *
   * @param name The name of the cookie.
   * @return The Cookie or null.
   */
  public Cookie getCookie(String name) {
    List<Cookie> cookies = response.getCookies()
                                   .stream()
                                   .filter(c -> c.name.equals(name))
                                   .map(this::convert)
                                   .toList();

    if (cookies.size() == 0) {
      return null;
    } else if (cookies.size() > 1) {
      throw new AssertionError("Expected a single cookie with name [" + name + "] but found [" + cookies.size() + "]."
          + "\nCookies found:\n"
          + cookies.stream().map(this::cookieToString).collect(Collectors.joining("\n")));
    }

    return cookies.get(0);
  }

  /**
   * Retrieve cookies by name. If the cookie does not exist in the response, this returns null.
   *
   * @param name The name of the cookie.
   * @return The list of cookies with this name, or an empty list.
   */
  public List<Cookie> getCookies(String name) {
    return response.getCookies()
                   .stream()
                   .filter(c -> c.name.equals(name))
                   .map(this::convert)
                   .collect(Collectors.toList());
  }

  /**
   * If the test is false, apply the consumer.
   * <p>
   * <pre>
   *   .ifFalse(foo.isBar(), () -> result.assertBodyDoesNotContain("bar"))
   * </pre>
   *
   * @param test     The boolean test to indicate if the runner should be used.
   * @param runnable The runnable to 'run'
   * @return This.
   */
  public RequestResult ifFalse(boolean test, ThrowingRunnable runnable) throws Exception {
    if (!test) {
      runnable.run();
    }
    return this;
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
   * If the test is true, apply the the <code>thenConsumer</code>, else apply the <code>elseConsumer</code>.
   *
   * @param test         the boolean test
   * @param thenConsumer the consumer to run if the test is true
   * @param elseConsumer the consumer to run if the test is false
   * @return This.
   */
  public RequestResult ifThenElse(boolean test, ThrowingConsumer<RequestResult> thenConsumer,
                                  ThrowingConsumer<RequestResult> elseConsumer) throws Exception {
    if (test) {
      thenConsumer.accept(this);
    } else {
      elseConsumer.accept(this);
    }

    return this;
  }


  /**
   * If the test is true, apply the consumer. Example:
   * <pre>
   *   .ifTrue(foo.isBar(), () -> result.assertBodyContains("bar"))
   * </pre>
   *
   * @param test     The boolean test to indicate if the runnable should be used.
   * @param runnable The runnable to 'run'.
   * @return This.
   */
  public RequestResult ifTrue(boolean test, ThrowingRunnable runnable) throws Exception {
    if (test) {
      runnable.run();
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
   * Repeat n times
   *
   * @param n        the number of times
   * @param consumer the index consumer
   * @return This.
   */
  public RequestResult repeat(int n, ThrowingConsumer<Integer> consumer) {
    IntStream.rangeClosed(1, n).forEach(index -> {
      try {
        consumer.accept(index);
      } catch (Throwable e) {
        throw new AssertionError("Iteration [" + n + "]\n" + e.getMessage(), e.getCause());
      }
    });
    return this;
  }

  /**
   * Can be called to set up objects for assertions.
   *
   * @param consumer The consumer that accepts the RequestResult.
   * @return This.
   */
  public RequestResult setup(ThrowingConsumer<RequestResult> consumer) throws Exception {
    consumer.accept(this);
    return this;
  }

  /**
   * Can be called to set up objects for assertions.
   *
   * @param runnable The runnable to stuff.
   * @return This.
   */
  public RequestResult setup(ThrowingRunnable runnable) throws Exception {
    runnable.run();
    return this;
  }

  private RequestResult _assertBodyContains(boolean escape, String... strings) {
    if (escape) {
      for (int i = 0; i < strings.length; i++) {
        strings[i] = escape(strings[i]);
      }
    }

    String body = getBodyAsString();
    for (String string : strings) {
      if (!body.contains(string)) {
        String redirect = response.getHeader(Headers.Location);
        throw new AssertionError("Body didn't contain [" + string + "]\nRedirect: [" + redirect + "]\nBody:\n" + body);
      }
    }

    return this;
  }

  private RequestResult _assertBodyContainsMessagesFromKey(MessageProvider messageProvider, boolean contains, String key, boolean escape,
                                                           Object... values) {
    String message;
    try {
      message = messageProvider.getMessage(key, values);
    } catch (MissingMessageException e) {
      // For good measure, let's see if we can find his message from a flash scope cookie.
      // - At runtime, when we move messages to the flash scope, we have already resolved the message.
      //   This means that the message that is displayed to the user is not resolved using the current
      //   action invocation, but the action that requested the redirect. This change allows
      //   this assertion to behave similar to how it works at runtime.
      CookieFlashScope cookieFlashScope = injector.getInstance(CookieFlashScope.class);
      List<Message> flashMessages = cookieFlashScope.get();
      message = flashMessages.stream()
                             .filter(m -> m.getCode().equals(key))
                             .findFirst()
                             .map(Object::toString)
                             .orElse(null);

      if (message == null) {
        throw e;
      }
    }

    if (escape) {
      message = escape(message);
    }

    String body = getBodyAsString();
    if (contains != body.contains(message)) {
      String redirect = response.getHeader(Headers.Location);
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
    String redirect = response.getHeader(Headers.Location);
    SortedMap<String, List<String>> actual = uriToMap(redirect);
    SortedMap<String, List<String>> expected = uriToMap(expectedUri);

    if (!actual.equals(expected)) {
      // Replace any 'actual' values requested and then try again
      boolean recheck = replaceWithActualValues(actual, expected);
      if (!recheck || !actual.equals(expected)) {
        throw new AssertionError("Actual redirect not equal to the expected. Expected [" + expectedUri + "] but found [" + redirect + "]");
      }
    }
  }

  private Cookie convert(com.inversoft.http.Cookie cookie) {
    return new Cookie().with(c -> c.domain = cookie.domain)
                       .with(c -> c.expires = cookie.expires)
                       .with(c -> c.httpOnly = cookie.httpOnly)
                       .with(c -> c.maxAge = cookie.maxAge)
                       .with(c -> c.name = cookie.name)
                       .with(c -> c.path = cookie.path)
                       .with(c -> c.sameSite = cookie.sameSite != null ? SameSite.valueOf(cookie.sameSite.name()) : null)
                       .with(c -> c.secure = cookie.secure)
                       .with(c -> c.value = cookie.value);
  }

  private String cookieToString(Cookie cookie) {
    return "Set-Cookie: " + cookie.toResponseHeader();
  }

  private String escape(String s) {
    // TODO
    // Should we be escaping everything that FreeMarker does? We could also just build a small template
    // and render it to allow FreeMarker to escape it like we do with BodyTools
    return s.replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");
  }

  private RequestResult executeFormPost(String selector, ThrowingConsumer<DOMHelper> domHelper,
                                        ThrowingConsumer<RequestResult> result)
      throws Exception {
    String body = getBodyAsString();
    Document document = Jsoup.parse(body);
    Element form = document.selectFirst(selector);

    if (form == null) {
      throw new AssertionError("Unable to find a form in the body using the provided select [" + selector + "]. Response body\n" + body);
    }

    if (domHelper != null) {
      domHelper.accept(new DOMHelper(body, document));
    }

    String uri = form.attr("action");
    // Try to handle relative URLs, assuming they are relative to the current request. Maybe a naive assumption, but for our use cases it works ok.
    // - We could also just allow the caller to pass in the URI
    if (!uri.startsWith("/")) {
      String baseURI = request.getPath();
      if (baseURI.contains("/")) {
        baseURI = request.getPath().substring(0, baseURI.lastIndexOf('/') + 1);
        uri = baseURI + uri;
      }
    }

    RequestBuilder rb = new RequestBuilder(uri, injector, userAgent, messageObserver, port);

    // Handle input, select and textarea
    for (Element element : form.select("input,select,textarea")) {
      if (element.hasAttr("name") && !element.hasAttr("disabled")) {
        if (element.is("select")) {
          for (Element option : element.select("option")) {
            if (option.hasAttr("selected")) {
              rb.withParameter(element.attr("name"), option.val());
            }
          }
        } else if (element.is("[type=radio],[type=checkbox]")) {
          if (element.hasAttr("checked")) {
            if (element.hasAttr("value")) {
              rb.withParameter(element.attr("name"), element.val());
            } else {
              rb.withParameter(element.attr("name"), "on");
            }
          }
        } else {
          rb.withParameter(element.attr("name"), element.val());
        }
      }
    }

    String method = form.attr("method");
    RequestResult requestResult = null;
    if (method == null || method.equalsIgnoreCase(Methods.GET)) {
      requestResult = rb.get();
    } else if (method.equalsIgnoreCase(Methods.POST)) {
      requestResult = rb.post();
    }

    result.accept(requestResult);
    return requestResult;
  }

  private RequestResult executeRedirectReturningResult(ThrowingConsumer<RequestResult> consumer) throws Exception {
    String redirect = response.getHeader(Headers.Location);
    String baseURI = redirect.contains("?") ? redirect.substring(0, redirect.indexOf('?')) : redirect;
    String originalURI = baseURI;
    String newRedirect = redirect;

    // Handle a relative URI
    if (!baseURI.startsWith("/")) {
      int index = request.getPath().lastIndexOf('/');
      String baseURL = request.getPath().substring(0, index);
      baseURI = baseURL + "/" + baseURI;
      newRedirect = redirect.replace(originalURI, baseURI);
    }

    RequestBuilder rb = new RequestBuilder(baseURI, injector, userAgent, messageObserver, port);
    if (baseURI.length() != newRedirect.length()) {
      String params = newRedirect.substring(newRedirect.indexOf('?') + 1);
      QueryStringTools.parseQueryString(params).forEach(rb::withURLParameters);
    }

    RequestResult result = rb.get();
    consumer.accept(result);
    return result;
  }

  private Charset getCharset() {
    HeaderValue headerValue = io.fusionauth.http.util.HTTPTools.parseHeaderValue(response.getHeader(Headers.ContentType));
    String charsetParam = headerValue.parameters().get(ContentTypes.CharsetParameter);
    Charset actual = null;
    if (charsetParam != null) {
      actual = Charset.forName(charsetParam);
    }

    return actual;
  }

  private Cookie getCookieOrThrow(String name) {
    Cookie actual = getCookie(name);
    if (actual == null) {
      throw new AssertionError("Cookie [" + name + "] was not found in the response.\n"
          + "Cookies found:\n"
          + response.getCookies().stream().map(this::convert).map(this::cookieToString).collect(Collectors.joining("\n")));
    }
    return actual;
  }

  private MessageProvider getMessageProviderToLookupMessages() {
    // Now that we are going across the wire, this likely will never be able to fetch the action that was just used.
    // Therefore, we just try to build a valid action invocation so at least the messages are correct, but the values
    // won't exist.
    MessageProvider messageProvider = get(MessageProvider.class);
    ActionInvocationStore actionInvocationStore = get(ActionInvocationStore.class);
    ActionMapper actionMapper = get(ActionMapper.class);

    // Using the ActionMapper so that URL segments are properly handled and the correct URL is used for message lookups.
    ActionInvocation actionInvocation = actionMapper.map(null, request.getPath(), true);
    actionInvocationStore.setCurrent(actionInvocation);

    return messageProvider;
  }

  private String normalize(String input) {
    return input.trim().replace("\r\n", "\n").replace("\r", "\n");
  }

  private boolean replaceWithActualValues(SortedMap<String, List<String>> actual, Map<String, List<String>> expected) {
    boolean recheck = false;

    // Bail early they are not even the same size
    if (actual.keySet().size() != expected.keySet().size()) {
      // Check for optional parameters, if we don't have any, we know we are done.
      if (expected.values().stream().noneMatch(v -> v.size() > 0 && v.get(0).equals("___optional___"))) {
        return false;
      }
    }

    List<String> actualKeys = new ArrayList<>(actual.keySet());
    List<String> expectedKeys = new ArrayList<>(expected.keySet());

    int i, j;
    for (i = j = 0; j < expectedKeys.size(); ) {
      if (!Objects.equals(actualKeys.get(i), expectedKeys.get(j))) {
        // We may have optional values, continue;
        List<String> expectedValues = expected.get(expectedKeys.get(j));
        if (expectedValues.size() > 0 && expectedValues.get(0).equals("___optional___")) {
          j++;
          continue;
        }

        return false;
      }

      // Replace any ___actual___ or ___optional___ if they exist.
      List<String> expectedValues = expected.get(expectedKeys.get(j));
      if (expectedValues != null && expectedValues.size() > 0) {
        if (expectedValues.get(0).equals("___actual___") || expectedValues.get(0).equals("___optional___")) {
          expectedValues.clear();
          expectedValues.addAll(actual.get(actualKeys.get(i)));
          recheck = true;
        }
      }

      i++;
      j++;
    }

    // Remove any optional keys if they aren't in the actual key set
    if (expected.keySet().removeIf(k -> !actualKeys.contains(k) && expected.get(k).get(0).equals("___optional___"))) {
      recheck = true;
    }

    return recheck;
  }

  private SortedMap<String, List<String>> uriToMap(String uri) {
    SortedMap<String, List<String>> map = new TreeMap<>();
    int queryIndex = uri.indexOf('?');
    int fragmentIndex = uri.indexOf('#');
    if (fragmentIndex != -1) {
      // If the fragment is followed by a '/' then ignore it.
      if ((fragmentIndex + 1) < uri.length() && uri.charAt(fragmentIndex + 1) == '/') {
        fragmentIndex = -1;
      }
    }

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
  public interface ThrowingBiConsumer<T, U> {
    void accept(T t, U u) throws Exception;
  }

  @FunctionalInterface
  public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
  }

  public static class DOMHelper {
    public String body;

    public Document document;

    public DOMHelper(String body, Document document) {
      this.body = body;
      this.document = document;
    }

    /**
     * Perform any custom modifications to the HTML document
     *
     * @return this.
     */
    public DOMHelper custom(ThrowingRunnable runnable) throws Exception {
      runnable.run();
      return this;
    }

    /**
     * Perform any custom modifications to the HTML document
     *
     * @param consumer the HTML document consumer
     * @return this.
     */
    public DOMHelper custom(ThrowingConsumer<Document> consumer) throws Exception {
      consumer.accept(document);
      return this;
    }

    /**
     * Remove an attribute from a DOM element.
     *
     * @param selector the DOM selector
     * @param name     the name of the attribute to remove
     * @return this.
     */
    public DOMHelper removeAttribute(String selector, String name) {
      Element element = document.selectFirst(selector);
      if (element == null) {
        throw new AssertionError("Expected at least one element to match the selector " + selector + ". Found [0] elements instead. Unable to set element value.\n\nActual body:\n" + body);
      }

      element.removeAttr(name);

      return this;
    }

    /**
     * Set an attribute w/ value on a DOM element.
     *
     * @param selector the DOM selector
     * @param name     the name of the attribute
     * @param value    the value of the attribute
     * @return this.
     */
    public DOMHelper setAttribute(String selector, String name, String value) {
      Element element = document.selectFirst(selector);
      if (element == null) {
        throw new AssertionError("Expected at least one element to match the selector " + selector + ". Found [0] elements instead. Unable to set element value.\n\nActual body:\n" + body);
      }

      element.attr(name, value);

      return this;
    }

    public DOMHelper setChecked(String selector, boolean value) {
      Element element = document.selectFirst(selector);
      if (element == null) {
        throw new AssertionError("Expected at least one element to match the selector " + selector + ". Found [0] elements instead. Unable to set element value.\n\nActual body:\n" + body);
      }

      if (element.is("input[type=radio]") && value) {
        Elements elements = document.select(element.tagName().toLowerCase() + "[type=radio][name=" + element.attr("name") + "]");
        for (Element e : elements) {
          e.attr("checked", false);
        }
      }

      element.attr("checked", value);
      return this;
    }

    public DOMHelper setValue(String selector, Object value) {
      if (value != null) {
        Element element = document.selectFirst(selector);
        if (element == null) {
          throw new AssertionError("Expected at least one element to match the selector " + selector + ". Found [0] elements instead. Unable to set element value.\n\nActual body:\n" + body);
        }

        // Handle a select element
        if (element.is("select")) {
          // Remove the selected attribute for each option, add it to the one that matches the requested value.
          for (Element option : element.getElementsByTag("option")) {
            if (option.attr("value").equals(value.toString())) {
              option.attr("selected", "selected");
            } else {
              option.removeAttr("selected");
            }
          }
        } else {
          element.val(value.toString());
        }
      }

      return this;
    }
  }

  public static class HTMLAsserter {
    public Document document;

    public RequestResult requestResult;

    public HTMLAsserter(RequestResult requestResult) {
      this.requestResult = requestResult;
      document = Jsoup.parse(requestResult.getBodyAsString());
    }

    /**
     * Ensure a single element matches the provided selector.
     *
     * @param selector the DOM selector
     * @param expected the number of expected matches for this selector in the DOM.
     * @return this.
     */
    public HTMLAsserter assertElementCount(String selector, int expected) {
      Elements elements = document.select(selector);
      if (elements.size() != expected) {
        throw new AssertionError("Expected [" + expected + "] elements to match the selector " + selector + " but found [" + elements.size() + "] elements instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Ensure no elements match the provided selector.
     *
     * @param selector the DOM selector
     * @return this.
     */
    public HTMLAsserter assertElementDoesNotExist(String selector) {
      Elements elements = document.select(selector);
      if (elements.size() > 0) {
        throw new AssertionError("Expected 0 elements to match the selector " + selector + ". Found [" + (elements.size() + "] elements.\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Assert an element does not have an attribute. The value is not checked.
     *
     * @param selector  the DOM selector
     * @param attribute the attribute you expect
     * @return this.
     */
    public HTMLAsserter assertElementDoesNotHaveAttribute(String selector, String attribute) {
      Element element = selectExpectOne(selector);

      if (element.hasAttr(attribute)) {
        throw new AssertionError("Expected the element not to have attribute [" + attribute + "]." + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Ensure a single element matches the provided selector.
     *
     * @param selector the DOM selector
     * @return this.
     */
    public HTMLAsserter assertElementExists(String selector) {
      selectExpectOne(selector);
      return this;
    }

    /**
     * Assert an element has an attribute. The value is not checked.
     *
     * @param selector  the DOM selector
     * @param attribute the attribute you expect
     * @return this.
     */
    public HTMLAsserter assertElementHasAttribute(String selector, String attribute) {
      Element element = selectExpectOne(selector);

      if (!element.hasAttr(attribute)) {
        throw new AssertionError("Expected the element to have attribute [" + attribute + "]." + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Assert the element has an attribute with a specific value.
     *
     * @param selector  the DOM selector
     * @param attribute the name of the attribute you expect
     * @param value     the value of the attribute you expect
     * @return this.
     */
    public HTMLAsserter assertElementHasAttributeValue(String selector, String attribute, String value) {
      Element element = selectExpectOne(selector);

      if (!element.hasAttr(attribute)) {
        throw new AssertionError("Expected the element to have attribute [" + attribute + "]." + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      String actual = element.attr(attribute);
      if (!value.equals(actual)) {
        throw new AssertionError("Attribute [" + attribute + "] value not equal to expected.\nExpected [" + value + "] but found [" + actual + "]" + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Ensure a single element matches the provided selector and has an expected inner HTML.
     *
     * @param selector          the DOM selector
     * @param expectedInnerHTML the expected inner HTML
     * @return this.
     */
    public HTMLAsserter assertElementInnerHTML(String selector, String expectedInnerHTML) {
      Elements elements = document.select(selector);
      if (elements.size() != 1) {
        throw new AssertionError("Expected a single element to match the selector " + selector + ". Found [" + elements.size() + "] elements instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      Element element = elements.get(0);
      if (!expectedInnerHTML.equals(element.html())) {
        throw new AssertionError("Expected a value of [" + expectedInnerHTML + "] to match the selector " + selector + ". Found [" + element.html() + "] instead." + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Ensure a single element matches the provided selector and is "checked"
     *
     * @param selector the DOM selector
     * @return this.
     */
    public HTMLAsserter assertElementIsChecked(String selector) {
      Elements elements = document.select(selector);
      if (elements.size() != 1) {
        throw new AssertionError("Expected a single element to match the selector " + selector + ". Found [" + elements.size() + "] elements instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      if (!elements.get(0).hasAttr("checked")) {
        throw new AssertionError("Expected the element to be checked." + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Ensure a single element matches the provided selector and is NOT "checked"
     *
     * @param selector the DOM selector
     * @return this.
     */
    public HTMLAsserter assertElementIsNotChecked(String selector) {
      Elements elements = document.select(selector);
      if (elements.size() != 1) {
        throw new AssertionError("Expected a single element to match the selector " + selector + ". Found [" + elements.size() + "] elements instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      if (elements.get(0).hasAttr("checked")) {
        throw new AssertionError("Expected the element NOT to be checked." + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Ensure a single element matches the provided selector and equals the provided value.
     *
     * @param selector the DOM selector
     * @param value    the expected value
     * @return this.
     */
    public HTMLAsserter assertElementValue(String selector, Object value) {
      Elements elements = document.select(selector);
      if (elements.size() != 1) {
        throw new AssertionError("Expected a single element to match the selector " + selector + ". Found [" + elements.size() + "] instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      Element element = elements.get(0);
      if (!element.val().equals(value.toString())) {
        throw new AssertionError("Using the selector [" + selector + "] expected [" + value + "] but found [" + element.val() + "]. Actual matched element: \n\n" + element + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Allow for custom assertions on an element.
     *
     * @param selector the DOM selector
     * @param consumer a consumer that will take the element found by the selector
     * @return this.
     */
    public HTMLAsserter assertOnElement(String selector, Consumer<Element> consumer) {
      Element element = selectExpectOne(selector);
      consumer.accept(element);
      return this;
    }

    /**
     * Assert that a Select option is not selected.
     *
     * @param selector the DOM selector
     * @return this.
     */
    public HTMLAsserter assertOptionIsNotSelected(String selector) {
      Element element = selectExpectOne(selector);

      if (!element.is("option")) {
        throw new AssertionError("Expected the element not be an [option] but found [" + element.tagName().toLowerCase(Locale.ROOT) + "].\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      if (element.hasAttr("selected")) {
        throw new AssertionError("Expected the element not to be selected." + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Assert that an option is selected.
     *
     * @param selector the DOM selector
     * @return this.
     */
    public HTMLAsserter assertOptionIsSelected(String selector) {
      Element element = selectExpectOne(selector);

      if (!element.is("option")) {
        throw new AssertionError("Expected the element not be an [option] but found [" + element.tagName().toLowerCase(Locale.ROOT) + "].\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      if (!element.hasAttr("selected")) {
        throw new AssertionError("Expected the element to be selected." + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return this;
    }

    /**
     * Perform any custom assertions on the parsed HTML document.
     *
     * @param consumer the HTML document consumer
     * @return this.
     */
    public HTMLAsserter custom(ThrowingConsumer<Document> consumer) throws Exception {
      consumer.accept(document);
      return this;
    }

    private Element selectExpectOne(String selector) {
      Elements elements = document.select(selector);
      if (elements.size() != 1) {
        throw new AssertionError("Expected a single element to match the selector " + selector + ". Found [" + elements.size() + "] elements instead." + ((elements.size() == 0) ? "" : "\n\n" + elements) + "\n\nActual body:\n" + requestResult.getBodyAsString());
      }

      return elements.get(0);
    }
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

    @Override
    public TestURIBuilder uri(String uri) {
      return (TestURIBuilder) super.uri(uri);
    }

    @Override
    public TestURIBuilder with(String name, Consumer<QueryStringBuilder> consumer) {
      return (TestURIBuilder) super.with(name, consumer);
    }

    @Override
    public TestURIBuilder with(String name, Object value) {
      return (TestURIBuilder) super.with(name, value);
    }

    /**
     * Add a parameter to the request that you will expect to match the expected. This may be useful if a timestamp oro other random data is returned
     * that is not important to assert on.
     * <p>
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

    /**
     * Add a parameter as optional which means that it is not required to be on the query string, but if it is, the actual value will be used during
     * the assertion.
     * <p>
     * <strong>This is only intended for use during testing.</strong>
     *
     * @param name the parameter name
     * @return this
     */
    public TestURIBuilder withOptional(String name) {
      with(name, "___optional___");
      return this;
    }

    @Override
    public TestURIBuilder withSegment(Object segment) {
      return (TestURIBuilder) super.withSegment(segment);
    }
  }
}

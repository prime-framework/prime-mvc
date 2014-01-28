/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Injector;
import org.primeframework.mock.servlet.MockHttpServletRequest;
import org.primeframework.mock.servlet.MockHttpServletResponse;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Result of a request to the {@link org.primeframework.mvc.test.RequestSimulator}.
 *
 * @author Brian Pontarelli
 */
public class RequestResult {
  public final MockHttpServletRequest request;
  public final MockHttpServletResponse response;
  public final Injector injector;
  public final String body;
  public final String redirect;
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
   * Verifies that the system contains the given error message(s). The message(s) might be in the request, flash, session or
   * application scopes.
   *
   * @param messages The fully rendered error message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsErrors(String... messages) {
    return assertContainsMessages(MessageType.ERROR, messages);
  }

  /**
   * Verifies that the system contains the given info message(s). The message(s) might be in the request, flash, session or
   * application scopes.
   *
   * @param messages The fully rendered info message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsInfos(String... messages) {
    return assertContainsMessages(MessageType.INFO, messages);
  }

  /**
   * Verifies that the system contains the given warning message(s). The message(s) might be in the request, flash, session or
   * application scopes.
   *
   * @param messages The fully rendered warning message(s) (not the code).
   * @return This.
   */
  public RequestResult assertContainsWarnings(String... messages) {
    return assertContainsMessages(MessageType.WARNING, messages);
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
    Set<String> inMessageStore = new HashSet<String>();
    MessageStore messageStore = get(MessageStore.class);
    List<Message> msgs = messageStore.getGeneralMessages();
    for (Message msg : msgs) {
      if (msg.getType() == type) {
        inMessageStore.add(msg.toString());
      }
    }

    if (!inMessageStore.containsAll(asList(messages))) {
      throw new AssertionError("The MessageStore does not contain the [" + type + "] message " + asList(messages) + "");
    }

    return this;
  }

  /**
   * Verifies that the redirect URI is the given URI.
   *
   * @param uri The redirect URI.
   * @return This.
   */
  public RequestResult assertRedirect(String uri) {
    if (redirect == null || !redirect.equals(uri)) {
      throw new AssertionError("Redirect [" + redirect + "] was not equal to [" + uri + "]");
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
      throw new AssertionError("Status code [" + this.statusCode+ "] was not equal to [" + statusCode + "]");
    }

    return this;
  }
}
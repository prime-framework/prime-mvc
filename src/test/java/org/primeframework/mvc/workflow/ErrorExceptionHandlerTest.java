/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.workflow;

import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;
import org.primeframework.mvc.validation.ValidationException;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * @author James Humphrey
 */
public class ErrorExceptionHandlerTest {

  @Test
  public void errorExceptionWithCustomResultCode() {
    ErrorException errorException = new MockErrorExceptionWithCode();
    MVCConfiguration configuration = new MockConfiguration();

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    expect(messageProvider.getMessage("[" + errorException.getClass().getSimpleName() + "]", errorException.args)).andReturn("foo");
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(isA(SimpleMessage.class));
    replay(messageStore);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set(errorException.resultCode);
    replay(resultStore);

    ErrorExceptionHandler handler = new ErrorExceptionHandler(resultStore, configuration, messageStore, messageProvider);
    handler.handle(errorException);

    verify(messageProvider, messageStore, resultStore);
  }

  @Test
  public void errorExceptionWithDefaultResultCode() {
    ErrorException errorException = new MockErrorException();
    MVCConfiguration configuration = new MockConfiguration();

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    expect(messageProvider.getMessage("[" + errorException.getClass().getSimpleName() + "]", errorException.args)).andReturn("foo");
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(isA(SimpleMessage.class));
    replay(messageStore);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set(configuration.exceptionResultCode());
    replay(resultStore);

    ErrorExceptionHandler handler = new ErrorExceptionHandler(resultStore, configuration, messageStore, messageProvider);
    handler.handle(errorException);

    verify(messageProvider, messageStore, resultStore);
  }

  @Test
  public void errorExceptionWithNoMessageLookup() {
    ErrorException errorException = new ErrorException("error", false);
    MVCConfiguration configuration = new MockConfiguration();

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set("error");
    replay(resultStore);

    ErrorExceptionHandler handler = new ErrorExceptionHandler(resultStore, configuration, messageStore, messageProvider);
    handler.handle(errorException);

    verify(messageProvider, messageStore, resultStore);
  }

  @Test
  public void validationExceptionWithoutMessage() {
    ValidationException e = new ValidationException();
    MVCConfiguration configuration = new MockConfiguration();

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    messageProvider.getMessage("[" + e.getClass().getSimpleName() + "]", e.args);
    expectLastCall().andThrow(new MissingMessageException());
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set(e.resultCode);
    replay(resultStore);

    ErrorExceptionHandler handler = new ErrorExceptionHandler(resultStore, configuration, messageStore, messageProvider);
    handler.handle(e);

    verify(messageProvider, messageStore, resultStore);
  }

  /**
   * Mock error exception with no custom result code
   */
  public static class MockErrorException extends ErrorException {
    public MockErrorException() {
      super();
    }
  }

  /**
   * Mock error exception with custom result code
   */
  public static class MockErrorExceptionWithCode extends ErrorException {
    public static final String resultCode = "result.code";

    public MockErrorExceptionWithCode() {
      super(resultCode);
    }
  }
}
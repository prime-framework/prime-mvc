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
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.config.AbstractMVCConfiguration;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;
import org.primeframework.mvc.validation.ValidationException;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * @author James Humphrey
 */
@Test
public class DefaultExceptionHandlerTest {

  @Test
  public void errorExceptionWithDefaultResultCode() {
    ErrorException errorException = new MockErrorException();
    MVCConfiguration configuration = new AbstractMVCConfiguration() {
      @Override
      public int templateCheckSeconds() {
        return 0;
      }

      @Override
      public int l10nReloadSeconds() {
        return 0;
      }

      @Override
      public boolean allowUnknownParameters() {
        return false;
      }
    };

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    expect(messageProvider.getMessage(errorException.getClass().getSimpleName(), errorException.args)).andReturn("foo");
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(isA(SimpleMessage.class));
    replay(messageStore);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set(configuration.exceptionResultCode());
    replay(resultStore);

    DefaultExceptionHandler handler = new DefaultExceptionHandler(resultStore, configuration, messageStore, messageProvider);
    handler.handle(errorException);

    verify(messageProvider, messageStore, resultStore);
  }

  @Test
  public void errorExceptionWithCustomResultCode() {
    ErrorException errorException = new MockErrorExceptionWithCode();
    MVCConfiguration configuration = new AbstractMVCConfiguration() {
      @Override
      public int templateCheckSeconds() {
        return 0;
      }

      @Override
      public int l10nReloadSeconds() {
        return 0;
      }

      @Override
      public boolean allowUnknownParameters() {
        return false;
      }
    };

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    expect(messageProvider.getMessage(errorException.getClass().getSimpleName(), errorException.args)).andReturn("foo");
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    messageStore.add(isA(SimpleMessage.class));
    replay(messageStore);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set(errorException.resultCode);
    replay(resultStore);

    DefaultExceptionHandler handler = new DefaultExceptionHandler(resultStore, configuration, messageStore, messageProvider);
    handler.handle(errorException);

    verify(messageProvider, messageStore, resultStore);
  }

  @Test
  public void validationExceptionWithoutMessage() {
    ValidationException e = new ValidationException();
    MVCConfiguration configuration = new AbstractMVCConfiguration() {
      @Override
      public int templateCheckSeconds() {
        return 0;
      }

      @Override
      public int l10nReloadSeconds() {
        return 0;
      }

      @Override
      public boolean allowUnknownParameters() {
        return false;
      }
    };

    MessageProvider messageProvider = createStrictMock(MessageProvider.class);
    messageProvider.getMessage(e.getClass().getSimpleName(), e.args);
    expectLastCall().andThrow(new MissingMessageException());
    replay(messageProvider);

    MessageStore messageStore = createStrictMock(MessageStore.class);
    replay(messageStore);

    ResultStore resultStore = createStrictMock(ResultStore.class);
    resultStore.set(e.resultCode);
    replay(resultStore);

    DefaultExceptionHandler handler = new DefaultExceptionHandler(resultStore, configuration, messageStore, messageProvider);
    handler.handle(e);

    verify(messageProvider, messageStore, resultStore);
  }

  @Test
  public void runtimeException() {
    DefaultExceptionHandler handler = new DefaultExceptionHandler(null, null, null, null);

    try {
      handler.handle(new RuntimeException());
      fail("RuntimeException should have rethrown in the handle method");
    } catch (RuntimeException e) {
      // no-op, test successful
    }
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
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
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.message.l10n.MissingMessageException;

import com.google.inject.Inject;

/**
 * Handles {@link ErrorException} when thrown.  The handle method is called by the {@link ExceptionHandler}
 * implementation which, by default, is the {@link DefaultExceptionHandler}.
 * <p/>
 * This handler is just one of many different mechanisms by which you can handle exceptions in Prime.  You can configure
 * additional handlers by using Guice's multi-binding mechanism as follows:
 *
 * @author James Humphrey
 */
public class ErrorExceptionHandler implements TypedExceptionHandler<ErrorException> {

  private final ResultStore resultStore;
  private final MVCConfiguration configuration;
  private final MessageStore messageStore;
  private final MessageProvider messageProvider;

  @Inject
  public ErrorExceptionHandler(ResultStore resultStore, MVCConfiguration configuration, MessageStore messageStore, MessageProvider messageProvider) {
    this.resultStore = resultStore;
    this.configuration = configuration;
    this.messageStore = messageStore;
    this.messageProvider = messageProvider;
  }

  @Override
  public void handle(ErrorException exception) {

    // set the result code.  if null, grab from mvc configuration
    String code = exception.resultCode != null ? exception.resultCode : configuration.exceptionResultCode();
    resultStore.set(code);

    // get the message from the message provider.  key = name of the class
    try {
      String messageCode = "[" + exception.getClass().getSimpleName() + "]";
      String message = messageProvider.getMessage(messageCode, exception.args);
      messageStore.add(new SimpleMessage(MessageType.ERROR, messageCode, message));
    } catch (MissingMessageException mme) {
      // Ignore because there isn't a message
    }
  }
}

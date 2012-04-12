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
 * @author James Humphrey
 */
public class DefaultExceptionHandler implements ExceptionHandler {
  private final ResultStore resultStore;
  private final MVCConfiguration configuration;
  private final MessageStore messageStore;
  private final MessageProvider messageProvider;

  @Inject
  public DefaultExceptionHandler(ResultStore resultStore, MVCConfiguration configuration, MessageStore messageStore,
                                 MessageProvider messageProvider) {
    this.resultStore = resultStore;
    this.configuration = configuration;
    this.messageStore = messageStore;
    this.messageProvider = messageProvider;
  }

  @Override
  public void handle(RuntimeException e) {
    if (e instanceof ErrorException) {
      ErrorException errorException = (ErrorException) e;

      // set the result code.  if null, grab from mvc configuration
      String code = errorException.resultCode != null ? errorException.resultCode : configuration.exceptionResultCode();
      resultStore.set(code);

      // get the message from the message provider.  key = name of the class
      try {
        String message = messageProvider.getMessage(e.getClass().getSimpleName(), errorException.args);
        messageStore.add(new SimpleMessage(MessageType.ERROR, message));
      } catch (MissingMessageException mme) {
        // Ignore because there isn't a message
      }
    } else {
      // anything other than error messages get re-thrown
      throw e;
    }
  }
}

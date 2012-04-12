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
import org.primeframework.mvc.validation.ValidationException;
import org.primeframework.mvc.validation.jsr303.ValidationProcessor;

import com.google.inject.Inject;

/**
 * @author James Humphrey
 */
public class DefaultExceptionHandler implements ExceptionHandler {

  private final ResultStore resultStore;
  private final MVCConfiguration configuration;
  private final MessageStore messageStore;
  private final MessageProvider messageProvider;
  private final ValidationProcessor validationProcessor;

  @Inject
  public DefaultExceptionHandler(ResultStore resultStore, MVCConfiguration configuration,
                                 MessageStore messageStore, MessageProvider messageProvider, ValidationProcessor validationProcessor) {
    this.resultStore = resultStore;
    this.configuration = configuration;
    this.messageStore = messageStore;
    this.messageProvider = messageProvider;
    this.validationProcessor = validationProcessor;
  }

  @Override
  public void handle(RuntimeException e) {
    if (e instanceof ErrorException) {
      ErrorException errorException = (ErrorException) e;

      // set the result code.  if null, grab from mvc configuration
      String code = errorException.resultCode != null ? errorException.resultCode : configuration.exceptionResultCode();
      resultStore.set(code);


      if (errorException instanceof ValidationException) {
        ValidationException validationException = (ValidationException)errorException;
        validationProcessor.handle(validationException.violations);
      } else {
        // get the message from the message provider.  key = name of the class
        String message = messageProvider.getMessage(e.getClass().getSimpleName(), errorException.args);

        // add the message to the message store
        messageStore.add(new SimpleMessage(MessageType.ERROR, message));
      }
    } else {
      // anything other than error messages get re-thrown
      throw e;
    }
  }
}

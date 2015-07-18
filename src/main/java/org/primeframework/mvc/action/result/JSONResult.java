/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.JacksonActionConfiguration;
import org.primeframework.mvc.message.*;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * This result writes out Java objects to JSON using Jackson. The content type is set to 'application/json'.
 *
 * @author Brian Pontarelli
 */
public class JSONResult extends AbstractResult<JSON> {
  private final ActionInvocationStore actionInvocationStore;

  private final MessageStore messageStore;

  private final ObjectMapper objectMapper;

  private final HttpServletResponse response;

  @Inject
  public JSONResult(ExpressionEvaluator expressionEvaluator, ActionInvocationStore actionInvocationStore, MessageStore messageStore,
                    ObjectMapper objectMapper, HttpServletResponse response) {
    super(expressionEvaluator);
    this.messageStore = messageStore;
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
    this.objectMapper = objectMapper;
  }

  public void execute(JSON json) throws IOException, ServletException {
    ActionInvocation current = actionInvocationStore.getCurrent();
    Object action = current.action;
    if (action == null) {
      throw new PrimeException("There is no action class and somehow we got into the JSONResult code. Bad mojo!");
    }

    ActionConfiguration configuration = current.configuration;
    if (configuration == null) {
      throw new PrimeException("The action [" + action.getClass() + "] has no configuration. This should be impossible!");
    }

    // If there are error messages, put them in a well known container and render that instead of looking for the
    // @JSONResponse annotation
    Object jacksonObject;
    List<Message> messages = messageStore.get(MessageScope.REQUEST);
    if (messages.size() > 0) {
      jacksonObject = convertErrors(messages);
    } else {
      JacksonActionConfiguration jacksonActionConfiguration = (JacksonActionConfiguration) configuration.additionalConfiguration.get(JacksonActionConfiguration.class);
      if (jacksonActionConfiguration == null || jacksonActionConfiguration.responseMember == null) {
        throw new PrimeException("The action [" + action.getClass() + "] is missing a field annotated with @JSONResponse. This is used to figure out what to send back in the response.");
      }

      jacksonObject = expressionEvaluator.getValue(jacksonActionConfiguration.responseMember, action);
      if (jacksonObject == null) {
        throw new PrimeException("The @JSONResponse field [" + jacksonActionConfiguration.responseMember + "] in the action [" + action.getClass() + "] is null. It cannot be null!");
      }
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    objectMapper.writeValue(baos, jacksonObject);

    byte[] result = baos.toByteArray();
    response.setStatus(json.status());
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.setContentLength(result.length);

    if (!isHeadRequest(current)) {
      ServletOutputStream outputStream = response.getOutputStream();
      outputStream.write(result);
      outputStream.flush();
    }
  }

  private ErrorMessages convertErrors(List<Message> messages) {
    ErrorMessages errorMessages = new ErrorMessages();
    for (Message message : messages) {
      if (message instanceof FieldMessage) {
        FieldMessage fieldMessage = (FieldMessage) message;
        errorMessages.addFieldError(fieldMessage.getField(), fieldMessage.getCode(), fieldMessage.toString());
      } else {
        errorMessages.generalErrors.add(new ErrorMessage(message.getCode(), message.toString()));
      }
    }

    return errorMessages;
  }

}

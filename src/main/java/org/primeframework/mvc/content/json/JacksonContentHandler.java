/*
 * Copyright (c) 2013-2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content.json;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.content.ContentHandler;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.RequestMember;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.servlet.HTTPMethod;
import org.primeframework.mvc.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the Jackson JSON processor to marshall JSON into Java objects and set them into the action.
 *
 * @author Brian Pontarelli
 */
public class JacksonContentHandler implements ContentHandler {
  private static final Logger logger = LoggerFactory.getLogger(JacksonContentHandler.class);

  private final ExpressionEvaluator expressionEvaluator;

  private final MessageProvider messageProvider;

  private final MessageStore messageStore;

  private final ObjectMapper objectMapper;

  private final HttpServletRequest request;

  private final ActionInvocationStore store;

  @Inject
  public JacksonContentHandler(HttpServletRequest request, ActionInvocationStore store, ObjectMapper objectMapper,
                               ExpressionEvaluator expressionEvaluator, MessageProvider messageProvider,
                               MessageStore messageStore) {
    this.request = request;
    this.store = store;
    this.objectMapper = objectMapper;
    this.expressionEvaluator = expressionEvaluator;
    this.messageProvider = messageProvider;
    this.messageStore = messageStore;
  }

  @Override
  public void cleanup() {
    // No-Op
  }

  @Override
  public void handle() throws IOException {
    ActionInvocation actionInvocation = store.getCurrent();
    Object action = actionInvocation.action;
    if (action == null) {
      return;
    }

    ActionConfiguration config = actionInvocation.configuration;
    if (!config.additionalConfiguration.containsKey(JacksonActionConfiguration.class)) {
      return;
    }

    int contentLength = request.getContentLength();
    if (contentLength == 0) {
      return;
    }

    // Process JSON and set into the object
    JacksonActionConfiguration jacksonConfiguration = (JacksonActionConfiguration) config.additionalConfiguration.get(JacksonActionConfiguration.class);
    if (!jacksonConfiguration.requestMembers.isEmpty()) {
      HTTPMethod httpMethod = actionInvocation.method.httpMethod;
      RequestMember requestMember = jacksonConfiguration.requestMembers.get(httpMethod);

      try {
        // Retrieve the current value from the action so we can see if it is non-null
        Object currentValue = expressionEvaluator.getValue(requestMember.name, action);
        ObjectReader reader;
        if (currentValue != null) {
          reader = objectMapper.readerForUpdating(currentValue);
        } else {
          reader = objectMapper.readerFor(requestMember.type);
        }

        Object jsonObject;
        if (logger.isDebugEnabled()) {
          final String req = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
          logger.debug("Request: (" + request.getMethod() + " " + request.getRequestURI() + ") " + req);
          jsonObject = reader.readValue(req);
        } else {
          jsonObject = reader.readValue(request.getInputStream());
        }

        // Set the value into the action if the currentValue from the action was null
        if (currentValue == null) {
          expressionEvaluator.setValue(requestMember.name, action, jsonObject);
        }
      } catch (InvalidFormatException e) {
        logger.debug("Error parsing JSON request", e);
        addFieldError(e);
        throw new ValidationException(e);
      } catch (UnrecognizedPropertyException e) {
        logger.debug("Error parsing JSON request", e);
        String field = buildField(e);
        messageStore.add(new SimpleMessage(MessageType.ERROR, "[invalidJSON]", messageProvider.getMessage("[invalidJSON]", field, "Unrecognized property", e.getMessage())));
        throw new ValidationException(e);
      } catch (JsonMappingException e) {
        logger.debug("Error parsing JSON request", e);

        if (!(e.getCause() instanceof JsonParseException)) {
          addFieldError(e);
        } else {
          messageStore.add(new SimpleMessage(MessageType.ERROR, "[invalidJSON]", messageProvider.getMessage("[invalidJSON]", "unknown", "Unexpected mapping exception", e.getMessage())));
        }
        throw new ValidationException(e);
      } catch (JsonProcessingException e) {
        logger.debug("Error parsing JSON request", e);
        messageStore.add(new SimpleMessage(MessageType.ERROR, "[invalidJSON]", messageProvider.getMessage("[invalidJSON]", "unknown", "Unexpected processing exception", e.getMessage())));
        throw new ValidationException(e);
      }
    }
  }

  /**
   * Adds a field error using the information stored in the JsonMappingException.
   *
   * @param e The exception.
   */
  private void addFieldError(JsonMappingException e) {
    // Build the path so we can make the error
    String field = buildField(e);
    String code = "[invalidJSON]";

    messageStore.add(new SimpleFieldMessage(MessageType.ERROR, field, code, messageProvider.getMessage(code, field, "Possible conversion error", e.getMessage())));
  }

  private String buildField(JsonMappingException e) {
    StringBuilder fieldBuilder = new StringBuilder();
    List<JsonMappingException.Reference> references = e.getPath();
    for (JsonMappingException.Reference reference : references) {
      String fieldName = reference.getFieldName();
      if (fieldName == null) {
        continue;
      }

      if (fieldBuilder.length() > 0) {
        fieldBuilder.append(".");
      }
      fieldBuilder.append(fieldName);
    }

    return fieldBuilder.toString();
  }
}

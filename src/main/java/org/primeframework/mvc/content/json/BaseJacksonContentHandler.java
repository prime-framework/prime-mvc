/*
 * Copyright (c) 2013-2022, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.github.fge.jsonpatch.JsonPatchException;
import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.content.ContentHandler;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.RequestMember;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the Jackson JSON processor to marshall JSON into Java objects and set them into the action.
 *
 * @author Brian Pontarelli
 */
public abstract class BaseJacksonContentHandler implements ContentHandler {
  private static final Logger logger = LoggerFactory.getLogger(BaseJacksonContentHandler.class);

  protected final ExpressionEvaluator expressionEvaluator;

  protected final MessageProvider messageProvider;

  protected final MessageStore messageStore;

  protected final ObjectMapper objectMapper;

  protected final HTTPRequest request;

  protected final ActionInvocationStore store;

  @Inject
  public BaseJacksonContentHandler(HTTPRequest request, ActionInvocationStore store, ObjectMapper objectMapper,
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

    String contentType = request.getContentType();
    Long contentLength = request.getContentLength();
    if (contentLength == null || contentLength == 0) {
      return;
    }

    // Process JSON and set into the object
    JacksonActionConfiguration jacksonConfiguration = (JacksonActionConfiguration) config.additionalConfiguration.get(JacksonActionConfiguration.class);
    if (!jacksonConfiguration.requestMembers.isEmpty()) {
      HTTPMethod httpMethod = actionInvocation.method.httpMethod;
      RequestMember requestMember = jacksonConfiguration.requestMembers.get(httpMethod);

      try {
        if (logger.isDebugEnabled()) {
          String body = new String(request.getBody().array(), 0, contentLength.intValue());
          logger.debug("Request: ({} {}) {}", request.getMethod(), request.getPath(), body);
        }

        // Retrieve the current value from the action, so we can see if it is non-null
        Object currentValue = expressionEvaluator.getValue(requestMember.name, action);
        handle(action, currentValue, contentLength, contentType, requestMember);
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
      } catch (JsonPatchException e) {
        // This isn't ideal, but they do not have any hierarchy in their exception structure and no cause.
        // - We have tests for this in various products to ensure if the assumption changes we'll catch it.
        if ("value differs from expectations".equals(e.getMessage())) {
          messageStore.add(new SimpleMessage(MessageType.ERROR, "[JSONPatchTestFailed]", messageProvider.getMessage("[JSONPatchTestFailed]")));
        } else {
          logger.debug("Error parsing JSON Patch request", e);
          messageStore.add(new SimpleMessage(MessageType.ERROR, "[invalidJSON]", messageProvider.getMessage("[invalidJSON]", "unknown", "Unexpected processing exception", e.getMessage())));
        }

        throw new ValidationException(e);
      }
    }
  }

  /**
   * Handle the current value, do the JSON Jackson stuff.
   *
   * @param action        the action from the current action invocation.
   * @param currentValue  the current value found in the action
   * @param contentLength the value of the Content-Length header for this request
   * @param contentType   the value of the Content-Type header for this request
   * @param requestMember the request member for this action that is bound to the @JSONRequest.
   * @throws IOException        when an IOException occurs. Duh.
   * @throws JsonPatchException when a JsonPatchException occurs. Duh.
   */
  protected abstract void handle(Object action, Object currentValue, Long contentLength, String contentType,
                                 RequestMember requestMember) throws IOException, JsonPatchException;

  /**
   * Adds a field error using the information stored in the JsonMappingException.
   *
   * @param e The exception.
   */
  private void addFieldError(JsonMappingException e) {
    // Build the path so we can make the error
    String field = buildField(e);
    String code = "[invalidJSON]";

    // If we cannot find the field, make this a general error.
    if (field.equals("")) {
      messageStore.add(new SimpleMessage(MessageType.ERROR, code, messageProvider.getMessage(code, "unknown", "Possible conversion error", e.getMessage())));
    } else {
      messageStore.add(new SimpleFieldMessage(MessageType.ERROR, field, code, messageProvider.getMessage(code, field, "Possible conversion error", e.getMessage())));
    }
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

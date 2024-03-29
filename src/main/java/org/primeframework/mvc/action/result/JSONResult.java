/*
 * Copyright (c) 2001-2022, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.PrimeException;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.JacksonActionConfiguration;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.JSONPropertyFilterConfig;
import org.primeframework.mvc.message.ErrorMessage;
import org.primeframework.mvc.message.ErrorMessages;
import org.primeframework.mvc.message.FieldMessage;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.scope.MessageScope;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.util.ReflectionUtils;

/**
 * This result writes out Java objects to JSON using Jackson. The content type is set to 'application/json'.
 *
 * @author Brian Pontarelli
 */
public class JSONResult extends AbstractResult<JSON> {
  private final ActionInvocationStore actionInvocationStore;

  private final MessageStore messageStore;

  private final ObjectMapper objectMapper;

  private final HTTPResponse response;

  @Inject
  public JSONResult(ExpressionEvaluator expressionEvaluator, ActionInvocationStore actionInvocationStore,
                    MessageStore messageStore,
                    ObjectMapper objectMapper, HTTPResponse response) {
    super(expressionEvaluator);
    this.messageStore = messageStore;
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
    this.objectMapper = objectMapper;
  }

  public boolean execute(JSON json) throws IOException {
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    Object action = actionInvocation.action;
    if (action == null) {
      throw new PrimeException("There is no action class and somehow we got into the JSONResult code. Bad mojo!");
    }

    ActionConfiguration configuration = actionInvocation.configuration;
    if (configuration == null) {
      throw new PrimeException("The action [" + action.getClass() + "] has no configuration. This should be impossible!");
    }

    Object jacksonObject;
    boolean prettyPrint = false;
    Class<?> serializationView = void.class;
    JSONPropertyFilterConfig jsonPropertyFilterConfig = null;
    List<Message> errorMessages = messageStore.get(MessageScope.REQUEST).stream()
                                              .filter(m -> m.getType() == MessageType.ERROR)
                                              .collect(Collectors.toList());

    // If there are ERROR messages, put them in a well known container and render that instead of looking for the @JSONResponse annotation
    if (errorMessages.size() > 0) {
      jacksonObject = convertErrors(errorMessages);
    } else {
      JacksonActionConfiguration jacksonActionConfiguration = (JacksonActionConfiguration) configuration.additionalConfiguration.get(JacksonActionConfiguration.class);
      if (jacksonActionConfiguration == null || jacksonActionConfiguration.responseMember == null) {
        throw new PrimeException("The action [" + action.getClass() + "] is missing a field annotated with @JSONResponse. This is used to figure out what to send back in the response.");
      }
      serializationView = jacksonActionConfiguration.getSerializationView();
      jacksonObject = expressionEvaluator.getValue(jacksonActionConfiguration.responseMember.name, action);
      if (jacksonObject == null) {
        throw new PrimeException("The @JSONResponse field [" + jacksonActionConfiguration.responseMember.name + "] in the action [" + action.getClass() + "] is null. It cannot be null!");
      }

      // Allow the JSONResponse annotation to override the contentType.
      prettyPrint = jacksonActionConfiguration.responseMember.annotation.prettyPrint();

      // Capture a jsonFilterConfig method if defined.
      jsonPropertyFilterConfig = jacksonActionConfiguration.jsonPropertyFilterConfig;
    }

    response.setStatus(json.status());
    response.setContentType(json.contentType());

    // Handle setting cache controls
    addCacheControlHeader(json, response);

    if (isHeadRequest(actionInvocation)) {
      return true;
    }

    FilterProvider filterProvider = null;
    ObjectMapper mapper = objectMapper;
    if (jsonPropertyFilterConfig != null) {
      PropertyFilter propertyFilter = ReflectionUtils.invoke(jsonPropertyFilterConfig.method, action);
      filterProvider = new SimpleFilterProvider().addFilter(jsonPropertyFilterConfig.name, propertyFilter);

      // When the mixin is specified on the annotation do not mutate the global objectMapper
      if (jsonPropertyFilterConfig.mixinSource != null && jsonPropertyFilterConfig.mixinTarget != null) {
        mapper = mapper.copy().addMixIn(jsonPropertyFilterConfig.mixinTarget, jsonPropertyFilterConfig.mixinSource);
      }
    }

    writeValue(mapper, response.getOutputStream(), jacksonObject, serializationView, prettyPrint, filterProvider);
    return true;
  }

  @Override
  protected String getCacheControl(JSON result) {
    return result.cacheControl();
  }

  @Override
  protected boolean getDisableCacheControl(JSON result) {
    return result.disableCacheControl();
  }

  private ErrorMessages convertErrors(List<Message> messages) {
    ErrorMessages errorMessages = new ErrorMessages();
    for (Message message : messages) {
      if (message instanceof FieldMessage fieldMessage) {
        errorMessages.addFieldError(fieldMessage.getField(), fieldMessage.getCode(), fieldMessage.toString(), fieldMessage.getData());
      } else {
        errorMessages.generalErrors.add(new ErrorMessage(message.getCode(), message.toString(), message.getData()));
      }
    }

    return errorMessages;
  }

  private ObjectWriter getPrettyWriter(ObjectMapper objectMapper, FilterProvider filterProvider) {
    DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
    prettyPrinter.indentArraysWith(new DefaultIndenter());

    return objectMapper.writerWithDefaultPrettyPrinter()
                       .withFeatures(SerializationFeature.INDENT_OUTPUT)
                       .with(filterProvider)
                       .with(prettyPrinter);
  }

  private void writeValue(ObjectMapper mapper, OutputStream os, Object jacksonObject, Class<?> serializationView,
                          boolean prettyPrint, FilterProvider filterProvider) throws IOException {

    // Most common path
    if (!prettyPrint && serializationView == void.class) {
      if (filterProvider != null) {
        mapper.writer(filterProvider).writeValue(os, jacksonObject);
      } else {
        mapper.writeValue(os, jacksonObject);
      }

      return;
    }

    if (prettyPrint && serializationView != void.class) {
      getPrettyWriter(mapper, filterProvider).with(filterProvider).withView(serializationView).writeValue(os, jacksonObject);
      return;
    }

    if (prettyPrint) {
      getPrettyWriter(mapper, filterProvider).writeValue(os, jacksonObject);
      return;
    }

    // serializationView is always non-null here
    if (filterProvider != null) {
      mapper.writerWithView(serializationView).with(filterProvider).writeValue(os, jacksonObject);
    } else {
      mapper.writerWithView(serializationView).writeValue(os, jacksonObject);
    }
  }
}

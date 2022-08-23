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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.content.json.JacksonActionConfiguration.RequestMember;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.message.MessageStore;
import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * Uses the Jackson JSON processor to marshall JSON into Java objects and set them into the action.
 *
 * @author Brian Pontarelli
 */
public class JacksonContentHandler extends BaseJacksonContentHandler {
  @Inject
  public JacksonContentHandler(HTTPRequest request, ActionInvocationStore store, ObjectMapper objectMapper,
                               ExpressionEvaluator expressionEvaluator, MessageProvider messageProvider,
                               MessageStore messageStore) {
    super(request, store, objectMapper, expressionEvaluator, messageProvider, messageStore);
  }

  @Override
  protected void handle(Object action, Object currentValue, Long contentLength, String contentType,
                        RequestMember requestMember)
      throws IOException {

    ObjectReader reader;
    if (currentValue != null) {
      reader = objectMapper.readerForUpdating(currentValue);
    } else {
      reader = objectMapper.readerFor(requestMember.type);
    }

    Object jsonObject = reader.readValue(request.getBody().array(), 0, contentLength.intValue());

    // Set the value into the action if the currentValue from the action was null
    if (currentValue == null) {
      expressionEvaluator.setValue(requestMember.name, action, jsonObject);
    }
  }
}

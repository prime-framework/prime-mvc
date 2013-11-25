/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.content.ContentHandler;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Uses the Jackson JSON processor to marshall JSON into Java objects and set them into the action.
 *
 * @author Brian Pontarelli
 */
public class JacksonContentHandler implements ContentHandler {
  private final ExpressionEvaluator expressionEvaluator;

  private final ObjectMapper objectMapper;

  private final HttpServletRequest request;

  private final ActionInvocationStore store;

  @Inject
  public JacksonContentHandler(HttpServletRequest request, ActionInvocationStore store, ObjectMapper objectMapper,
                               ExpressionEvaluator expressionEvaluator) {
    this.request = request;
    this.store = store;
    this.objectMapper = objectMapper;
    this.expressionEvaluator = expressionEvaluator;
  }

  @Override
  public void handle() throws IOException {
    ActionInvocation current = store.getCurrent();
    Object action = current.action;
    if (action == null) {
      return;
    }

    ActionConfiguration config = current.configuration;
    if (!config.additionalConfiguration.containsKey(JacksonActionConfiguration.class)) {
      return;
    }

    // Process JSON and set into the object
    JacksonActionConfiguration jacksonConfiguration = (JacksonActionConfiguration) config.additionalConfiguration.get(JacksonActionConfiguration.class);
    if (jacksonConfiguration.requestMember != null) {
      Object jsonObject = objectMapper.reader(jacksonConfiguration.requestMemberType).readValue(request.getInputStream());
      expressionEvaluator.setValue(jacksonConfiguration.requestMember, action, jsonObject);
    }
  }
}
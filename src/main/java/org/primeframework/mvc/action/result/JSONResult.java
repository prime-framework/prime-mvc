/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.JacksonActionConfiguration;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This result writes out Java objects to JSON using Jackson. The content type is set to 'application/json'.
 *
 * @author Brian Pontarelli
 */
public class JSONResult extends AbstractResult<JSON> {
  private final ActionInvocationStore actionInvocationStore;

  private final ObjectMapper objectMapper;

  private final HttpServletResponse response;

  @Inject
  public JSONResult(ExpressionEvaluator expressionEvaluator, HttpServletResponse response,
                    ActionInvocationStore actionInvocationStore, ObjectMapper objectMapper) {
    super(expressionEvaluator);
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
    this.objectMapper = objectMapper;
  }

  public void execute(JSON json) throws IOException, ServletException {
    ActionInvocation current = actionInvocationStore.getCurrent();
    Object action = current.action;
    if (action == null) {
      return;
    }

    ActionConfiguration configuration = current.configuration;
    if (configuration == null) {
      return;
    }

    JacksonActionConfiguration jacksonActionConfiguration = (JacksonActionConfiguration) configuration.additionalConfiguration.get(JacksonActionConfiguration.class);
    if (jacksonActionConfiguration == null || jacksonActionConfiguration.responseMember == null) {
      return;
    }

    Object jacksonObject = expressionEvaluator.getValue(jacksonActionConfiguration.responseMember, action);
    if (jacksonObject == null) {
      return;
    }


    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    objectMapper.writerWithType(jacksonActionConfiguration.responseMemberType).writeValue(baos, jacksonObject);

    byte[] result = baos.toByteArray();
    response.setStatus(json.status());
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.setContentLength(result.length);

    ServletOutputStream outputStream = response.getOutputStream();
    outputStream.write(result);
    outputStream.flush();
  }
}
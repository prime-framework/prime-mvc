/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.security;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.action.result.SavedRequestTools;
import org.primeframework.mvc.action.result.SavedRequestTools.SaveHttpRequestResult;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.security.saved.SavedHttpRequest;
import org.primeframework.mvc.workflow.WorkflowChain;

/**
 * Default saved request workflow that uses the {@link SavedHttpRequest} from the session to mock out the request.
 *
 * @author Brian Pontarelli
 */
public class DefaultSavedRequestWorkflow implements SavedRequestWorkflow {
  private final MVCConfiguration configuration;

  private final Encryptor encryptor;

  private final ObjectMapper objectMapper;

  private final HTTPRequest request;

  private final HTTPResponse response;

  @Inject
  public DefaultSavedRequestWorkflow(MVCConfiguration configuration, Encryptor encryptor, ObjectMapper objectMapper,
                                     HTTPRequest request, HTTPResponse response) {
    this.objectMapper = objectMapper;
    this.request = request;
    this.encryptor = encryptor;
    this.configuration = configuration;
    this.response = response;
  }

  @Override
  public void perform(WorkflowChain workflowChain) throws IOException {
    SaveHttpRequestResult result = SavedRequestTools.getSaveRequestForWorkflow(configuration, encryptor, objectMapper, request, response);
    if (result != null) {
      request.setPath(result.savedHttpRequest.uri);
      request.setMethod(result.savedHttpRequest.method);
      request.getParameters().putAll(result.savedHttpRequest.parameters);
    }

    workflowChain.continueWorkflow();
  }
}

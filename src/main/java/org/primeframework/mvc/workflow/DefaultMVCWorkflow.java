/*
` * Copyright (c) 2001-2025, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.ActionInvocationWorkflow;
import org.primeframework.mvc.action.ActionMappingWorkflow;
import org.primeframework.mvc.action.result.ResultInvocationWorkflow;
import org.primeframework.mvc.content.ContentWorkflow;
import org.primeframework.mvc.cors.CORSRequestWorkflow;
import org.primeframework.mvc.message.MessageWorkflow;
import org.primeframework.mvc.parameter.ParameterWorkflow;
import org.primeframework.mvc.parameter.PostParameterWorkflow;
import org.primeframework.mvc.parameter.URIParameterWorkflow;
import org.primeframework.mvc.scope.ScopeRetrievalWorkflow;
import org.primeframework.mvc.scope.ScopeStorageWorkflow;
import org.primeframework.mvc.security.SavedRequestWorkflow;
import org.primeframework.mvc.security.SecurityWorkflow;
import org.primeframework.mvc.validation.ValidationWorkflow;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * This class is the main entry point for the Prime MVC. It uses the workflows passed into the constructor in the order
 * they are passed in. It also catches {@link ErrorException} and then processes errors using an error workflow set. The
 * error set consists of the {@link ScopeStorageWorkflow} followed by the {@link ResultInvocationWorkflow}.
 *
 * @author Brian Pontarelli
 */
public class DefaultMVCWorkflow implements MVCWorkflow {
  private final ErrorWorkflow errorWorkflow;

  private final ExceptionHandler exceptionHandler;

  private final HTTPResponse response;

  private final List<Workflow> workflows;

  @Inject
  public DefaultMVCWorkflow(CORSRequestWorkflow corsRequestWorkflow,
                            SavedRequestWorkflow savedRequestWorkflow,
                            ActionMappingWorkflow actionMappingWorkflow,
                            ScopeRetrievalWorkflow scopeRetrievalWorkflow,
                            URIParameterWorkflow uriParameterWorkflow,
                            ParameterWorkflow parameterWorkflow,
                            ContentWorkflow contentWorkflow,
                            PostParameterWorkflow postParameterWorkflow,
                            SecurityWorkflow securityWorkflow,
                            ValidationWorkflow validationWorkflow,
                            MessageWorkflow messageWorkflow,
                            ActionInvocationWorkflow actionInvocationWorkflow,
                            ScopeStorageWorkflow scopeStorageWorkflow,
                            ResultInvocationWorkflow resultInvocationWorkflow,
                            StaticResourceWorkflow staticResourceWorkflow,
                            MissingWorkflow missingWorkflow,
                            ErrorWorkflow errorWorkflow,
                            ExceptionHandler exceptionHandler,
                            HTTPResponse response) {
    this.exceptionHandler = exceptionHandler;
    this.errorWorkflow = errorWorkflow;
    this.response = response;
    this.workflows = asList(
        corsRequestWorkflow,
        savedRequestWorkflow,
        actionMappingWorkflow,
        scopeRetrievalWorkflow,
        uriParameterWorkflow,
        parameterWorkflow,
        contentWorkflow,
        postParameterWorkflow,
        securityWorkflow,
        validationWorkflow,
        messageWorkflow,
        actionInvocationWorkflow,
        scopeStorageWorkflow,
        resultInvocationWorkflow,
        staticResourceWorkflow,
        missingWorkflow);
  }

  /**
   * Creates a sub-chain of the MVC workflows and invokes it.
   *
   * @param workflowChain Not used because there is no outer workflow outside the MVC.
   * @throws IOException If the sub-chain throws an IOException
   */
  public void perform(WorkflowChain workflowChain) throws IOException {
    try {
      WorkflowChain chain = new SubWorkflowChain(workflows, workflowChain);
      chain.continueWorkflow();
    } catch (RuntimeException | Error e) {
      // If any bytes were written, we are screwed and can't do anything here. Re-throw
      if (response.isCommitted()) {
        throw e;
      }

      // Reset the response, but preserve cookies and headers.
      var cookies = List.copyOf(response.getCookies());
      var headers = Map.copyOf(response.getHeadersMap());
      response.reset();
      headers.keySet().forEach(k -> headers.get(k).forEach(v -> response.addHeader(k, v)));
      cookies.forEach(response::addCookie);

      // Call the exception handler
      exceptionHandler.handle(e);

      // Continue the error workflow
      WorkflowChain errorChain = new SubWorkflowChain(singletonList(errorWorkflow), workflowChain);
      errorChain.continueWorkflow();
    }
  }
}

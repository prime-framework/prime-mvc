/*
 * Copyright (c) 2013-2016, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.primeframework.mvc.content.guice.ContentHandlerFactory;
import org.primeframework.mvc.workflow.WorkflowChain;

import com.google.inject.Inject;

/**
 * Default content workflow. This uses the Content-Type header and {@link ContentHandler} implementations to handle the
 * content types.
 *
 * @author Brian Pontarelli
 */
public class DefaultContentWorkflow implements ContentWorkflow {
  private final ContentHandlerFactory factory;

  private final HttpServletRequest request;

  @Inject
  public DefaultContentWorkflow(HttpServletRequest request, ContentHandlerFactory factory) {
    this.request = request;
    this.factory = factory;
  }

  @Override
  public void perform(WorkflowChain workflowChain) throws IOException, ServletException {
    String contentType = request.getContentType();
    ContentHandler handler = factory.build(contentType);
    if (handler != null) {
      handler.handle();
    }

    workflowChain.continueWorkflow();

    if (handler != null) {
      handler.cleanup();
    }
  }
}

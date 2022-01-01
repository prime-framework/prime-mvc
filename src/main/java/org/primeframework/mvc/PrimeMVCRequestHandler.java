/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import com.google.inject.Injector;
import org.primeframework.mvc.guice.GuiceBootstrap;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.http.MutableHTTPRequest;
import org.primeframework.mvc.workflow.MVCWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brian Pontarelli
 */
public class PrimeMVCRequestHandler {
  private static final Logger logger = LoggerFactory.getLogger(PrimeMVCRequestHandler.class);

  protected volatile Injector injector;

  public PrimeMVCRequestHandler(Injector injector) {
    logger.info("Initializing Prime");
    this.injector = injector;
  }

  /**
   * Invokes the Workflow chain.
   *
   * @param request  Passed down chain.
   * @param response Passed down chain.
   */
  public void handleRequest(MutableHTTPRequest request, HTTPResponse response) {
    // Support for HTTP Method Override
    String methodOverride = request.getHeader("X-HTTP-Method-Override");
    if (methodOverride == null) {
      methodOverride = request.getHeader("X-Method-Override");
    }

    if (methodOverride != null) {
      request.setMethod(HTTPMethod.of(methodOverride));
    }

    HTTPObjectsHolder.setRequest(request);
    HTTPObjectsHolder.setResponse(response);

    try {
      injector.getInstance(MVCWorkflow.class).perform(null);
    } catch (Throwable t) {
      response.setStatus(500);

      logger.error("Error encountered", t);
    } finally {
      HTTPObjectsHolder.clearRequest();
      HTTPObjectsHolder.clearResponse();
    }
  }

  public void shutdown() {
    logger.info("Shutting down Prime MVC");
    GuiceBootstrap.shutdown(injector);
  }

  public void updateInjector(Injector injector) {
    this.injector = injector;
  }
}
/*
 * Copyright (c) 2021-2023, Inversoft Inc., All Rights Reserved
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

import java.io.Closeable;

import com.google.inject.Injector;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.server.HTTPHandler;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.action.result.MVCWorkflowFinalizer;
import org.primeframework.mvc.guice.GuiceBootstrap;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.workflow.MVCWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brian Pontarelli
 */
public class PrimeMVCRequestHandler implements HTTPHandler, Closeable {
  private static final Logger logger = LoggerFactory.getLogger(PrimeMVCRequestHandler.class);

  protected volatile Injector injector;

  public PrimeMVCRequestHandler(Injector injector) {
    logger.info("Initializing Prime");
    this.injector = injector;
  }

  public void close() {
    logger.info("Shutting down Prime MVC");
    GuiceBootstrap.shutdown(injector);
  }

  /**
   * Invokes the Workflow chain.
   *
   * @param request  Passed down chain.
   * @param response Passed down chain.
   */
  @Override
  public void handle(HTTPRequest request, HTTPResponse response) {
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
      // Do not error log an InterruptedException
      if (t.getCause() != null && t.getCause() instanceof InterruptedException) {
        // Making the assumption that the java-http server threw this Exception because the connection was interrupted
        // because the client stopped responding, or was reading or writing too slow.
        // - Not using a 500 because that implies a server side failure, and the convention is that a stack trace will
        //   be found in the logs for each 500 status code.
        // - It would be great if java-http would include some meta-data in the Exception that we could log. java-http
        //   will produce logs with DEBUG enabled to indicate why the connection is closed. I don't know if this is possible yet.
        // - 408 seems the most appropriate status code here. https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/408
        response.setStatus(408);
        logger.debug("Request interrupted.", t);
      } else {
        response.setStatus(500);
        logger.error("Error encountered", t);
      }
    } finally {
      HTTPObjectsHolder.clearRequest();
      HTTPObjectsHolder.clearResponse();

      // Execute the finalizer
      injector.getInstance(MVCWorkflowFinalizer.class).run();
    }
  }

  public void updateInjector(Injector injector) {
    this.injector = injector;
  }
}

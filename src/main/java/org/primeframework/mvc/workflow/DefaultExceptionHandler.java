/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.config.MVCConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author James Humphrey
 */
public class DefaultExceptionHandler implements ExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

  private final MVCConfiguration configuration;

  private final TypedExceptionHandlerFactory factory;

  private final HTTPResponse response;

  private final ResultStore resultStore;

  @Inject
  public DefaultExceptionHandler(MVCConfiguration configuration, TypedExceptionHandlerFactory factory,
                                 HTTPResponse response, ResultStore resultStore) {
    this.configuration = configuration;
    this.factory = factory;
    this.response = response;
    this.resultStore = resultStore;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void handle(Throwable exception) {
    // Add the exception to the response so that handles and templates can get it
    response.setException(exception);

    // Find a handler
    Class<? extends Throwable> klass = exception.getClass();
    while (klass != Throwable.class) {
      TypedExceptionHandler handler = factory.build(klass);
      if (handler == null) {
        klass = (Class<? extends Throwable>) klass.getSuperclass();
        continue;
      }

      // Handle and exit method
      handler.handle(exception);
      return;
    }

    // Set the result code to the default so that the ErrorWorkflow can handle it
    logger.error("Unhandled exception occurred", exception);
    resultStore.set(configuration.exceptionResultCode());
    response.setStatus(500);
  }
}

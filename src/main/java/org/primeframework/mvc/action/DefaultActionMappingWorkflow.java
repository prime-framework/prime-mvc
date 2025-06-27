/*
 * Copyright (c) 2001-2025, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action;

import java.io.IOException;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import io.fusionauth.http.HTTPMethod;
import io.fusionauth.http.io.MultipartConfiguration;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.NotAllowedException;
import org.primeframework.mvc.http.HTTPTools;
import org.primeframework.mvc.http.Status;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the default implementation of the ActionMappingWorkflow. During the perform method, this class determines the action that will be
 * invoked based on the URI and put it into the ActionInvocationStore.
 *
 * @author Brian Pontarelli
 */
public class DefaultActionMappingWorkflow implements ActionMappingWorkflow {
  private final static Logger logger = LoggerFactory.getLogger(DefaultActionMappingWorkflow.class);

  private final ActionInvocationStore actionInvocationStore;

  private final ActionMapper actionMapper;

  private final HTTPRequest request;

  private final HTTPResponse response;

  @Inject(optional = true) private MetricRegistry metricRegistry;

  @Inject
  public DefaultActionMappingWorkflow(HTTPRequest request, HTTPResponse response, ActionInvocationStore actionInvocationStore,
                                      ActionMapper actionMapper) {
    this.request = request;
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
    this.actionMapper = actionMapper;
  }

  /**
   * Processes the request URI, loads the action configuration, creates the action and stores the invocation in the request.
   *
   * @param chain The workflow chain.
   * @throws IOException If the chain throws an exception.
   */
  public void perform(WorkflowChain chain) throws IOException {
    // First, see if they hit a different button
    String uri = determineURI();
    if (logger.isDebugEnabled()) {
      logger.debug("METHOD: [{}]; URI: [{}]" + uri, request.getMethod(), uri);
    }

    HTTPMethod method = request.getMethod();
    ActionInvocation actionInvocation = actionMapper.map(method, uri);

    // This case is a redirect because the URI maps to something new and there isn't an action associated with it. For
    // example, this is how the index handling works.
    if (actionInvocation.redirect) {
      response.sendRedirect(actionInvocation.uri());
      response.setStatus(Status.MOVED_PERMANENTLY);
      return;
    }

    // Keep the current action in the store if an exception is thrown so that it can be used from the error workflow
    actionInvocationStore.setCurrent(actionInvocation);

    // Anyone downstream should understand it is possible for the method to be null in the ActionInvocation
    if (actionInvocation.action != null && actionInvocation.method == null) {
      Class<?> actionClass = actionInvocation.configuration.actionClass;
      logger.debug("The action class [{}] does not have a valid execute method for the HTTP method [{}]", actionClass.getCanonicalName(), method);
      throw new NotAllowedException();
    }

    // Handle multipart file configuration
    handleMultiPartConfiguration(actionInvocation);

    // Start the timers and grab some meters for errors
    Timer.Context perPathTimer = null;
    Timer.Context aggregateTimer = null;
    Meter perPathErrorMeter = null;
    Meter aggregateErrorMeter = null;

    try {
      if (metricRegistry != null && actionInvocation.action != null) {
        // Ignore try/resource inspection, we are closing this in the finally block already

        //noinspection resource
        perPathTimer = metricRegistry.timer("prime-mvc.[" + actionInvocation.uri() + "].requests").time();

        //noinspection resource
        aggregateTimer = metricRegistry.timer("prime-mvc.[*].requests").time();
        perPathErrorMeter = metricRegistry.meter("prime-mvc.[" + actionInvocation.uri() + "].errors");
        aggregateErrorMeter = metricRegistry.meter("prime-mvc.[*].errors");
      }

      chain.continueWorkflow();

      // We need to leave the action in the store because it might be used by the Error Workflow
      actionInvocationStore.removeCurrent();
    } catch (IOException | RuntimeException | Error e) {
      if (perPathErrorMeter != null) {
        perPathErrorMeter.mark();
      }

      if (aggregateErrorMeter != null) {
        aggregateErrorMeter.mark();
      }

      throw e;
    } finally {
      if (aggregateTimer != null) {
        aggregateTimer.stop();
      }

      if (perPathTimer != null) {
        perPathTimer.stop();
      }
    }
  }

  private String determineURI() {
    String uri = HTTPTools.getRequestURI(request);
    if (!uri.startsWith("/")) {
      uri = "/" + uri;
    }

    return uri;
  }

  private void handleMultiPartConfiguration(ActionInvocation actionInvocation) {
    if (actionInvocation.configuration == null) {
      return;
    }

    // Note that multipart file handling is disabled by default. Enable it if the action has indicated it is expecting a file upload.
    boolean expectingFileUploads = !actionInvocation.configuration.fileUploadMembers.isEmpty();
    if (!expectingFileUploads) {
      return;
    }

    // Note we could optionally inspect all the FileUpload annotations, and take the MAX of maxSize if set, and set the max file size for this
    // specific request. In most cases - assuming we have just a single FileUpload annotation per action, this would effectively allow java-http
    // to enforce this value.
    MultipartConfiguration multipartConfiguration = request.getMultiPartStreamProcessor().getMultiPartConfiguration();
    multipartConfiguration.withFileUploadEnabled(true);

    // Take the largest configured file size, or if non have specified a max file size, use the configured default.
    long configuredMaxFileSize = multipartConfiguration.getMaxFileSize();
    var fileUploadMembers = actionInvocation.configuration.fileUploadMembers;
    long maxFileSize = fileUploadMembers.values().stream()
                                        .map(FileUpload::maxSize)
                                        .filter(m -> m != -1)
                                        .max(Long::compareTo)
                                        .orElse(configuredMaxFileSize);
    multipartConfiguration.withMaxFileSize(maxFileSize);

    // If each file specifies a max size, then this will be the sum. It may also be 0 if no FileUpload members specified a max size.
    long expectedFileSizes = fileUploadMembers.values().stream()
                                              .map(FileUpload::maxSize)
                                              .filter(m -> m != -1)
                                              .count();
    long calculatedExpectedFileSize = maxFileSize * fileUploadMembers.size();

    // Take the larger of the two and add 1MB
    long adjustedMaxRequestSize = Math.max(expectedFileSizes, calculatedExpectedFileSize) + (1024 * 1024);
    if (Math.max(expectedFileSizes, calculatedExpectedFileSize) > multipartConfiguration.getMaxRequestSize()) {
      multipartConfiguration.withMaxRequestSize(adjustedMaxRequestSize);
    }
  }
}

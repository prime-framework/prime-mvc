/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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
import java.util.Set;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import org.primeframework.mvc.NotAllowedException;
import org.primeframework.mvc.http.HTTPMethod;
import org.primeframework.mvc.http.HTTPRequest;
import org.primeframework.mvc.http.HTTPResponse;
import org.primeframework.mvc.http.HTTPTools;
import org.primeframework.mvc.http.Status;
import org.primeframework.mvc.parameter.DefaultParameterParser;
import org.primeframework.mvc.parameter.InternalParameters;
import org.primeframework.mvc.workflow.WorkflowChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the default implementation of the ActionMappingWorkflow. During the perform method, this class
 * determines the action that will be invoked based on the URI and put it into the ActionInvocationStore.
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
  public DefaultActionMappingWorkflow(HTTPRequest request, HTTPResponse response,
                                      ActionInvocationStore actionInvocationStore, ActionMapper actionMapper) {
    this.request = request;
    this.response = response;
    this.actionInvocationStore = actionInvocationStore;
    this.actionMapper = actionMapper;
  }

  /**
   * Processes the request URI, loads the action configuration, creates the action and stores the invocation in the
   * request.
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
    boolean executeResult = InternalParameters.is(request, InternalParameters.EXECUTE_RESULT);
    ActionInvocation actionInvocation = actionMapper.map(method, uri, executeResult);

    // This case is a redirect because they URI maps to something new and there isn't an action associated with it. For
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
      logger.warn("The action class [{}] does not have a valid execute method for the HTTP method [{}]", actionClass.getCanonicalName(), method);
      throw new NotAllowedException();
    }

    // Start the timer and grab a meter for errors
    Timer.Context timer = null;
    Meter errorMeter = null;
    if (metricRegistry != null && actionInvocation.action != null) {
      timer = metricRegistry.timer("prime-mvc.[" + actionInvocation.uri() + "].requests").time();
      errorMeter = metricRegistry.meter("prime-mvc.[" + actionInvocation.uri() + "].errors");
    }

    try {
      chain.continueWorkflow();

      // We need to leave the action in the store because it might be used by the Error Workflow
      actionInvocationStore.removeCurrent();
    } catch (IOException | RuntimeException | Error e) {
      if (errorMeter != null) {
        errorMeter.mark();
      }

      throw e;
    } finally {
      if (timer != null) {
        timer.stop();
      }
    }
  }

  private String determineURI() {
    String uri = null;
    Set<String> keys = request.getParameters().keySet();
    for (String key : keys) {
      if (key.startsWith(DefaultParameterParser.ACTION_PREFIX)) {
        String actionParameterName = key.substring(4);
        String actionParameterValue = request.getParameterValue(key);
        if (request.getParameterValue(actionParameterName) != null && actionParameterValue.trim().length() > 0) {
          uri = actionParameterValue;

          // Handle relative URIs
          if (!uri.startsWith("/")) {
            String requestURI = HTTPTools.getRequestURI(request);
            int index = requestURI.lastIndexOf('/');
            if (index >= 0) {
              uri = requestURI.substring(0, index) + "/" + uri;
            }
          }
        }
      }
    }

    if (uri == null) {
      uri = HTTPTools.getRequestURI(request);
      if (!uri.startsWith("/")) {
        uri = "/" + uri;
      }
    }
    return uri;
  }
}

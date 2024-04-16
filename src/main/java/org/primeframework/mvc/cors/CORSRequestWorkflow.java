/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.cors;

import java.io.IOException;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.workflow.MVCWorkflow;
import org.primeframework.mvc.workflow.WorkflowChain;

/**
 * A {@link MVCWorkflow} for processing CORS requests.
 *
 * @author Trevor Smith
 */
public class CORSRequestWorkflow implements MVCWorkflow {
  private final CORSConfigurationProvider corsConfigurationProvider;

  private final HTTPRequest request;

  private final HTTPResponse response;

  private CORSDebugger debugger;

  @Inject
  public CORSRequestWorkflow(CORSConfigurationProvider corsConfigurationProvider, HTTPRequest request,
                             HTTPResponse response) {
    this.corsConfigurationProvider = corsConfigurationProvider;
    this.request = request;
    this.response = response;
  }

  @Override
  public void perform(WorkflowChain workflowChain) throws IOException {
    CORSConfiguration corsConfiguration = corsConfigurationProvider.get();
    if (corsConfiguration != null) {
      CORSFilter corsFilter = new CORSFilter()
          .withAllowCredentials(corsConfiguration.allowCredentials)
          .withAllowedHTTPHeaders(corsConfiguration.allowedHeaders)
          .withAllowedHTTPMethods(corsConfiguration.allowedMethods)
          .withAllowedOrigins(corsConfiguration.allowedOrigins)
          .withDebugEnabled(corsConfiguration.debug)
          .withDebugger(debugger);
      if (corsConfiguration.excludedPathPattern != null) {
        corsFilter.withExcludedPathPattern(corsConfiguration.excludedPathPattern);
      }
      else if (corsConfiguration.includedPathPattern != null) {
        corsFilter.withIncludedPathPattern(corsConfiguration.includedPathPattern);
      }
      else if (corsConfiguration.includeUriChecker != null) {
        corsFilter.withIncludedUriChecker(corsConfiguration.includeUriChecker);
      }
      else if (corsConfiguration.excludeUriChecker != null) {
        corsFilter.withExcludedUriChecker(corsConfiguration.excludeUriChecker);
      }
      corsFilter.withExposedHeaders(corsConfiguration.exposedHeaders)
                .withPreflightMaxAge(corsConfiguration.preflightMaxAgeInSeconds);

      corsFilter.doFilter(request, response, workflowChain);
    } else {
      workflowChain.continueWorkflow();
    }
  }

  @Inject(optional = true)
  public void setDebugger(CORSDebugger debugger) {
    this.debugger = debugger;
  }
}

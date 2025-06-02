/*
 * Copyright (c) 2025, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.http;

import io.fusionauth.http.io.MultipartProcessorConfiguration;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;

/**
 * Extend the base Multipart configuration to allow individual actions to participate in the configuration during runtime.
 *
 * @author Daniel DeGroff
 */
public class PrimeMultipartConfiguration extends MultipartProcessorConfiguration {
  private final ActionInvocationStore actionInvocationStore;

  public PrimeMultipartConfiguration(ActionInvocationStore actionInvocationStore) {
    this.actionInvocationStore = actionInvocationStore;
  }

  @Override
  public boolean isFileUploadEnabled() {
    // If we have identified an action based upon the current URI, and that action is not expecting any file uploads, then throw out the request.
    ActionInvocation actionInvocation = actionInvocationStore.getCurrent();
    if (actionInvocation != null) {
      return !actionInvocation.configuration.fileUploadMembers.isEmpty();
    }

    // If we don't have an action invocation, defer to the default configuration.
    return super.isFileUploadEnabled();
  }
}

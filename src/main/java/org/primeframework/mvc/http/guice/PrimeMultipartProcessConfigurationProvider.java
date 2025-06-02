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
package org.primeframework.mvc.http.guice;

import javax.inject.Inject;

import com.google.inject.Provider;
import io.fusionauth.http.io.MultipartProcessorConfiguration;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.http.PrimeMultipartConfiguration;

/**
 * @author Daniel DeGroff
 */
public class PrimeMultipartProcessConfigurationProvider implements Provider<MultipartProcessorConfiguration> {
  private final ActionInvocationStore actionInvocationStore;

  @Inject
  public PrimeMultipartProcessConfigurationProvider(ActionInvocationStore actionInvocationStore) {
    this.actionInvocationStore = actionInvocationStore;
  }

  @Override
  public MultipartProcessorConfiguration get() {
    return new PrimeMultipartConfiguration(actionInvocationStore);
  }
}

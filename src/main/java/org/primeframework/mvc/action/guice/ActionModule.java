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
package org.primeframework.mvc.action.guice;

import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.ActionInvocationWorkflow;
import org.primeframework.mvc.action.ActionMapper;
import org.primeframework.mvc.action.ActionMappingWorkflow;
import org.primeframework.mvc.action.DefaultActionInvocationStore;
import org.primeframework.mvc.action.DefaultActionInvocationWorkflow;
import org.primeframework.mvc.action.DefaultActionMapper;
import org.primeframework.mvc.action.DefaultActionMappingWorkflow;
import org.primeframework.mvc.action.annotation.Extension;
import org.primeframework.mvc.action.config.ActionConfigurationBuilder;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.action.config.DefaultActionConfigurationBuilder;
import org.primeframework.mvc.action.config.DefaultActionConfigurationProvider;
import org.primeframework.mvc.action.result.DefaultResourceLocator;
import org.primeframework.mvc.action.result.DefaultResultInvocationWorkflow;
import org.primeframework.mvc.action.result.ResourceLocator;
import org.primeframework.mvc.action.result.ResultInvocationWorkflow;
import org.primeframework.mvc.action.result.ResultStore;
import org.primeframework.mvc.action.result.ThreadLocalResultStore;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Binds injections regarding actions such as extensions.
 *
 * @author Brian Pontarelli
 */
public class ActionModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(String.class).annotatedWith(Extension.class).toProvider(ExtensionProvider.class);

    // Configuration
    bindConfigurationBuilder();
    bindConfigurationProvider();

    // Invocation
    bindStore();
    bindWorkflow();
    bindMapper();
    bindMappingWorkflow();

    // Results
    bindResourceLocator();
    bindResultInvocationWorkflow();
    bindResultStore();
  }

  protected void bindConfigurationBuilder() {
    bind(ActionConfigurationBuilder.class).to(DefaultActionConfigurationBuilder.class);
  }

  protected void bindConfigurationProvider() {
    bind(ActionConfigurationProvider.class).to(DefaultActionConfigurationProvider.class).in(Singleton.class);
  }

  protected void bindStore() {
    bind(ActionInvocationStore.class).to(DefaultActionInvocationStore.class);
  }

  protected void bindWorkflow() {
    bind(ActionInvocationWorkflow.class).to(DefaultActionInvocationWorkflow.class);
  }

  protected void bindMapper() {
    bind(ActionMapper.class).to(DefaultActionMapper.class);
  }

  protected void bindMappingWorkflow() {
    bind(ActionMappingWorkflow.class).to(DefaultActionMappingWorkflow.class);
  }

  protected void bindResourceLocator() {
    bind(ResourceLocator.class).to(DefaultResourceLocator.class);
  }

  protected void bindResultInvocationWorkflow() {
    bind(ResultInvocationWorkflow.class).to(DefaultResultInvocationWorkflow.class);
  }

  protected void bindResultStore() {
    bind(ResultStore.class).to(ThreadLocalResultStore.class);
  }
}

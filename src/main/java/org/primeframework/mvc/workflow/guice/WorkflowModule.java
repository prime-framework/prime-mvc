/*
 * Copyright (c) 2012-2023, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.workflow.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.result.DefaultMVCWorkflowFinalizer;
import org.primeframework.mvc.action.result.MVCWorkflowFinalizer;
import org.primeframework.mvc.workflow.DefaultErrorWorkflow;
import org.primeframework.mvc.workflow.DefaultExceptionHandler;
import org.primeframework.mvc.workflow.DefaultMVCWorkflow;
import org.primeframework.mvc.workflow.DefaultMissingWorkflow;
import org.primeframework.mvc.workflow.ErrorExceptionHandler;
import org.primeframework.mvc.workflow.ErrorWorkflow;
import org.primeframework.mvc.workflow.ExceptionHandler;
import org.primeframework.mvc.workflow.MVCWorkflow;
import org.primeframework.mvc.workflow.MissingWorkflow;
import org.primeframework.mvc.workflow.TypedExceptionHandlerFactory;

/**
 * Guice Module for mapping exception handler to exception types
 *
 * @author James Humphrey
 */
public class WorkflowModule extends AbstractModule {
  protected void bindErrorWorkflow() {
    bind(ErrorWorkflow.class).to(DefaultErrorWorkflow.class);
  }

  protected void bindExceptionHandler() {
    bind(ExceptionHandler.class).to(DefaultExceptionHandler.class);
  }

  protected void bindMVCWorkflow() {
    bind(MVCWorkflow.class).to(DefaultMVCWorkflow.class);
  }

  protected void bindMVCWorkflowFinalizer() {
    bind(MVCWorkflowFinalizer.class).to(DefaultMVCWorkflowFinalizer.class).in(Singleton.class);
  }

  protected void bindMissingWorkflow() {
    bind(MissingWorkflow.class).to(DefaultMissingWorkflow.class);
  }

  @Override
  protected void configure() {
    bind(TypedExceptionHandlerFactory.class);
    TypedExceptionHandlerFactory.addExceptionHandler(binder(), ErrorException.class, ErrorExceptionHandler.class);

    bindErrorWorkflow();
    bindExceptionHandler();
    bindMissingWorkflow();
    bindMVCWorkflow();
    bindMVCWorkflowFinalizer();
  }
}

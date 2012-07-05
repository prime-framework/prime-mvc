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
package org.primeframework.mvc.workflow.guice;

import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.workflow.ErrorExceptionHandler;
import org.primeframework.mvc.workflow.ExceptionHandlerBinder;

import com.google.inject.AbstractModule;

/**
 * Guice Module for mapping exception handler to exception types
 *
 * @author James Humphrey
 */
public class ExceptionHandlerModule extends AbstractModule {

  @Override
  protected void configure() {
    ExceptionHandlerBinder.newBinder(binder()).exception(ErrorException.class).toHandler(ErrorExceptionHandler.class);
  }
}

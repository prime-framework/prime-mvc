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

/**
 * @author James Humphrey
 */
public class DefaultExceptionHandler implements ExceptionHandler {
  private final TypedExceptionHandlerFactory factory;

  @Inject
  public DefaultExceptionHandler(TypedExceptionHandlerFactory factory) {
    this.factory = factory;
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public void handle(Throwable exception) {
    Class<? extends Throwable> klass = exception.getClass();
    boolean handled = false;
    while (klass != Throwable.class) {
      TypedExceptionHandler handler = factory.build(klass);
      if (handler != null) {
        handler.handle(exception);
        handled = true;
        break;
      } else {
        klass = (Class<? extends Throwable>) klass.getSuperclass();
      }
    }

    if (!handled) {
      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else {
        throw (Error) exception;
      }
    }
  }
}

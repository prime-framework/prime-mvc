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

import java.util.Map;

import com.google.inject.Inject;

/**
 * @author James Humphrey
 */
public class DefaultExceptionHandler implements ExceptionHandler {

  private final Map<Class<?>, TypedExceptionHandler<?>> handlers;

  @Inject
  public DefaultExceptionHandler(Map<Class<?>, TypedExceptionHandler<?>> handlers) {
    this.handlers = handlers;
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public <T extends RuntimeException> void handle(T exception) {
    Class<? extends RuntimeException> klass = exception.getClass();
    boolean handled = false;
    while (klass != RuntimeException.class) {
      TypedExceptionHandler handler = handlers.get(klass);
      if (handler != null) {
        handler.handle(exception);
        handled = true;
        break;
      } else {
        klass = (Class<? extends RuntimeException>) klass.getSuperclass();
      }
    }

    if (!handled) {
      throw exception;
    }
  }
}

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

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Builds {@link TypedExceptionHandler} instances on-demand based on the Class of the exception that was thrown.
 *
 * @author Brian Pontarelli
 */
public class TypedExceptionHandlerFactory {
  private static final Map<Class<? extends Throwable>, Class<? extends TypedExceptionHandler<? extends Throwable>>> handlers = new HashMap<Class<? extends Throwable>, Class<? extends TypedExceptionHandler<? extends Throwable>>>();
  private final Injector injector;

  /**
   * Adds an exception handler mapping.
   *
   * @param binder        Used to make a hard binding to the exception handler.
   * @param exceptionType The exception type for the mapping.
   * @param handlerType   The handler type for the mapping.
   * @param <T>           The exception type.
   * @param <U>           The handler type.
   */
  public static <T extends Throwable, U extends TypedExceptionHandler<T>> void addExceptionHandler(Binder binder, Class<T> exceptionType, Class<U> handlerType) {
    binder.bind(handlerType);
    handlers.put(exceptionType, handlerType);
  }

  @Inject
  public TypedExceptionHandlerFactory(Injector injector) {
    this.injector = injector;
  }

  /**
   * Builds an {@link TypedExceptionHandler} for the given type of exception.
   *
   * @param exceptionType The exception type.
   * @param <T>           The exception type.
   * @param <U>           The handler type.
   * @return An instance of the handler or null if that exception type wasn't registered to a {@link
   *         TypedExceptionHandler}
   */
  @SuppressWarnings("unchecked")
  public <T extends Throwable, U extends TypedExceptionHandler<T>> U build(Class<T> exceptionType) {
    Class<U> type = (Class<U>) handlers.get(exceptionType);
    if (type == null) {
      return null;
    }

    return injector.getInstance(type);
  }
}

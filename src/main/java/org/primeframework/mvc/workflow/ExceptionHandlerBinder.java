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

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

/**
 * A binder DSL for adding GlobalConverters to Prime.
 *
 * @author Brian Pontarelli
 */
public class ExceptionHandlerBinder {

  private final Binder binder;

  /**
   * Creates a new GlobalConverterBinder that can be used to register global type converters.
   *
   * @param binder The binder.
   * @return The GlobalConverterBinder.
   */
  public static ExceptionHandlerBinder newBinder(Binder binder) {
    return new ExceptionHandlerBinder(binder);
  }

  public ExceptionHandlerBinder(Binder binder) {
    this.binder = binder;
  }

  public <T extends RuntimeException> ExceptionHandlerTypeBinder<T> exception(Class<T> exceptionClass) {
    return new ExceptionHandlerTypeBinder<T>(binder, exceptionClass);
  }

  public class ExceptionHandlerTypeBinder<T extends RuntimeException> {
    private final Binder binder;
    private final Class<T> exceptionClass;

    private ExceptionHandlerTypeBinder(Binder binder, Class<T> exceptionClass) {
      this.binder = binder;
      this.exceptionClass = exceptionClass;
    }

    public void toHandler(Class<? extends TypedExceptionHandler<T>> handlerClass) {
      MapBinder<Class<?>, TypedExceptionHandler<?>> mapBinder = MapBinder.newMapBinder(binder, new TypeLiteral<Class<?>>(){}, new TypeLiteral<TypedExceptionHandler<?>>(){});
      mapBinder.addBinding(exceptionClass).to(handlerClass);
    }
  }
}

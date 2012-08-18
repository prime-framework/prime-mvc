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
package org.primeframework.mvc.parameter.convert.guice;

import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.GlobalConverter;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

/**
 * A binder DSL for adding GlobalConverters to Prime. Generally speaking, using MultiBindings is horrifically slow and
 * generates a lot of objects. This uses MultiBindings because the {@link ConverterProvider} is a singleton and that is
 * the only place this gets injected. Therefore, this should have very little overhead.
 *
 * @author Brian Pontarelli
 */
public class GlobalConverterBinder {
  /**
   * Creates a new GlobalConverterBinder that can be used to register global type converters.
   *
   * @param binder The binder.
   * @return The GlobalConverterBinder.
   */
  public static GlobalConverterBinder newGlobalConverterBinder(Binder binder) {
    return new GlobalConverterBinder(binder);
  }

  private final Binder binder;

  public GlobalConverterBinder(Binder binder) {
    this.binder = binder;
  }

  public GlobalConverterTypeBinder add(Class<? extends GlobalConverter> converterType) {
    return new GlobalConverterTypeBinder(binder, converterType);
  }

  public class GlobalConverterTypeBinder {
    private final Binder binder;
    private final Class<? extends GlobalConverter> converterType;

    private GlobalConverterTypeBinder(Binder binder, Class<? extends GlobalConverter> converterType) {
      this.binder = binder;
      this.converterType = converterType;
    }

    public void forTypes(Class<?> firstType, Class<?>... additionalTypes) {
      MapBinder<Class<?>, GlobalConverter> mapBinder = MapBinder.newMapBinder(binder, new TypeLiteral<Class<?>>() {}, TypeLiteral.get(GlobalConverter.class));
      mapBinder.addBinding(firstType).to(converterType);
      for (Class<?> type : additionalTypes) {
        mapBinder.addBinding(type).to(converterType);
      }
    }
  }
}

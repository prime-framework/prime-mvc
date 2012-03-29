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
package org.primeframework.mvc.guice;

import org.primeframework.mvc.control.Control;
import org.primeframework.mvc.parameter.convert.GlobalConverter;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import freemarker.template.TemplateModel;

/**
 * A Guice module that helps setup prime framework injections.
 *
 * @author Brian Pontarelli
 */
public abstract class AbstractPrimeModule extends AbstractModule {
  /**
   * Adds a Prime MVC control to the bindings.
   *
   * @param prefix      The prefix that is used to access the control in the FreeMarker templates.
   * @param controlType The control type.
   */
  protected void addControl(String prefix, Class<? extends Control> controlType) {
    MapBinder<String, Control> binder = MapBinder.newMapBinder(binder(), String.class, Control.class);
    binder.permitDuplicates();
    binder.addBinding(prefix).to(controlType);
  }

  /**
   * Adds a global converter to the bindings.
   *
   * @param converterType The class of the global converter.
   * @param firstType The first type that the converter can convert.
   * @param additionalTypes Any additional types the converter can convert.
   */
  protected void addGlobalConverter(Class<? extends GlobalConverter> converterType, Class<?> firstType, Class<?>... additionalTypes) {
    MapBinder<Class<?>, GlobalConverter> binder = MapBinder.newMapBinder(binder(), new TypeLiteral<Class<?>>() {}, TypeLiteral.get(GlobalConverter.class));
    binder.addBinding(firstType).to(converterType).asEagerSingleton();
    for (Class<?> type : additionalTypes) {
      binder.addBinding(type).to(converterType).asEagerSingleton();
    }
  }

  /**
   * Adds a binding to a FreeMarker TemplateModel class.
   *
   * @param name The name the TemplateModel will show up in the FreeMarker templates.
   * @param modelType The class of the TemplateModel.
   */
  protected void addFreemarkerModel(String name, Class<? extends TemplateModel> modelType) {
    MapBinder<String, TemplateModel> binder = MapBinder.newMapBinder(binder(), String.class, TemplateModel.class);
    binder.addBinding(name).to(modelType);
  }
}

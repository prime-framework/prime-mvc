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
package org.primeframework.mvc.freemarker.guice;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import freemarker.template.TemplateModel;

/**
 * Builds TemplateModels on-demand.
 *
 * @author Brian Pontarelli
 */
public class TemplateModelFactory {
  private static final Map<String, Map<String, Class<? extends TemplateModel>>> bindings = new HashMap<String, Map<String, Class<? extends TemplateModel>>>();
  private final Injector injector;

  /**
   * Adds a binding to a TemplateModel so that it can be accessed in FreeMarker like [prefix.name/]
   *
   * @param binder    Used to make a hard binding to the TemplateModel.
   * @param prefix    The prefix of the TemplateModel.
   * @param name      The name of the TemplateModel.
   * @param modelType The TemplateModle type used to build the TemplateModel on demand.
   */
  public static void addModel(Binder binder, String prefix, String name, Class<? extends TemplateModel> modelType) {
    binder.bind(modelType);
    Map<String, Class<? extends TemplateModel>> models = bindings.get(prefix);
    if (models == null) {
      models = new HashMap<String, Class<? extends TemplateModel>>();
      bindings.put(prefix, models);
    }

    models.put(name, modelType);
  }

  /**
   * Adds a singleton binding to a TemplateModel so that it can be accessed in FreeMarker like [prefix.name/]
   *
   * @param binder    Used to make a hard binding to the TemplateModel.
   * @param prefix    The prefix of the TemplateModel.
   * @param name      The name of the TemplateModel.
   * @param modelType The TemplateModle type used to build the TemplateModel on demand.
   */
  public static void addSingletonModel(Binder binder, String prefix, String name, Class<? extends TemplateModel> modelType) {
    binder.bind(modelType).asEagerSingleton();
    Map<String, Class<? extends TemplateModel>> models = bindings.get(prefix);
    if (models == null) {
      models = new HashMap<String, Class<? extends TemplateModel>>();
      bindings.put(prefix, models);
    }

    models.put(name, modelType);
  }

  @Inject
  public TemplateModelFactory(Injector injector) {
    this.injector = injector;
  }

  /**
   * @return The prefixes of the TemplateModels that have been registered.
   */
  public Set<String> prefixes() {
    return bindings.keySet();
  }

  /**
   * @param prefix The prefix of the TemplateModels names to grab.
   * @return The names of the TemplateModels registered under the given prefix.
   */
  public Set<String> controlNames(String prefix) {
    return bindings.get(prefix).keySet();
  }

  /**
   * Builds a TemplateModel.
   *
   * @param prefix The prefix of the TemplateModel to build.
   * @param name   The name of the TemplateModel to build.
   * @return The TemplateModel.
   */
  public TemplateModel build(String prefix, String name) {
    Map<String, Class<? extends TemplateModel>> controls = bindings.get(prefix);
    if (controls == null) {
      throw new IllegalArgumentException("Unbound TemplateModel prefix [" + prefix + "]");
    }

    Class<? extends TemplateModel> modelType = controls.get(name);
    if (modelType == null) {
      throw new IllegalArgumentException("TemplateModel named [" + name + "] is not bound in the prefix [" + prefix + "]");
    }

    return injector.getInstance(modelType);
  }
}

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

import org.primeframework.mvc.freemarker.NamedTemplateModel;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;

/**
 * A binder DSL for adding FreeMarker TemplateModel objects to Prime.
 *
 * @author Brian Pontarelli
 */
public class TemplateModelBinder {
  /**
   * Creates a new TemplateModelBinder that can be used to add arbitrary TemplateModels to the FreeMarker templates.
   *
   * @param binder The binder.
   * @return The TemplateModelBinder.
   */
  public static TemplateModelBinder newTemplateModelBinder(Binder binder) {
    return new TemplateModelBinder(binder);
  }

  private final Binder binder;

  private TemplateModelBinder(Binder binder) {
    this.binder = binder;
  }

  /**
   * Adds a binding for the given TemplateModel.
   *
   * @param modelType The TemplateModel type.
   * @return The prefix binder that is used to specify the prefix the TemplateModel is place under. You must call a method
   *         on the returned obejct otherwise the binding won't be setup.
   */
  public TemplateModelPrefixBinder add(Class<? extends NamedTemplateModel> modelType) {
    return new TemplateModelPrefixBinder(binder, modelType);
  }

  public class TemplateModelPrefixBinder {
    private final Binder binder;
    private final Class<? extends NamedTemplateModel> modelType;

    private TemplateModelPrefixBinder(Binder binder, Class<? extends NamedTemplateModel> modelType) {
      this.binder = binder;
      this.modelType = modelType;
    }

    /**
     * Specifies the prefix for the TemplateModel in the FreeMarker template.
     *
     * @param prefix The prefix.
     */
    public void withPrefix(String prefix) {
      MapBinder<String, NamedTemplateModel> mapBinder = MapBinder.newMapBinder(binder, String.class, NamedTemplateModel.class);
      mapBinder.permitDuplicates();
      mapBinder.addBinding(prefix).to(modelType);
    }
  }
}

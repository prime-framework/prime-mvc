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

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import freemarker.template.TemplateModel;

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
   * @return The name binder that is used to specify the name the TemplateModel is place under. You must call a method
   *         on the returned obejct otherwise the binding won't be setup.
   */
  public TemplateModelNameBinder add(Class<? extends TemplateModel> modelType) {
    return new TemplateModelNameBinder(binder, modelType);
  }

  public class TemplateModelNameBinder {
    private final Binder binder;
    private final Class<? extends TemplateModel> modelType;

    public TemplateModelNameBinder(Binder binder, Class<? extends TemplateModel> modelType) {
      this.binder = binder;
      this.modelType = modelType;
    }

    /**
     * Specifies the name that the TemplateModel is available under in the FreeMarker template.
     *
     * @param name The name.
     */
    public void withName(String name) {
      MapBinder<String, TemplateModel> mapBinder = MapBinder.newMapBinder(binder, String.class, TemplateModel.class);
      mapBinder.addBinding(name).to(modelType);
    }
  }
}

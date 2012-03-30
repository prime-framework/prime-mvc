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
package org.primeframework.mvc.control.guice;

import org.primeframework.mvc.control.Control;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;

/**
 * A binder DSL for adding controls to Prime.
 *
 * @author Brian Pontarelli
 */
public class ControlBinder {
  /**
   * Creates a new ControlBinder that can be used to add new Controls that will be accessible in the FreeMarker templates.
   *
   * @param binder The binder.
   * @return The ControlBinder.
   */
  public static ControlBinder newControlBinder(Binder binder) {
    return new ControlBinder(binder);
  }

  private final Binder binder;

  private ControlBinder(Binder binder) {
    this.binder = binder;
  }

  /**
   * Adds a control type class to the binder.
   *
   * @param controlType The control type.
   * @return The ControlTypeBinder so that you can put the control into a prefix. You must use the returned object or
   *         the binding won't be setup.
   */
  public ControlTypeBinder add(Class<? extends Control> controlType) {
    return new ControlTypeBinder(binder, controlType);
  }

  public class ControlTypeBinder {
    private final Binder binder;
    private final Class<? extends Control> controlType;

    private ControlTypeBinder(Binder binder, Class<? extends Control> controlType) {
      this.binder = binder;
      this.controlType = controlType;
    }

    /**
     * Adds the control binding so that it will show up under the given prefix in the FreeMarker templates.
     *
     * @param prefix The prefix.
     */
    public void withPrefix(String prefix) {
      MapBinder<String, Control> mapBinder = MapBinder.newMapBinder(binder, String.class, Control.class);
      mapBinder.permitDuplicates();
      mapBinder.addBinding(prefix).to(controlType);
    }
  }
}

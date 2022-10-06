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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.primeframework.mvc.control.Control;

/**
 * Builds controls on-demand.
 *
 * @author Brian Pontarelli
 */
public class ControlFactory {
  private static final Map<String, Map<String, Class<? extends Control>>> bindings = new HashMap<>();

  private final Injector injector;

  @Inject
  public ControlFactory(Injector injector) {
    this.injector = injector;
  }

  /**
   * Adds a binding to a control so that it can be accessed in FreeMarker like [prefix.name/]
   *
   * @param binder      The binder to add a hard binding for the control.
   * @param prefix      The prefix of the control.
   * @param name        The name of the control.
   * @param controlType The control type used to build the control on demand.
   */
  public static void addControl(Binder binder, String prefix, String name, Class<? extends Control> controlType) {
    binder.bind(controlType);
    Map<String, Class<? extends Control>> controls = bindings.computeIfAbsent(prefix, k -> new HashMap<>());
    controls.put(name, controlType);
  }

  /**
   * Builds a control.
   *
   * @param prefix The prefix of the control to build.
   * @param name   The name of the control to build.
   * @return The control.
   */
  public Control build(String prefix, String name) {
    Map<String, Class<? extends Control>> controls = bindings.get(prefix);
    if (controls == null) {
      throw new IllegalArgumentException("Unbound control prefix [" + prefix + "]");
    }

    Class<? extends Control> controlType = controls.get(name);
    if (controlType == null) {
      throw new IllegalArgumentException("Control named [" + name + "] is not bound in the prefix [" + prefix + "]");
    }

    return injector.getInstance(controlType);
  }

  /**
   * @param prefix The prefix of the control names to grab.
   * @return The names of the controls registered under the given prefix.
   */
  public Set<String> controlNames(String prefix) {
    return bindings.get(prefix).keySet();
  }

  /**
   * @return The prefixes of the controls that have been registered.
   */
  public Set<String> prefixes() {
    return bindings.keySet();
  }
}

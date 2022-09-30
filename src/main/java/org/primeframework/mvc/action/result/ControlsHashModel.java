/*
 * Copyright (c) 2001-2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.CollectionModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModelException;
import org.primeframework.mvc.control.Control;
import org.primeframework.mvc.control.FreeMarkerControlProxy;
import org.primeframework.mvc.control.guice.ControlFactory;
import org.primeframework.mvc.freemarker.FreeMarkerRenderException;

/**
 * This class is a hash that stores the {@link Control} classes so that they can be used from the FreeMarker templates.
 *
 * @author Brian Pontarelli
 */
public class ControlsHashModel implements TemplateHashModelEx {
  private final ControlFactory controlFactory;

  private final Map<String, FreeMarkerControlProxy> controls = new HashMap<>();

  private final ObjectWrapper objectWrapper;

  private final String prefix;

  public ControlsHashModel(String prefix, ControlFactory controlFactory, ObjectWrapper objectWrapper) {
    this.prefix = prefix;
    this.controlFactory = controlFactory;
    this.objectWrapper = objectWrapper;
  }

  public FreeMarkerControlProxy get(String key) {
    FreeMarkerControlProxy proxy = controls.get(key);
    if (proxy == null) {
      Control control = controlFactory.build(prefix, key);
      if (control == null) {
        throw new FreeMarkerRenderException("Prime control named [" + key + "] doesn't exist. Currently registered controls are " + controls.keySet());
      }

      proxy = new FreeMarkerControlProxy(control, objectWrapper);
      controls.put(key, proxy);
    }

    return proxy;
  }

  public boolean isEmpty() {
    return controlFactory.controlNames(prefix).isEmpty();
  }

  public TemplateCollectionModel keys() throws TemplateModelException {
    return new CollectionModel(controlFactory.controlNames(prefix), (BeansWrapper) objectWrapper);
  }

  public int size() {
    return controlFactory.controlNames(prefix).size();
  }

  public TemplateCollectionModel values() {
    return new CollectionModel(valueCollection(), (BeansWrapper) objectWrapper);
  }

  private Collection<FreeMarkerControlProxy> valueCollection() {
    return controlFactory.controlNames(prefix).stream().map(this::get).collect(Collectors.toList());
  }
}
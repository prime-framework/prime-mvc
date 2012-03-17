/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.control.Control;
import org.primeframework.mvc.control.FreeMarkerControlProxy;
import org.primeframework.mvc.freemarker.FieldSupportBeansWrapper;

import freemarker.ext.beans.CollectionModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * This class is a hash that stores the {@link Control} classes so that they can be used from the FreeMarker templates.
 *
 * @author Brian Pontarelli
 */
public class ControlsHashModel implements TemplateHashModelEx {
  private final Map<String, Control> controls = new HashMap<String, Control>();

  public ControlsHashModel(Set<Control> controls) {
    for (Control control : controls) {
      this.controls.put(control.getName(), control);
    }
  }

  public TemplateCollectionModel keys() throws TemplateModelException {
    return new CollectionModel(controls.keySet(), FieldSupportBeansWrapper.INSTANCE);
  }

  public int size() {
    return controls.size();
  }

  public boolean isEmpty() {
    return controls.isEmpty();
  }

  public TemplateModel get(String key) {
    return new FreeMarkerControlProxy(controls.get(key));
  }

  public TemplateCollectionModel values() {
    return new CollectionModel(valueCollection(), FieldSupportBeansWrapper.INSTANCE);
  }

  private Collection<FreeMarkerControlProxy> valueCollection() {
    List<FreeMarkerControlProxy> all = new ArrayList<FreeMarkerControlProxy>();
    for (Control control : controls.values()) {
      all.add(new FreeMarkerControlProxy(control));
    }
    return all;
  }
}
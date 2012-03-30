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
package org.primeframework.mvc.action.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.freemarker.FieldSupportBeansWrapper;
import org.primeframework.mvc.freemarker.NamedTemplateModel;

import freemarker.ext.beans.CollectionModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * This class is a hash that stores TemplateModels in a namespace for use in the FreeMarker templates.
 *
 * @author Brian Pontarelli
 */
public class ModelsHashModel implements TemplateHashModelEx {
  private final Map<String, NamedTemplateModel> models = new HashMap<String, NamedTemplateModel>();

  public ModelsHashModel(Set<NamedTemplateModel> models) {
    for (NamedTemplateModel model : models) {
      this.models.put(model.getName(), model);
    }
  }

  public TemplateCollectionModel keys() throws TemplateModelException {
    return new CollectionModel(models.keySet(), FieldSupportBeansWrapper.INSTANCE);
  }

  public int size() {
    return models.size();
  }

  public boolean isEmpty() {
    return models.isEmpty();
  }

  public TemplateModel get(String key) {
    return models.get(key);
  }

  public TemplateCollectionModel values() {
    return new CollectionModel(valueCollection(), FieldSupportBeansWrapper.INSTANCE);
  }

  private Collection<NamedTemplateModel> valueCollection() {
    List<NamedTemplateModel> all = new ArrayList<NamedTemplateModel>();
    for (NamedTemplateModel model : models.values()) {
      all.add(model);
    }
    return all;
  }
}
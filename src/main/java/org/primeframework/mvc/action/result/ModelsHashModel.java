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
import java.util.List;

import org.primeframework.mvc.freemarker.FieldSupportBeansWrapper;
import org.primeframework.mvc.freemarker.guice.TemplateModelFactory;

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
  private final String prefix;
  private final TemplateModelFactory factory;

  public ModelsHashModel(String prefix, TemplateModelFactory factory) {
    this.prefix = prefix;
    this.factory = factory;
  }

  public TemplateCollectionModel keys() throws TemplateModelException {
    return new CollectionModel(factory.controlNames(prefix), FieldSupportBeansWrapper.INSTANCE);
  }

  public int size() {
    return factory.controlNames(prefix).size();
  }

  public boolean isEmpty() {
    return factory.controlNames(prefix).isEmpty();
  }

  public TemplateModel get(String key) {
    return factory.build(prefix, key);
  }

  public TemplateCollectionModel values() {
    return new CollectionModel(valueCollection(), FieldSupportBeansWrapper.INSTANCE);
  }

  private Collection<TemplateModel> valueCollection() {
    List<TemplateModel> all = new ArrayList<TemplateModel>();
    for (String name : factory.controlNames(prefix)) {
      all.add(get(name));
    }
    return all;
  }
}
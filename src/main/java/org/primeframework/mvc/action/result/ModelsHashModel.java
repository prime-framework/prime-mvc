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

import java.util.Collection;
import java.util.stream.Collectors;

import org.primeframework.mvc.freemarker.guice.TemplateModelFactory;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.CollectionModel;
import freemarker.template.ObjectWrapper;
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
  private final TemplateModelFactory factory;

  private final ObjectWrapper objectWrapper;

  private final String prefix;

  public ModelsHashModel(String prefix, TemplateModelFactory factory, ObjectWrapper objectWrapper) {
    this.prefix = prefix;
    this.factory = factory;
    this.objectWrapper = objectWrapper;
  }

  public TemplateModel get(String key) {
    return factory.build(prefix, key);
  }

  public boolean isEmpty() {
    return factory.controlNames(prefix).isEmpty();
  }

  public TemplateCollectionModel keys() throws TemplateModelException {
    return new CollectionModel(factory.controlNames(prefix), (BeansWrapper) objectWrapper);
  }

  public int size() {
    return factory.controlNames(prefix).size();
  }

  public TemplateCollectionModel values() {
    return new CollectionModel(valueCollection(), (BeansWrapper) objectWrapper);
  }

  private Collection<TemplateModel> valueCollection() {
    return factory.controlNames(prefix).stream().map(this::get).collect(Collectors.toList());
  }
}
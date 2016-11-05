/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.primeframework.mvc.freemarker.FieldSupportBeansWrapper;

import freemarker.ext.beans.CollectionModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Freemarker Root Map
 *
 * @author Daniel DeGroff
 */
public class TestRootMap implements TemplateHashModelEx {
  private final Map<String, Object> objects = new HashMap<>();

  public TestRootMap(Object... values) {
    if (values.length % 2 != 0) {
      String key = values[values.length - 1].toString();
      throw new IllegalArgumentException("Invalid mapping values. Must have a multiple of 2. Missing value for key [" + key + "]");
    }

    for (int i = 0; i < values.length; i = i + 2) {
      objects.put(values[i].toString(), values[i + 1]);
    }
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    Object value = objects.get(key);
    return FieldSupportBeansWrapper.INSTANCE.wrap(value);
  }

  @Override
  public boolean isEmpty() throws TemplateModelException {
    return objects.isEmpty();
  }

  @Override
  public TemplateCollectionModel keys() throws TemplateModelException {
    Set<String> keys = new HashSet<>(objects.keySet());
    return new CollectionModel(keys, FieldSupportBeansWrapper.INSTANCE);
  }

  @Override
  public int size() throws TemplateModelException {
    return objects.size();
  }

  @Override
  public TemplateCollectionModel values() throws TemplateModelException {
    Collection<Object> values = new ArrayList<>(objects.values());
    return new CollectionModel(values, FieldSupportBeansWrapper.INSTANCE);
  }
}

/*
 * Copyright (c) 2025, Inversoft Inc., All Rights Reserved
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A map implementation that captures which keys in the map have been accessed.
 */
public class DetectionMap extends HashMap<String, Object> {
  public final Set<String> variablesAccessed = new HashSet<>();

  @Override
  public Object get(Object key) {
    variablesAccessed.add((String) key);
    return super.get(key);
  }

  @Override
  public Object getOrDefault(Object key, Object defaultValue) {
    variablesAccessed.add((String) key);
    return super.getOrDefault(key, defaultValue);
  }

  public Set<Object> getUnusedVariables() {
    return keySet().stream()
                   .filter(key -> !variablesAccessed.contains(key))
                   .collect(Collectors.toSet());
  }
}

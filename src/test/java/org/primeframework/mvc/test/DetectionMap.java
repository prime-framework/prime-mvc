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
import java.util.Optional;
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

  /**
   * Gets variables that were put in the map since instantiation, but not accessed
   *
   * @param excludePrefix excludes keys that start with this value
   * @param excludeKeys   exclude these act keys
   * @return unused variables
   */
  public Set<Object> getUnusedVariables(String excludePrefix,
                                        String... excludeKeys) {
    Set<String> excludeKeySet = Set.of(excludeKeys);
    return keySet().stream()
                   .filter(key -> !variablesAccessed.contains(key) && !key.startsWith(excludePrefix) && !excludeKeySet.contains(key))
                   // optional variables do not have to be used in the template
                   .filter(key -> !(super.get(key) instanceof Optional<?>))
                   .collect(Collectors.toSet());
  }

  @Override
  public Object put(String key, Object value) {
    // go ahead and evaluate optional values for FreeMarker
    if (value instanceof Optional<?> opt && opt.isPresent()) {
      value = opt.get();
    }
    return super.put(key, value);
  }
}

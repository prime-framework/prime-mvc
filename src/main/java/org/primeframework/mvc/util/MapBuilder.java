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
package org.primeframework.mvc.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple map builder.
 *
 * @author Brian Pontarelli
 */
public class MapBuilder<T, U> {
  public final Map<T, U> map = new LinkedHashMap<>();

  public MapBuilder() {
  }

  public MapBuilder(T key, U value) {
    map.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public static <T> Map<T, T> asMap(T... values) {
    if (values.length % 2 != 0) {
      throw new IllegalArgumentException("Invalid mapping values. Must have a multiple of 2");
    }

    Map<T, T> map = new HashMap<>();
    for (int i = 0; i < values.length; i = i + 2) {
      map.put(values[i], values[i + 1]);
    }

    return map;
  }

  @SuppressWarnings("unchecked")
  public static <T> Map<T, T> lmap(T... values) {
    LinkedHashMap<T, T> map = new LinkedHashMap<>();
    for (int i = 0; i < values.length; i = i + 2) {
      T key = values[i];
      T value = values[i + 1];
      map.put(key, value);
    }

    return map;
  }

  public static <T, U> MapBuilder<T, U> map(T key, U value) {
    return new MapBuilder<>(key, value);
  }

  public static <T, U> MapBuilder<T, U> map() {
    return new MapBuilder<>();
  }

  public Map<T, U> done() {
    return map;
  }

  public MapBuilder<T, U> put(T key, U value) {
    map.put(key, value);
    return this;
  }
}

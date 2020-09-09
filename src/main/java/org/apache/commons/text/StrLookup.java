/*
 * Copyright (c) 2020, Inversoft Inc., All Rights Reserved
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
package org.apache.commons.text;

import java.util.Map;

public abstract class StrLookup<V> {

  /**
   * Lookup that always returns null.
   */
  private static final StrLookup<String> NONE_LOOKUP;

  /**
   * Lookup that uses System properties.
   */
  private static final StrLookup<String> SYSTEM_PROPERTIES_LOOKUP;

  protected StrLookup() {
    super();
  }

  public static <V> StrLookup<V> mapLookup(Map<String, V> map) {
    return new MapStrLookup<V>(map);
  }

  public static StrLookup<?> noneLookup() {
    return NONE_LOOKUP;
  }

  public static StrLookup<String> systemPropertiesLookup() {
    return SYSTEM_PROPERTIES_LOOKUP;
  }

  public abstract String lookup(String key);

  /**
   * Lookup implementation that uses a Map.
   */
  static class MapStrLookup<V> extends StrLookup<V> {

    /**
     * Map keys are variable names and value.
     */
    private final Map<String, V> map;

    /**
     * Creates a new instance backed by a Map.
     *
     * @param map the map of keys to values, may be null
     */
    MapStrLookup(Map<String, V> map) {
      this.map = map;
    }

    /**
     * Looks up a String key to a String value using the map.
     * <p>
     * If the map is null, then null is returned. The map result object is converted to a string using toString().
     *
     * @param key the key to be looked up, may be null
     * @return the matching value, null if no match
     */
    @Override
    public String lookup(String key) {
      if (map == null) {
        return null;
      }
      Object obj = map.get(key);
      if (obj == null) {
        return null;
      }
      return obj.toString();
    }
  }

  //-----------------------------------------------------------------------

  static {
    NONE_LOOKUP = new MapStrLookup<String>(null);
    StrLookup<String> lookup = null;
    try {
      final Map<?, ?> propMap = System.getProperties();
      @SuppressWarnings("unchecked") // System property keys and values are always Strings
      final Map<String, String> properties = (Map<String, String>) propMap;
      lookup = new MapStrLookup<String>(properties);
    } catch (SecurityException ex) {
      lookup = NONE_LOOKUP;
    }
    SYSTEM_PROPERTIES_LOOKUP = lookup;
  }
}

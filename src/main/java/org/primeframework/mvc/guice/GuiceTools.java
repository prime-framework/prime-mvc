/*
 * Copyright (c) 2001-2010, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.guice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * This class provides helper functions for Guice.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class GuiceTools {
  /**
   * Locates all of the Keys for the given Type.
   *
   * @param injector The injector to locate the Keys inside.
   * @param type     The type.
   * @param <T>      The type.
   * @return The list of keys or an empty list if there aren't any.
   */
  public static <T> List<Key<? extends T>> getKeys(Injector injector, Class<T> type) {
    Map<Key<?>, Binding<?>> bindings = injector.getBindings();
    List<Key<? extends T>> results = new ArrayList<>();
    for (Key<?> key : bindings.keySet()) {
      Type t = key.getTypeLiteral().getType();
      if (t instanceof ParameterizedType) {
        t = ((ParameterizedType) t).getRawType();
      }

      if (t instanceof Class<?> bindingType) {
        if (type.isAssignableFrom(bindingType)) {
          results.add((Key<? extends T>) key);
        }
      }
    }

    return results;
  }

  /**
   * Finds all of the bindings that are assignable to the given type.
   *
   * @param injector The injector.
   * @param type     The type.
   * @param <T>      The type.
   * @return The binding types.
   */
  public static <T> List<Class<? extends T>> getTypes(Injector injector, Class<T> type) {
    List<Key<? extends T>> keys = getKeys(injector, type);
    List<Class<? extends T>> types = new ArrayList<>();
    for (Key<? extends T> key : keys) {
      types.add((Class<? extends T>) key.getTypeLiteral().getType());
    }

    return types;
  }
}

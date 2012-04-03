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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.*;

/**
 * A simple array builder.
 *
 * @author Brian Pontarelli
 */
@SuppressWarnings("unchecked")
public class ArrayBuilder<T> {
  public final List<T> list = new ArrayList<T>();
  public final Class<T> type;

  public static <T> T[] asArray(Class<T> type, T... values) {
    T[] array = (T[]) Array.newInstance(type, values.length);
    return asList(values).toArray(array);
  }

  public ArrayBuilder(Class<T> type, T... values) {
    this.type = type;
    this.list.addAll(asList(values));
  }

  public ArrayBuilder<T> add(T value) {
    list.add(value);
    return this;
  }

  public ArrayBuilder<T> addAll(T... values) {
    list.addAll(asList(values));
    return this;
  }

  public ArrayBuilder<T> addAll(Iterable<T> values) {
    for (T value : values) {
      list.add(value);
    }
    return this;
  }

  public T[] done() {
    T[] array = (T[]) Array.newInstance(type, list.size());
    return list.toArray(array);
  }
}

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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * This class adapts an Iterator to an Enumeration.
 *
 * @author Brian Pontarelli
 */
public class IteratorEnumeration<T> implements Enumeration<T> {
  private final Iterator<T> iterator;

  public IteratorEnumeration(Iterator<T> iterator) {
    this.iterator = iterator;
  }

  @Override
  public boolean hasMoreElements() {
    return iterator.hasNext();
  }

  @Override
  public T nextElement() {
    return iterator.next();
  }
}

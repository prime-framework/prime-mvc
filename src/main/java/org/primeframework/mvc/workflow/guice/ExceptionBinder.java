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
package org.primeframework.mvc.workflow.guice;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;

/**
 * A binder DSL for adding exceptions that can be handled by Prime.
 *
 * @author Brian Pontarelli
 */
public class ExceptionBinder {
  private static final Set<Class<? extends RuntimeException>> set = new HashSet<Class<? extends RuntimeException>>();

  static void init(Binder binder) {
    binder.bind(new TypeLiteral<Set<Class<? extends RuntimeException>>>() {}).toInstance(set);
  }

  /**
   * Adds a handlable exception to the binder.
   *
   * @param type The type.
   */
  public static void add(Class<? extends RuntimeException> type) {
    set.add(type);
  }
}

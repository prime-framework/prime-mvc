/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import java.util.List;

import com.google.inject.ImplementedBy;

/**
 * <p> This interface defines how objects that are required by the MVC are created. </p>
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(GuiceObjectFactory.class)
public interface ObjectFactory {
  /**
   * Creates the class given. If the object cannot be created, a RuntimeException is thrown.
   *
   * @param klass The class.
   * @return The Object and never null.
   */
  <T> T create(Class<T> klass);

  /**
   * Locates all of the Class that implement the given type. This might be via extension, implementation, etc. This will
   * only return the known classes, usually by looking them up via the Guice Bindings. This doesn't perform any
   * classpath parsing.
   *
   * @param type The type to find the classes for.
   * @return A list of types or an empty list.
   */
  <T> List<Class<? extends T>> getAllForType(Class<T> type);
}
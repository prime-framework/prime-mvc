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
package org.primeframework.mvc.action.result;

/**
 * Locates resources based on the current action invocation.
 *
 * @author Brian Pontarelli
 */
public interface ResourceLocator {
  /**
   * Locates a resource in the web application under the given root directory.
   *
   * @param directory The directory.
   * @return The location of the resource or null if the resource doesn't exist.
   */
  String locate(String directory);

  /**
   * Locates an index resource in the web application under the given root directory.
   *
   * @param directory The directory.
   * @return The location of the resource or null if the resource doesn't exist.
   */
  String locateIndex(String directory);
}

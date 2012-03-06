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

import com.google.inject.ImplementedBy;

/**
 * Stores the result code of the current request.
 *
 * @author Brian Pontarelli
 */
@ImplementedBy(ThreadLocalResultStore.class)
public interface ResultStore {
  /**
   * @return The current result code.
   */
  String get();

  /**
   * Sets the result code.
   *
   * @param resultCode The new result code.
   */
  void set(String resultCode);

  /**
   * Clears the result code.
   */
  void clear();
}

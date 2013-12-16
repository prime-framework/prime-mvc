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
package org.primeframework.mvc.validation;

/**
 * Marks an action as handling its own validation.
 *
 * @author Brian Pontarelli
 */
public interface Validatable {
  /**
   * Called by Prime instead of using the default handling and group determination. This allows an action to handle
   * validation however it wants. Validation errors should be put into the {@link org.primeframework.mvc.message.MessageStore}
   * by implementers.
   */
  void validate();
}

/*
 * Copyright (c) 2025, Inversoft Inc., All Rights Reserved
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

import java.lang.annotation.Annotation;

/**
 * An interface to define an action result definition. This can be bound and replace having to add a result annotation to each action.
 *
 * @author Daniel DeGroff
 */
public interface ActionResultDefinition {
  /**
   * @param resultCode the result code to use for the mapping
   * @return the constructed annotation.
   */
  Annotation getAnnotation(String resultCode);

  /**
   * @return the status code for the HTTP response
   */
  int getStatus();
}

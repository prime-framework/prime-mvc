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
package org.primeframework.mvc.workflow;

import com.google.inject.ImplementedBy;

/**
 * Translates exceptions thrown while executing the MVC workflows and the application code to result codes.
 * @author Brian Pontarelli
 */
@ImplementedBy(DefaultExceptionTranslator.class)
public interface ExceptionTranslator {
  /**
   * Translate the given exception to a result code.
   *
   * @param t The throwable.
   * @return The result code and never null.
   */
  String translate(Throwable t);
}

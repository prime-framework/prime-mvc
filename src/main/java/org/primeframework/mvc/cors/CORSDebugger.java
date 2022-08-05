/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.cors;

/**
 * An injectable debugger interface that the {@link CORSFilter} leverages to assist with debugging.
 *
 * @author Brian Pontarelli
 */
public interface CORSDebugger {
  /**
   * Turn off the timestamp for this debugger.
   *
   * @return this.
   */
  CORSDebugger disableTimestamp();

  /**
   * Call this one you are done with the debugger.
   */
  void done();

  /**
   * Log a message allowing for replacement values. Each replacement value will be null safe and a default value will be
   * used in place of a null.
   *
   * @param message the message to log.
   * @param values  the values to use as replacement values in the message.
   * @return this.
   */
  CORSDebugger log(String message, Object... values);
}

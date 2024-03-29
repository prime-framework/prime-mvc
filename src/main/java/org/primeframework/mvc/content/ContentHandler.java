/*
 * Copyright (c) 2013-2016, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content;

import java.io.IOException;

/**
 * Defines the method that Prime uses to handle different content types.
 *
 * @author Brian Pontarelli
 */
public interface ContentHandler {
  /**
   * Handles any cleanup that needs to be done by the content handler. This is called after the action invocation has
   * completed. If any files were created by the content handler this is an opportunity to clean it up.
   */
  void cleanup();

  /**
   * Handles the content (HTTP request body) however is needed.
   *
   * @throws IOException If an IO error occurs.
   */
  void handle() throws IOException;
}

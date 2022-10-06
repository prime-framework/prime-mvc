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
package org.primeframework.mvc.content;

import java.io.IOException;

/**
 * A no-op handler that indicates the HTTP server handled the content type so that Prime doesn't need to.
 *
 * @author Brian Pontarelli
 */
public class ServerHandledContentHandler implements ContentHandler {
  @Override
  public void cleanup() {
  }

  @Override
  public void handle() throws IOException {
  }
}

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
package org.primeframework.mvc.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Stream helper methods.
 *
 * @author Brian Pontarelli
 */
public final class StreamTools {
  /**
   * Fully reads the InputStream as bytes and then converts them to a String in UTF-8 encoding.
   *
   * @param is The InputStream to read fully.
   * @return The String result.
   * @throws IOException If the read fails.
   */
  public static String readFullyAsString(InputStream is) throws IOException {
    if (is == null) {
      return null;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int length;
    byte[] buf = new byte[1024];
    while ((length = is.read(buf)) != -1) {
      baos.write(buf, 0, length);
    }

    return baos.toString(StandardCharsets.UTF_8);
  }
}

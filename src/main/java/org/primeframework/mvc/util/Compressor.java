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
package org.primeframework.mvc.util;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Helper class for compressing and decompressing bytes (like for cookies and such).
 *
 * @author Brian Pontarelli
 */
public final class Compressor {
  public static byte[] compress(byte[] bytes) {
    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, false);
    deflater.setInput(bytes);
    deflater.finish();

    byte[] buf = new byte[1024];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while (!deflater.finished()) {
      int length = deflater.deflate(buf);
      baos.write(buf, 0, length);
    }

    return baos.toByteArray();
  }

  public static byte[] decompress(byte[] bytes) throws DataFormatException {
    Inflater inflater = new Inflater(false);
    inflater.setInput(bytes);

    byte[] buf = new byte[1024];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while (!inflater.finished()) {
      int length = inflater.inflate(buf);
      baos.write(buf, 0, length);
    }

    return baos.toByteArray();
  }
}

/*
 * Copyright (c) 2023, Inversoft Inc., All Rights Reserved
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

import java.util.zip.DataFormatException;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * @author Brent Halsey
 */
public class CompressorTest {
  @Test
  public void emptyInputDecompress() throws DataFormatException {
    byte[] emptyBytes = new byte[0];
    byte[] decompressed = Compressor.decompress(emptyBytes);
    assertEquals(decompressed, emptyBytes);
  }
}
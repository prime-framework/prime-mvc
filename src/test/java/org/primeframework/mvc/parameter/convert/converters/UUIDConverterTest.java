/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter.convert.converters;

import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

/**
 * This tests the locale converter.
 *
 * @author Brian Pontarelli
 */
public class UUIDConverterTest {
  @Test
  public void fromStrings() {
    GlobalConverter converter = new UUIDConverter(new MockConfiguration());
    UUID uuid = (UUID) converter.convertFromStrings(UUID.class, null, "testExpr", (String) null);
    assertNull(uuid);

    uuid = (UUID) converter.convertFromStrings(UUID.class, null, "testExpr", "   ");
    assertNull(uuid);

    UUID expected = UUID.randomUUID();
    uuid = (UUID) converter.convertFromStrings(UUID.class, null, "testExpr", expected.toString());
    assertEquals(uuid, expected);

    uuid = (UUID) converter.convertFromStrings(UUID.class, null, "testExpr", "1");
    assertEquals(uuid, new UUID(0, 1));

    uuid = (UUID) converter.convertFromStrings(UUID.class, null, "testExpr", "4242");
    assertEquals(uuid, new UUID(0, 4242));

    try {
      converter.convertFromStrings(UUID.class, null, "testExpr", "NotAUUID");
      fail("Should throw an exception");
    } catch(ConversionException ce) {
      // Expected and therefore we ignore it
    } catch(Throwable t) {
      fail("Should have thrown a ConversionException");
    }
  }

  @Test
  public void toStrings() {
    GlobalConverter converter = new UUIDConverter(new MockConfiguration());
    String str = converter.convertToString(UUID.class, null, "testExpr", null);
    assertNull(str);

    UUID expected = UUID.randomUUID();
    str = converter.convertToString(UUID.class, null, "testExpr", expected);
    assertEquals(str, expected.toString());

    str = converter.convertToString(UUID.class, null, "testExpr", new UUID(0, 1));
    assertEquals(str, "1");

    str = converter.convertToString(UUID.class, null, "testExpr", new UUID(0, 4242));
    assertEquals(str, "4242");
  }
}

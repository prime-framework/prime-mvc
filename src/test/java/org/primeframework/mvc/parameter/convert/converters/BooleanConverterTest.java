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

import org.apache.commons.lang3.ArrayUtils;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * This tests the boolean converter.
 *
 * @author Brian Pontarelli
 */
public class BooleanConverterTest {
  @Test
  public void fromStrings() {
    GlobalConverter converter = new BooleanConverter(new MockConfiguration());
    Boolean b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(b);

    b = (Boolean) converter.convertFromStrings(Boolean.TYPE, null, "testExpr", ArrayUtils.toArray((String) null));
    assertFalse(b);

    b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", ArrayUtils.toArray("true"));
    assertTrue(b);

    b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", ArrayUtils.toArray("yes"));
    assertTrue(b);

    b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", ArrayUtils.toArray("on"));
    assertTrue(b);

    b = (Boolean) converter.convertFromStrings(Boolean.TYPE, null, "testExpr", ArrayUtils.toArray("true"));
    assertTrue(b);

    b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", ArrayUtils.toArray("false"));
    assertFalse(b);

    b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", ArrayUtils.toArray("no"));
    assertFalse(b);

    b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", ArrayUtils.toArray("off"));
    assertFalse(b);

    b = (Boolean) converter.convertFromStrings(Boolean.TYPE, null, "testExpr", ArrayUtils.toArray("false"));
    assertFalse(b);

    b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", ArrayUtils.toArray("   "));
    assertNull(b);

    b = (Boolean) converter.convertFromStrings(Boolean.TYPE, null, "testExpr", ArrayUtils.toArray("   "));
    assertFalse(b);

    Boolean[] ba = (Boolean[]) converter.convertFromStrings(Boolean[].class, null, "testExpr", ArrayUtils.toArray("true", "false"));
    assertTrue(ba[0]);
    assertFalse(ba[1]);

    boolean[] bpa = (boolean[]) converter.convertFromStrings(boolean[].class, null, "testExpr", ArrayUtils.toArray("true", "false"));
    assertTrue(bpa[0]);
    assertFalse(bpa[1]);

    try {
      converter.convertFromStrings(Boolean.class, null, "testExpr", ArrayUtils.toArray("fals3"));
      fail("Should have failed");
    } catch (ConversionException ce) {
      // Expected
    }
  }

  @Test
  public void toStrings() {
    GlobalConverter converter = new BooleanConverter(new MockConfiguration());
    String str = converter.convertToString(Boolean.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(Boolean.class, null, "testExpr", Boolean.TRUE);
    assertEquals(str, "true");

    str = converter.convertToString(Boolean.TYPE, null, "testExpr", Boolean.TRUE);
    assertEquals(str, "true");

    str = converter.convertToString(Boolean.class, null, "testExpr", Boolean.FALSE);
    assertEquals(str, "false");

    str = converter.convertToString(Boolean.TYPE, null, "testExpr", Boolean.FALSE);
    assertEquals(str, "false");
  }
}

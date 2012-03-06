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

import static org.testng.Assert.*;

/**
 * This tests the number converter.
 *
 * @author Brian Pontarelli
 */
public class NumberConverterTest {
  /**
   * Test the conversion from Strings.
   */
  @Test
  public void fromStrings() {
    GlobalConverter converter = new NumberConverter(new MockConfiguration());
    Byte bw = (Byte) converter.convertFromStrings(Byte.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(bw);

    Short sw = (Short) converter.convertFromStrings(Short.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(sw);

    Integer iw = (Integer) converter.convertFromStrings(Integer.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(iw);

    Long lw = (Long) converter.convertFromStrings(Long.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(lw);

    Float fw = (Float) converter.convertFromStrings(Float.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(fw);

    Double dw = (Double) converter.convertFromStrings(Double.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(dw);

    byte b = (Byte) converter.convertFromStrings(Byte.TYPE, null, "testExpr", ArrayUtils.toArray((String) null));
    assertEquals(0, b);

    short s = (Short) converter.convertFromStrings(Short.TYPE, null, "testExpr", ArrayUtils.toArray((String) null));
    assertEquals(0, s);

    int i = (Integer) converter.convertFromStrings(Integer.TYPE, null, "testExpr", ArrayUtils.toArray((String) null));
    assertEquals(0, i);

    long l = (Long) converter.convertFromStrings(Long.TYPE, null, "testExpr", ArrayUtils.toArray((String) null));
    assertEquals(0, l);

    float f = (Float) converter.convertFromStrings(Float.TYPE, null, "testExpr", ArrayUtils.toArray((String) null));
    assertEquals(0, f, 0);

    double d = (Double) converter.convertFromStrings(Double.TYPE, null, "testExpr", ArrayUtils.toArray((String) null));
    assertEquals(0, d, 0);

    bw = (Byte) converter.convertFromStrings(Byte.class, null, "testExpr", ArrayUtils.toArray("1"));
    assertEquals(1, (byte) bw);

    sw = (Short) converter.convertFromStrings(Short.class, null, "testExpr", ArrayUtils.toArray("1"));
    assertEquals(1, (short) sw);

    iw = (Integer) converter.convertFromStrings(Integer.class, null, "testExpr", ArrayUtils.toArray("1"));
    assertEquals(1, (int) iw);

    lw = (Long) converter.convertFromStrings(Long.class, null, "testExpr", ArrayUtils.toArray("1"));
    assertEquals(1l, (long) lw);

    fw = (Float) converter.convertFromStrings(Float.class, null, "testExpr", ArrayUtils.toArray("1"));
    assertEquals(1f, fw, 0);

    dw = (Double) converter.convertFromStrings(Double.class, null, "testExpr", ArrayUtils.toArray("1"));
    assertEquals(1d, dw, 0);

    bw = (Byte) converter.convertFromStrings(Byte.class, null, "testExpr", ArrayUtils.toArray("   "));
    assertNull(bw);

    sw = (Short) converter.convertFromStrings(Short.class, null, "testExpr", ArrayUtils.toArray("   "));
    assertNull(sw);

    iw = (Integer) converter.convertFromStrings(Integer.class, null, "testExpr", ArrayUtils.toArray("   "));
    assertNull(iw);

    lw = (Long) converter.convertFromStrings(Long.class, null, "testExpr", ArrayUtils.toArray("   "));
    assertNull(lw);

    fw = (Float) converter.convertFromStrings(Float.class, null, "testExpr", ArrayUtils.toArray("   "));
    assertNull(fw);

    dw = (Double) converter.convertFromStrings(Double.class, null, "testExpr", ArrayUtils.toArray("   "));
    assertNull(dw);

    b = (Byte) converter.convertFromStrings(Byte.TYPE, null, "testExpr", ArrayUtils.toArray("   "));
    assertEquals(0, b);

    s = (Short) converter.convertFromStrings(Short.TYPE, null, "testExpr", ArrayUtils.toArray("   "));
    assertEquals(0, s);

    i = (Integer) converter.convertFromStrings(Integer.TYPE, null, "testExpr", ArrayUtils.toArray("   "));
    assertEquals(0, i);

    l = (Long) converter.convertFromStrings(Long.TYPE, null, "testExpr", ArrayUtils.toArray("   "));
    assertEquals(0, l);

    f = (Float) converter.convertFromStrings(Float.TYPE, null, "testExpr", ArrayUtils.toArray("   "));
    assertEquals(0, f, 0);

    d = (Double) converter.convertFromStrings(Double.TYPE, null, "testExpr", ArrayUtils.toArray("   "));
    assertEquals(0, d, 0);

    try {
      converter.convertFromStrings(Byte.class, null, "testExpr", ArrayUtils.toArray("bad"));
      fail("Should have failed");
    } catch (ConversionException ce) {
      // Expected
    }

    try {
      converter.convertFromStrings(Short.class, null, "testExpr", ArrayUtils.toArray("bad"));
      fail("Should have failed");
    } catch (ConversionException ce) {
      // Expected
    }

    try {
      converter.convertFromStrings(Integer.class, null, "testExpr", ArrayUtils.toArray("bad"));
      fail("Should have failed");
    } catch (ConversionException ce) {
      // Expected
    }

    try {
      converter.convertFromStrings(Long.class, null, "testExpr", ArrayUtils.toArray("bad"));
      fail("Should have failed");
    } catch (ConversionException ce) {
      // Expected
    }

    try {
      converter.convertFromStrings(Float.class, null, "testExpr", ArrayUtils.toArray("bad"));
      fail("Should have failed");
    } catch (ConversionException ce) {
      // Expected
    }

    try {
      converter.convertFromStrings(Double.class, null, "testExpr", ArrayUtils.toArray("bad"));
      fail("Should have failed");
    } catch (ConversionException ce) {
      // Expected
    }
  }

  /**
   * Test the conversion from Strings.
   */
  @Test
  public void toStrings() {
    GlobalConverter converter = new NumberConverter(new MockConfiguration());
    String str = converter.convertToString(Integer.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(Byte.class, null, "testExpr", (byte) 42);
    assertEquals("42", str);

    str = converter.convertToString(Byte.TYPE, null, "testExpr", (byte) 42);
    assertEquals("42", str);

    str = converter.convertToString(Short.class, null, "testExpr", (short) 42);
    assertEquals("42", str);

    str = converter.convertToString(Short.TYPE, null, "testExpr", (short) 42);
    assertEquals("42", str);

    str = converter.convertToString(Integer.class, null, "testExpr", 42);
    assertEquals("42", str);

    str = converter.convertToString(Integer.class, null, "testExpr", 42);
    assertEquals("42", str);

    str = converter.convertToString(Long.class, null, "testExpr", 42l);
    assertEquals("42", str);

    str = converter.convertToString(Long.TYPE, null, "testExpr", 42l);
    assertEquals("42", str);

    str = converter.convertToString(Float.class, null, "testExpr", 42f);
    assertEquals("42.0", str);

    str = converter.convertToString(Float.TYPE, null, "testExpr", 42f);
    assertEquals("42.0", str);

    str = converter.convertToString(Double.class, null, "testExpr", 42.0);
    assertEquals("42.0", str);

    str = converter.convertToString(Double.TYPE, null, "testExpr", 42.0);
    assertEquals("42.0", str);
  }
}

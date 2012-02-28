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

import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.testng.annotations.Test;

import static net.java.util.CollectionTools.*;
import static org.testng.Assert.*;

/**
 * <p> This tests the enum converter. </p>
 *
 * @author Brian Pontarelli
 */
public class EnumConverterTest {
  /**
   * Test the conversion from Strings.
   */
  @Test
  public void testFromStrings() {
    GlobalConverter converter = new EnumConverter();
    TestEnum e = (TestEnum) converter.convertFromStrings(TestEnum.class, null, "testExpr", array((String) null));
    assertNull(e);

    e = (TestEnum) converter.convertFromStrings(TestEnum.class, null, "testExpr", array("value1"));
    assertSame(e, TestEnum.value1);

    e = (TestEnum) converter.convertFromStrings(TestEnum.class, null, "testExpr", array("value2"));
    assertSame(e, TestEnum.value2);

    try {
      converter.convertFromStrings(TestEnum.class, null, "testExpr", array("value3"));
      fail("Should have thrown conversion exception");
    } catch (ConversionException e1) {
      // Expected
    }

    try {
      converter.convertFromStrings(TestEnum.class, null, "testExpr", array("value1", "value2"));
      fail("Should have failed");
    } catch (UnsupportedOperationException e1) {
      // Expected
    }
  }

  /**
   * Test the conversion from Strings.
   */
  @Test
  public void testToStrings() {
    GlobalConverter converter = new EnumConverter();
    String str = converter.convertToString(TestEnum.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(TestEnum.class, null, "testExpr", TestEnum.value1);
    assertEquals("value1", str);
  }
}

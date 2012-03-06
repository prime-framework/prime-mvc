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
 * This tests the enum converter.
 *
 * @author Brian Pontarelli
 */
public class EnumConverterTest {
  @Test
  public void fromStrings() {
    GlobalConverter converter = new EnumConverter(new MockConfiguration());
    TestEnum e = (TestEnum) converter.convertFromStrings(TestEnum.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(e);

    e = (TestEnum) converter.convertFromStrings(TestEnum.class, null, "testExpr", ArrayUtils.toArray("value1"));
    assertSame(e, TestEnum.value1);

    e = (TestEnum) converter.convertFromStrings(TestEnum.class, null, "testExpr", ArrayUtils.toArray("value2"));
    assertSame(e, TestEnum.value2);

    try {
      converter.convertFromStrings(TestEnum.class, null, "testExpr", ArrayUtils.toArray("value3"));
      fail("Should have thrown conversion exception");
    } catch (ConversionException e1) {
      // Expected
    }

    try {
      converter.convertFromStrings(TestEnum.class, null, "testExpr", ArrayUtils.toArray("value1", "value2"));
      fail("Should have failed");
    } catch (UnsupportedOperationException e1) {
      // Expected
    }
  }

  @Test
  public void toStrings() {
    GlobalConverter converter = new EnumConverter(new MockConfiguration());
    String str = converter.convertToString(TestEnum.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(TestEnum.class, null, "testExpr", TestEnum.value1);
    assertEquals("value1", str);
  }
}

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
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * This tests the String converter.
 *
 * @author Brian Pontarelli
 */
public class StringConverterTest {
  @Test
  public void fromStrings() {
    GlobalConverter converter = new StringConverter(new MockConfiguration());
    String str = (String) converter.convertFromStrings(String.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(str);

    str = (String) converter.convertFromStrings(String.class, null, "testExpr", ArrayUtils.toArray(""));
    assertNull(str);

    str = (String) converter.convertFromStrings(String.class, null, "testExpr", ArrayUtils.toArray("a"));
    assertEquals(str, "a");

    str = (String) converter.convertFromStrings(String.class, null, "testExpr", ArrayUtils.toArray("a", "b"));
    assertEquals(str, "a,b");
  }

  /**
   * Test the conversion from Strings.
   */
  @Test
  public void toStrings() {
    GlobalConverter converter = new StringConverter(new MockConfiguration());
    String str = converter.convertToString(String.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(String.class, null, "testExpr", "foo");
    assertEquals(str, "foo");
  }
}

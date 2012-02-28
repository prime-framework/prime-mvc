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
 * <p> This tests the character converter. </p>
 *
 * @author Brian Pontarelli
 */
public class CharacterConverterTest {
  /**
   * Test the conversion from Strings.
   */
  @Test
  public void testFromStrings() {
    GlobalConverter converter = new CharacterConverter();
    Character cw = (Character) converter.convertFromStrings(Character.class, null, "testExpr", array((String) null));
    assertNull(cw);

    char c = (Character) converter.convertFromStrings(Character.TYPE, null, "testExpr", array((String) null));
    assertEquals('\u0000', c);

    cw = (Character) converter.convertFromStrings(Character.class, null, "testExpr", array("c"));
    assertEquals('c', (char) cw);

    c = (Character) converter.convertFromStrings(Character.TYPE, null, "testExpr", array("c"));
    assertEquals('c', c);

    cw = (Character) converter.convertFromStrings(Character.class, null, "testExpr", array(" "));
    assertNull(cw);

    c = (Character) converter.convertFromStrings(Character.TYPE, null, "testExpr", array(" "));
    assertEquals(0, c);

    Character[] ca = (Character[]) converter.convertFromStrings(Character[].class, null, "testExpr", array("c", "d"));
    assertEquals((Character) 'c', ca[0]);
    assertEquals((Character) 'd', ca[1]);

    char[] cpa = (char[]) converter.convertFromStrings(char[].class, null, "testExpr", array("c", "d"));
    assertEquals('c', cpa[0]);
    assertEquals('d', cpa[1]);

    try {
      converter.convertFromStrings(Character.class, null, "testExpr", array("bad"));
      fail("Should have failed");
    } catch (ConversionException ce) {
      // Expected
    }

    try {
      converter.convertFromStrings(Character.TYPE, null, "testExpr", array("bad"));
      fail("Should have failed");
    } catch (ConversionException ce) {
      // Expected
    }
  }

  /**
   * Test the conversion from Strings.
   */
  @Test
  public void testToStrings() {
    GlobalConverter converter = new BooleanConverter();
    String str = converter.convertToString(Character.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(Character.class, null, "testExpr", 'c');
    assertEquals("c", str);

    str = converter.convertToString(Character.TYPE, null, "testExpr", 'c');
    assertEquals("c", str);
  }
}

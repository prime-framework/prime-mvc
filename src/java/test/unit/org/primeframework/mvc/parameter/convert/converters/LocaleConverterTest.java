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

import java.util.Locale;

import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.testng.annotations.Test;

import static net.java.util.CollectionTools.*;
import static org.testng.Assert.*;

/**
 * <p> This tests the locale converter. </p>
 *
 * @author Brian Pontarelli
 */
public class LocaleConverterTest {
  /**
   * Test the conversion from Strings.
   */
  @Test
  public void testFromStrings() {
    GlobalConverter converter = new LocaleConverter();
    Locale locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", array((String) null));
    assertNull(locale);

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", array("en"));
    assertEquals("en", locale.getLanguage());

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", array("en_US"));
    assertEquals("en", locale.getLanguage());
    assertEquals("US", locale.getCountry());

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", array("en", "US"));
    assertEquals("en", locale.getLanguage());
    assertEquals("US", locale.getCountry());

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", array("en_US_UTF8"));
    assertEquals("en", locale.getLanguage());
    assertEquals("US", locale.getCountry());
    assertEquals("UTF8", locale.getVariant());

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", array("en", "US", "UTF8"));
    assertEquals("en", locale.getLanguage());
    assertEquals("US", locale.getCountry());
    assertEquals("UTF8", locale.getVariant());
  }

  /**
   * Test the conversion from Strings.
   */
  @Test
  public void testToStrings() {
    GlobalConverter converter = new LocaleConverter();
    String str = converter.convertToString(Locale.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(Locale.class, null, "testExpr", Locale.US);
    assertEquals("en_US", str);
  }
}

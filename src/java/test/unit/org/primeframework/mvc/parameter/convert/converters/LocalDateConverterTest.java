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

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.LocalDate;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.util.MapBuilder;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * This tests the local date converter.
 *
 * @author Brian Pontarelli
 */
public class LocalDateConverterTest {
  @Test
  public void fromStrings() {
    GlobalConverter converter = new LocalDateConverter(new MockConfiguration());
    LocalDate value = (LocalDate) converter.convertFromStrings(LocalDate.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(value);

    value = (LocalDate) converter.convertFromStrings(Locale.class, MapBuilder.asMap("dateTimeFormat", "MM-dd-yyyy"), "testExpr", ArrayUtils.toArray("07-08-2008"));
    assertEquals(7, value.getMonthOfYear());
    assertEquals(8, value.getDayOfMonth());
    assertEquals(2008, value.getYear());

    try {
      converter.convertFromStrings(Locale.class, MapBuilder.asMap("dateTimeFormat", "MM-dd-yyyy"), "testExpr", ArrayUtils.toArray("07/08/2008"));
      fail("Should have failed");
    } catch (ConversionException e) {
    }
  }

  @Test
  public void toStrings() {
    GlobalConverter converter = new LocalDateConverter(new MockConfiguration());
    String str = converter.convertToString(LocalDate.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(LocalDate.class, MapBuilder.asMap("dateTimeFormat", "MM-dd-yyyy"), "testExpr", new LocalDate(2008, 7, 8));
    assertEquals("07-08-2008", str);
  }
}

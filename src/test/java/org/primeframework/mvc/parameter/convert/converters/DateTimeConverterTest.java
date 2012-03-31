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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.util.MapBuilder;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * This tests the date time converter.
 *
 * @author Brian Pontarelli
 */
public class DateTimeConverterTest {
  @Test
  public void fromStrings() {
    GlobalConverter converter = new DateTimeConverter(new MockConfiguration());
    DateTime value = (DateTime) converter.convertFromStrings(DateTime.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(value);

    value = (DateTime) converter.convertFromStrings(Locale.class, MapBuilder.asMap("dateTimeFormat", "MM-dd-yyyy"), "testExpr", ArrayUtils.toArray("07-08-2008"));
    assertEquals(value.getMonthOfYear(), 7);
    assertEquals(value.getDayOfMonth(), 8);
    assertEquals(value.getYear(), 2008);


    value = (DateTime) converter.convertFromStrings(Locale.class, MapBuilder.asMap("dateTimeFormat", "MM-dd-yyyy hh:mm:ss aa Z"), "testExpr", ArrayUtils.toArray("07-08-2008 10:13:34 AM -0800"));
    assertEquals(value.getMonthOfYear(), 7);
    assertEquals(value.getDayOfMonth(), 8);
    assertEquals(value.getYear(), 2008);
    assertEquals(value.getHourOfDay(), 10);
    assertEquals(value.getMinuteOfHour(), 13);
    assertEquals(value.getSecondOfMinute(), 34);
    assertEquals(value.getZone(), DateTimeZone.forOffsetHours(-8));

    try {
      converter.convertFromStrings(Locale.class, MapBuilder.asMap("dateTimeFormat", "MM-dd-yyyy"), "testExpr", ArrayUtils.toArray("07/08/2008"));
      fail("Should have failed");
    } catch (ConversionException e) {
    }
  }

  @Test
  public void toStrings() {
    GlobalConverter converter = new DateTimeConverter(new MockConfiguration());
    String str = converter.convertToString(DateTime.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(DateTime.class, MapBuilder.asMap("dateTimeFormat", "MM-dd-yyyy"), "testExpr", new DateTime(2008, 7, 8, 1, 1, 1, 0));
    assertEquals(str, "07-08-2008");
  }
}
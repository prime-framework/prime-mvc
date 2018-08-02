/*
 * Copyright (c) 2001-2018, Inversoft Inc., All Rights Reserved
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

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.primeframework.mvc.MockConfiguration;
import org.primeframework.mvc.TestBuilder;
import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.util.MapBuilder;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * This tests the date time converter.
 *
 * @author Brian Pontarelli
 */
public class ZonedDateTimeConverterTest {
  @Test
  public void fromStrings() {
    GlobalConverter converter = new ZonedDateTimeConverter(new MockConfiguration());
    ZonedDateTime value = (ZonedDateTime) converter.convertFromStrings(ZonedDateTime.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(value);

    value = (ZonedDateTime) converter.convertFromStrings(Locale.class, MapBuilder.asMap("dateTimeFormat", "MM-dd-yyyy hh:mm:ss a Z"), "testExpr", ArrayUtils.toArray("07-08-2008 10:13:34 AM -0800"));
    assertEquals(value.getMonthValue(), 7);
    assertEquals(value.getDayOfMonth(), 8);
    assertEquals(value.getYear(), 2008);
    assertEquals(value.getHour(), 10);
    assertEquals(value.getMinute(), 13);
    assertEquals(value.getSecond(), 34);
    assertEquals(value.getZone(), ZoneOffset.ofHours(-8));

    // Expect conversion error when no time or zone exist
    TestBuilder.expectException(ConversionException.class,
        () -> converter.convertFromStrings(Locale.class, MapBuilder.asMap("dateTimeFormat", "MM-dd-yyyy"), "testExpr", ArrayUtils.toArray("07/08/2008")));

    converter.convertFromStrings(Locale.class, MapBuilder.asMap("dateTimeFormat", "[MM-dd-yyyy hh:mm:ss a Z][MM/dd/yyyy hh;mm;ss a Z]"), "testExpr", ArrayUtils.toArray("07-08-2008 10:13:34 AM -0800"));
    converter.convertFromStrings(Locale.class, MapBuilder.asMap("dateTimeFormat", "[MM-dd-yyyy hh:mm:ss a Z][MM/dd/yyyy hh;mm;ss a Z]"), "testExpr", ArrayUtils.toArray("07/08/2008 10;13;34 AM -0800"));

    // A third format which is not one of the two will fail
    TestBuilder.expectException(ConversionException.class,
        () -> converter.convertFromStrings(Locale.class, MapBuilder.asMap("dateTimeFormat", "[MM-dd-yyyy hh:mm:ss a Z][MM/dd/yyyy hh;mm;ss a Z]"), "testExpr", ArrayUtils.toArray("07_08_2008 10;13;34 AM -0800")));
  }

  @Test
  public void toStrings() {
    GlobalConverter converter = new ZonedDateTimeConverter(new MockConfiguration());
    String str = converter.convertToString(ZonedDateTime.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(ZonedDateTime.class, MapBuilder.asMap("dateTimeFormat", "MM-dd-yyyy"), "testExpr", ZonedDateTime.of(2008, 7, 8, 1, 1, 1, 0, ZoneId.systemDefault()));
    assertEquals(str, "07-08-2008");

    // Multiple formats defined, expect the first one to dictate the string output
    str = converter.convertToString(ZonedDateTime.class, MapBuilder.asMap("dateTimeFormat", "[MM-dd-yyyy][MM/dd/yyyy]"), "testExpr", ZonedDateTime.of(2008, 7, 8, 1, 1, 1, 0, ZoneId.systemDefault()));
    assertEquals(str, "07-08-2008");

    str = converter.convertToString(ZonedDateTime.class, MapBuilder.asMap("dateTimeFormat", "[MM/dd/yyyy][MM-dd-yyyy]"), "testExpr", ZonedDateTime.of(2008, 7, 8, 1, 1, 1, 0, ZoneId.systemDefault()));
    assertEquals(str, "07/08/2008");
  }
}

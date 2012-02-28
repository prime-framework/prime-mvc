/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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

import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.joda.time.DateTime;
import static org.testng.Assert.*;

import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This tests the date time converter.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DateTimeConverterTest {
    /**
     * Test the conversion from Strings.
     */
    @Test
    public void fromStrings() {
        GlobalConverter converter = new DateTimeConverter();
        DateTime value = (DateTime) converter.convertFromStrings(DateTime.class, null, "testExpr", array((String) null));
        assertNull(value);

        value = (DateTime) converter.convertFromStrings(Locale.class, map("dateTimeFormat", "MM-dd-yyyy"), "testExpr", array("07-08-2008"));
        assertEquals(7, value.getMonthOfYear());
        assertEquals(8, value.getDayOfMonth());
        assertEquals(2008, value.getYear());


        value = (DateTime) converter.convertFromStrings(Locale.class, map("dateTimeFormat", "MM-dd-yyyy hh:mm:ss aa Z"), "testExpr", array("07-08-2008 10:13:34 AM -0800"));
        assertEquals(7, value.getMonthOfYear());
        assertEquals(8, value.getDayOfMonth());
        assertEquals(2008, value.getYear());
        assertEquals(10, value.getHourOfDay());
        assertEquals(13, value.getMinuteOfHour());
        assertEquals(34, value.getSecondOfMinute());
        assertEquals(DateTimeZone.forOffsetHours(-8), value.getZone());

        try {
            converter.convertFromStrings(Locale.class, map("dateTimeFormat", "MM-dd-yyyy"), "testExpr", array("07/08/2008"));
            fail("Should have failed");
        } catch (ConversionException e) {
        }
    }

    /**
     * Test the conversion from Strings.
     */
    @Test
    public void toStrings() {
        GlobalConverter converter = new DateTimeConverter();
        String str = converter.convertToString(DateTime.class, null, "testExpr", null);
        assertNull(str);

        str = converter.convertToString(DateTime.class, map("dateTimeFormat", "MM-dd-yyyy"), "testExpr", new DateTime(2008, 7, 8, 1, 1, 1, 0));
        assertEquals("07-08-2008", str);
    }
}

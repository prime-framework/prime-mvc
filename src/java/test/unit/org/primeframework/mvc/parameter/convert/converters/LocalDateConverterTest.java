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

import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.joda.time.LocalDate;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This tests the local date converter.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class LocalDateConverterTest {
    /**
     * Test the conversion from Strings.
     */
    @Test
    public void testFromStrings() {
        GlobalConverter converter = new LocalDateConverter();
        LocalDate value = (LocalDate) converter.convertFromStrings(LocalDate.class, null, "testExpr", array((String) null));
        assertNull(value);

        value = (LocalDate) converter.convertFromStrings(Locale.class, map("dateTimeFormat", "MM-dd-yyyy"), "testExpr", array("07-08-2008"));
        assertEquals(7, value.getMonthOfYear());
        assertEquals(8, value.getDayOfMonth());
        assertEquals(2008, value.getYear());

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
    public void testToStrings() {
        GlobalConverter converter = new LocalDateConverter();
        String str = converter.convertToString(LocalDate.class, null, "testExpr", null);
        assertNull(str);

        str = converter.convertToString(LocalDate.class, map("dateTimeFormat", "MM-dd-yyyy"), "testExpr", new LocalDate(2008, 7, 8));
        assertEquals("07-08-2008", str);
    }
}

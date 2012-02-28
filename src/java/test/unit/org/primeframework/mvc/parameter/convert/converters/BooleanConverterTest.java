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

import org.primeframework.mvc.parameter.convert.ConversionException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This tests the boolean converter.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class BooleanConverterTest {
    /**
     * Test the conversion from Strings.
     */
    @Test
    public void testFromStrings() {
        GlobalConverter converter = new BooleanConverter();
        Boolean b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", array((String) null));
        assertNull(b);

        b = (Boolean) converter.convertFromStrings(Boolean.TYPE, null, "testExpr", array((String) null));
        assertFalse(b);

        b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", array("true"));
        assertTrue(b);

        b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", array("yes"));
        assertTrue(b);

        b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", array("on"));
        assertTrue(b);

        b = (Boolean) converter.convertFromStrings(Boolean.TYPE, null, "testExpr", array("true"));
        assertTrue(b);

        b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", array("false"));
        assertFalse(b);

        b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", array("no"));
        assertFalse(b);

        b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", array("off"));
        assertFalse(b);

        b = (Boolean) converter.convertFromStrings(Boolean.TYPE, null, "testExpr", array("false"));
        assertFalse(b);

        b = (Boolean) converter.convertFromStrings(Boolean.class, null, "testExpr", array("   "));
        assertNull(b);

        b = (Boolean) converter.convertFromStrings(Boolean.TYPE, null, "testExpr", array("   "));
        assertFalse(b);

        Boolean[] ba = (Boolean[]) converter.convertFromStrings(Boolean[].class, null, "testExpr", array("true", "false"));
        assertTrue(ba[0]);
        assertFalse(ba[1]);

        boolean[] bpa = (boolean[]) converter.convertFromStrings(boolean[].class, null, "testExpr", array("true", "false"));
        assertTrue(bpa[0]);
        assertFalse(bpa[1]);

        try {
            converter.convertFromStrings(Boolean.class, null, "testExpr", array("fals3"));
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
        String str = converter.convertToString(Boolean.class, null, "testExpr", null);
        assertNull(str);

        str = converter.convertToString(Boolean.class, null, "testExpr", Boolean.TRUE);
        assertEquals("true", str);

        str = converter.convertToString(Boolean.TYPE, null, "testExpr", Boolean.TRUE);
        assertEquals("true", str);

        str = converter.convertToString(Boolean.class, null, "testExpr", Boolean.FALSE);
        assertEquals("false", str);

        str = converter.convertToString(Boolean.TYPE, null, "testExpr", Boolean.FALSE);
        assertEquals("false", str);
    }
}

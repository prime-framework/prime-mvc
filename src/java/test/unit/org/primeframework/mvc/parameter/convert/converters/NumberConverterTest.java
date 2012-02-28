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
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This tests the number converter.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class NumberConverterTest {
    /**
     * Test the conversion from Strings.
     */
    @Test
    public void testFromStrings() {
        GlobalConverter converter = new NumberConverter();
        Byte bw = (Byte) converter.convertFromStrings(Byte.class, null, "testExpr", array((String) null));
        assertNull(bw);

        Short sw = (Short) converter.convertFromStrings(Short.class, null, "testExpr", array((String) null));
        assertNull(sw);

        Integer iw = (Integer) converter.convertFromStrings(Integer.class, null, "testExpr", array((String) null));
        assertNull(iw);

        Long lw = (Long) converter.convertFromStrings(Long.class, null, "testExpr", array((String) null));
        assertNull(lw);

        Float fw = (Float) converter.convertFromStrings(Float.class, null, "testExpr", array((String) null));
        assertNull(fw);

        Double dw = (Double) converter.convertFromStrings(Double.class, null, "testExpr", array((String) null));
        assertNull(dw);

        byte b = (Byte) converter.convertFromStrings(Byte.TYPE, null, "testExpr", array((String) null));
        assertEquals(0, b);

        short s = (Short) converter.convertFromStrings(Short.TYPE, null, "testExpr", array((String) null));
        assertEquals(0, s);

        int i = (Integer) converter.convertFromStrings(Integer.TYPE, null, "testExpr", array((String) null));
        assertEquals(0, i);

        long l = (Long) converter.convertFromStrings(Long.TYPE, null, "testExpr", array((String) null));
        assertEquals(0, l);

        float f = (Float) converter.convertFromStrings(Float.TYPE, null, "testExpr", array((String) null));
        assertEquals(0, f, 0);

        double d = (Double) converter.convertFromStrings(Double.TYPE, null, "testExpr", array((String) null));
        assertEquals(0, d, 0);

        bw = (Byte) converter.convertFromStrings(Byte.class, null, "testExpr", array("1"));
        assertEquals(1, (byte) bw);

        sw = (Short) converter.convertFromStrings(Short.class, null, "testExpr", array("1"));
        assertEquals(1, (short) sw);

        iw = (Integer) converter.convertFromStrings(Integer.class, null, "testExpr", array("1"));
        assertEquals(1, (int) iw);

        lw = (Long) converter.convertFromStrings(Long.class, null, "testExpr", array("1"));
        assertEquals(1l, (long) lw);

        fw = (Float) converter.convertFromStrings(Float.class, null, "testExpr", array("1"));
        assertEquals(1f, (float) fw, 0);

        dw = (Double) converter.convertFromStrings(Double.class, null, "testExpr", array("1"));
        assertEquals(1d, (double) dw, 0);

        bw = (Byte) converter.convertFromStrings(Byte.class, null, "testExpr", array("   "));
        assertNull(bw);

        sw = (Short) converter.convertFromStrings(Short.class, null, "testExpr", array("   "));
        assertNull(sw);

        iw = (Integer) converter.convertFromStrings(Integer.class, null, "testExpr", array("   "));
        assertNull(iw);

        lw = (Long) converter.convertFromStrings(Long.class, null, "testExpr", array("   "));
        assertNull(lw);

        fw = (Float) converter.convertFromStrings(Float.class, null, "testExpr", array("   "));
        assertNull(fw);

        dw = (Double) converter.convertFromStrings(Double.class, null, "testExpr", array("   "));
        assertNull(dw);

        b = (Byte) converter.convertFromStrings(Byte.TYPE, null, "testExpr", array("   "));
        assertEquals(0, b);

        s = (Short) converter.convertFromStrings(Short.TYPE, null, "testExpr", array("   "));
        assertEquals(0, s);

        i = (Integer) converter.convertFromStrings(Integer.TYPE, null, "testExpr", array("   "));
        assertEquals(0, i);

        l = (Long) converter.convertFromStrings(Long.TYPE, null, "testExpr", array("   "));
        assertEquals(0, l);

        f = (Float) converter.convertFromStrings(Float.TYPE, null, "testExpr", array("   "));
        assertEquals(0, f, 0);

        d = (Double) converter.convertFromStrings(Double.TYPE, null, "testExpr", array("   "));
        assertEquals(0, d, 0);

        try {
            converter.convertFromStrings(Byte.class, null, "testExpr", array("bad"));
            fail("Should have failed");
        } catch (ConversionException ce) {
            // Expected
        }

        try {
            converter.convertFromStrings(Short.class, null, "testExpr", array("bad"));
            fail("Should have failed");
        } catch (ConversionException ce) {
            // Expected
        }

        try {
            converter.convertFromStrings(Integer.class, null, "testExpr", array("bad"));
            fail("Should have failed");
        } catch (ConversionException ce) {
            // Expected
        }

        try {
            converter.convertFromStrings(Long.class, null, "testExpr", array("bad"));
            fail("Should have failed");
        } catch (ConversionException ce) {
            // Expected
        }

        try {
            converter.convertFromStrings(Float.class, null, "testExpr", array("bad"));
            fail("Should have failed");
        } catch (ConversionException ce) {
            // Expected
        }

        try {
            converter.convertFromStrings(Double.class, null, "testExpr", array("bad"));
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
        GlobalConverter converter = new NumberConverter();
        String str = converter.convertToString(Integer.class, null, "testExpr", null);
        assertNull(str);

        str = converter.convertToString(Byte.class, null, "testExpr", (byte) 42);
        assertEquals("42", str);

        str = converter.convertToString(Byte.TYPE, null, "testExpr", (byte) 42);
        assertEquals("42", str);

        str = converter.convertToString(Short.class, null, "testExpr", (short) 42);
        assertEquals("42", str);

        str = converter.convertToString(Short.TYPE, null, "testExpr", (short) 42);
        assertEquals("42", str);

        str = converter.convertToString(Integer.class, null, "testExpr", 42);
        assertEquals("42", str);

        str = converter.convertToString(Integer.class, null, "testExpr", 42);
        assertEquals("42", str);

        str = converter.convertToString(Long.class, null, "testExpr", 42l);
        assertEquals("42", str);

        str = converter.convertToString(Long.TYPE, null, "testExpr", 42l);
        assertEquals("42", str);

        str = converter.convertToString(Float.class, null, "testExpr", 42f);
        assertEquals("42.0", str);

        str = converter.convertToString(Float.TYPE, null, "testExpr", 42f);
        assertEquals("42.0", str);

        str = converter.convertToString(Double.class, null, "testExpr", 42.0);
        assertEquals("42.0", str);

        str = converter.convertToString(Double.TYPE, null, "testExpr", 42.0);
        assertEquals("42.0", str);
    }
}

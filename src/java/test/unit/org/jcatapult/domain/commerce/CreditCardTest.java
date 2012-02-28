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
 *
 */
package org.jcatapult.domain.commerce;

import org.joda.time.YearMonthDay;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * <p>
 * This is a test class for the credit card domain object.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class CreditCardTest {
    /**
     * Tests the cc validation.
     */
    @Test
    public void testAll() {
        CreditCard cc = new CreditCard();
        cc.setNumber("4060324092945669");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("4411044090126247");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("4782006013461788");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("4427321110674998");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("5490990911132353");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("4427324090431868");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("4411044090573653");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("5105105105105100");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("5555555555554444");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("4111111111111111");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("4012888888881881");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("378282246310005");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("371449635398431");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("378734493671000");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("38520000023237");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("30569309025904");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("6011111111111117");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("6011000990139424");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("3530111333300000");
        Assert.assertTrue(cc.isNumberValid());
        cc.setNumber("3566002020360505");
        Assert.assertTrue(cc.isNumberValid());
    }

    /**
     * Test that the leap year code is working properly and that it always grab the last day of the
     * month.
     */
    @Test
    public void testExpirationDate() {
        CreditCard cc = new CreditCard();
        cc.setExpirationMonth(2);
        cc.setExpirationYear(2009);
        YearMonthDay ymd = cc.getExpirationDate();
        assertEquals(28, ymd.getDayOfMonth());

        cc.setExpirationMonth(4);
        cc.setExpirationYear(2010);
        ymd = cc.getExpirationDate();
        assertEquals(30, ymd.getDayOfMonth());
    }
}
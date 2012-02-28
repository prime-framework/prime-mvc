/*
 * Copyright (c) 2001-2006, JCatapult.org, All Rights Reserved
 */
package org.primeframework.domain.calendar;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * This tests the month for leap years.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class MonthTest {
    @Test
    public void testLeapYear() {
        Assert.assertEquals(29, Month.FEB.getDays(2000));
        Assert.assertEquals(29, Month.FEB.getDays(2004));
        Assert.assertEquals(29, Month.FEB.getDays(2008));
        Assert.assertEquals(28, Month.FEB.getDays(2009));
        Assert.assertEquals(28, Month.FEB.getDays(2100));
    }

    @Test
    public void testGetMonthOneBased() {
        Assert.assertEquals(Month.FEB, Month.getMonthOneBased(2));
        Assert.assertEquals(Month.MAY, Month.getMonthOneBased(5));

        try {
            Month.getMonthOneBased(13);
            Assert.fail("Should have failed.");
        } catch (Exception e) {
        }


        try {
            Month.getMonthOneBased(0);
            Assert.fail("Should have failed.");
        } catch (Exception e) {
        }
    }
}
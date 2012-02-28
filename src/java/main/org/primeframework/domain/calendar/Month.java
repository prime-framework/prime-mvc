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
package org.primeframework.domain.calendar;

import org.joda.time.YearMonthDay;

/**
 * <p>
 * This enumerates the months in the gregorian calendar and adds some extra
 * functionality to each month.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public enum Month {
    JAN(31),
    FEB(28),
    MAR(31),
    APR(30),
    MAY(31),
    JUN(30),
    JUL(31),
    AUG(31),
    SEP(30),
    OCT(31),
    NOV(30),
    DEC(31);

    private int days;

    Month(int days) {
        this.days = days;
    }

    /**
     * Returns the number of days for this month in the current year.
     *
     * @return  Returns the number of days in the month.
     */
    public int getDays() {
        return getDays(new YearMonthDay().getYear());
    }

    /**
     * Returns the number of days for this month in the given year. The year parameter is only applicable
     * for february due to leap years.
     *
     * @param   year The year to get the number of days for the month.
     * @return  Returns the number of days in the month.
     */
    public int getDays(int year) {
        if (this == FEB) {
            if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
                return 29;
            }

            return 28;
        }

        return days;
    }

    /**
     * Returns the month for the given integer value. This is one based so that if the value passed
     * in is 2, then February is returned.
     *
     * @param   month The one based month to return.
     * @return  The month and never null.
     * @throws  ArrayIndexOutOfBoundsException If the month parameter is less than 1 or greater than 12.
     */
    public static final Month getMonthOneBased(int month) {
        return values()[month - 1];
    }
}
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
 *
 */
package org.primeframework.domain.commerce;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.primeframework.domain.calendar.Month;
import org.primeframework.domain.location.Address;
import org.joda.time.YearMonthDay;

/**
 * <p>
 * This class stores the credit card information used during transaction
 * processing.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@MappedSuperclass
@Embeddable
public class CreditCard implements Serializable {
    @Column(nullable = false, length = 20)
    private String number;
    @Transient
    private String svn;
    @Column(nullable = false, name = "expiration_month")
    private Integer expirationMonth;
    @Column(nullable = false, name = "expiration_year")
    private Integer expirationYear;
    @Column(nullable = false)
    private boolean verified;
    @Column(nullable = false, length = 255, name = "first_name")
    private String firstName;
    @Column(nullable = false, length = 255, name = "last_name")
    private String lastName;
    private Address address;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * @return  A displayable partial number for the credit card that contains 10 astrixes and the last
     *          4 digits from the card.
     */
    public String getPartialNumber() {
        return "**********" + number.substring(number.length() - 5, number.length());
    }

    /**
     * A transient value for the security verification number on the back of the card. This is
     * transient in the fact that it cannot be persited via JPA. It can however be serialized.
     *
     * @return   The SVN.
     */
    public String getSvn() {
        return svn;
    }

    /**
     * A transient value for the security verification number on the back of the card. This is
     * transient in the fact that it cannot be persited via JPA. It can however be serialized.
     *
     * @param   svn The SVN.
     */
    public void setSvn(String svn) {
        this.svn = svn;
    }

    public Integer getExpirationMonth() {
        return expirationMonth;
    }

    public void setExpirationMonth(Integer expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    public Integer getExpirationYear() {
        return expirationYear;
    }

    public void setExpirationYear(Integer expirationYear) {
        this.expirationYear = expirationYear;
    }

    /**
     * @return  A YearMonthDay that is the expiration date. This uses the last day of the month since
     *          most credit cards expire on the last day of the month.
     *
     */
    public YearMonthDay getExpirationDate() {
        int day = Month.getMonthOneBased(expirationMonth).getDays(expirationYear);
        return new YearMonthDay(expirationYear, expirationMonth, day);
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * @return  Uses the standard credit card validation to determine if this credit card is valid
     *          or not. This method can easily be found online by searching for "credit card
     *          validation".
     */
    public boolean isNumberValid() {
        int len = number.length();
        if (number.startsWith("34") || number.startsWith("37") && len == 15 && isValid(number)) {
            return true;
        } else if (number.startsWith("36") || number.startsWith("38") ||
                number.startsWith("300") || number.startsWith("301") ||
                number.startsWith("302") || number.startsWith("303") ||
                number.startsWith("304") || number.startsWith("305") && len == 14 && isValid(number)) {
            return true;
        } else if (number.startsWith("6011") && len == 16 && isValid(number)) {
            return true;
        } else if (number.startsWith("3") && len == 16) {
            return true;
        } else if (number.startsWith("5020") || number.startsWith("6") && len == 16) {
            return true;
        } else if (number.startsWith("51") || number.startsWith("52") ||
                number.startsWith("53") || number.startsWith("54") ||
                number.startsWith("55") && len == 16 && isValid(number)) {
            return true;
        } else if (number.startsWith("63") || number.startsWith("6767") && len >= 16 & len <= 20) {
            return true;
        } else if (number.startsWith("4903") || number.startsWith("4905") ||
                number.startsWith("4911") || number.startsWith("4936") ||
                number.startsWith("564182") || number.startsWith("633110") ||
                number.startsWith("6333") || number.startsWith("6759") && len >= 16 && len <= 20) {
            return true;
        } else if (number.startsWith("4") && len == 16 && isValid(number)) {
            return true;
        }

        return false;
    }

    /**
     * Compares the address to the object for equality. This comparison uses the number, expiration
     * month and year, first and last names, and the address.
     *
     * @param   o The object to compare against.
     * @return  True if the object is a CreditCard and it is equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreditCard)) return false;

        CreditCard that = (CreditCard) o;

        if (!address.equals(that.address)) return false;
        if (!expirationMonth.equals(that.expirationMonth)) return false;
        if (!expirationYear.equals(that.expirationYear)) return false;
        if (!firstName.equals(that.firstName)) return false;
        if (!lastName.equals(that.lastName)) return false;
        if (!number.equals(that.number)) return false;

        return true;
    }

    /**
     * Generates a hash code using the number, expiration month and year, first and last names, and
     * the address.
     *
     * @return  The hash code.
     */
    @Override
    public int hashCode() {
        int result = number.hashCode();
        result = 31 * result + expirationMonth.hashCode();
        result = 31 * result + expirationYear.hashCode();
        result = 31 * result + firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + address.hashCode();
        return result;
    }

    /**
     * Outputs a String for debugging. This includes the number in full because it is often necessary
     * to help debug gateway issues. However, you should never use this in production.
     *
     * @return  The credit card.
     */
    public String toString() {
        return "CC #[" + number + "] svn[" + svn + "] expiry[" + expirationMonth + "/" + expirationYear +
            "] address[" + address + "]";
    }

    private static int[] lookup = {0, 2, 4, 6, 8, 1, 3, 5, 7, 9};

    private boolean isValid(String number) {
        number = number.trim();
        char[] ca = number.toCharArray();
        int sum = 0;
        for (int i = 0; i < ca.length; i++) {
            char c = ca[ca.length - 1 - i];
            int value = Character.digit(c, 10);
            if (i % 2 == 1) {
                sum += lookup[value];
            } else {
                sum += value;
            }
        }

        return (sum % 10 == 0);
    }
}
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
package org.primeframework.mvc.domain.commerce;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

/**
 * <p> This class represents an internationalized monetary value. </p>
 *
 * @author Brian Pontarelli
 */
public class Money implements Serializable, Comparable<Money> {
  public static final Money ZERO_USD = Money.valueOf("0.0", Currency.getInstance("USD"));
  private final static int serialVersionUID = 1;

  private final BigDecimal amount;
  private final Currency currency;

  private Money(String amount, Currency currency) {
    this(new BigDecimal(amount), currency);
  }

  private Money(BigDecimal amount, Currency currency) {
    if (amount == null || currency == null) {
      throw new NullPointerException("The amount and currency are required for a Money");
    }

    this.amount = amount;
    this.currency = currency;
  }

  /**
   * Creates a new Money using the numeric value from the given String, which must be in the correct format to be parsed
   * by the {@link BigDecimal#BigDecimal(String)} constructor.
   *
   * @param amount   The amount.
   * @param currency The currency.
   * @return The Money.
   * @throws NullPointerException If the amount or currency are null.
   */
  public static Money valueOf(String amount, Currency currency) {
    return new Money(amount, currency);
  }

  /**
   * Creates a new Money using the numeric value from the given BigDecimal. No rounding occurs in order to maintain
   * precision.
   *
   * @param amount   The amount.
   * @param currency The currency.
   * @return The Money.
   * @throws NullPointerException If the amount or currency are null.
   */
  public static Money valueOf(BigDecimal amount, Currency currency) {
    return new Money(amount, currency);
  }

  /**
   * Creates a new Money using the numeric value from the given String, which must be in the correct format to be parsed
   * by the {@link BigDecimal#BigDecimal(String)} constructor, in US Dollars.
   *
   * @param amount The amount.
   * @return The Money.
   * @throws NullPointerException If the amount or currency are null.
   */
  public static Money valueOfUSD(String amount) {
    return new Money(amount, Currency.getInstance("USD"));
  }

  /**
   * Creates a new Money using the numeric value from the given BigDecimal in US Dollars. No rounding occurs in order to
   * maintain precision.
   *
   * @param amount The amount.
   * @return The Money.
   * @throws NullPointerException If the amount or currency are null.
   */
  public static Money valueOfUSD(BigDecimal amount) {
    return new Money(amount, Currency.getInstance("USD"));
  }

  /**
   * @return The currency of this Money.
   */
  public Currency getCurrency() {
    return currency;
  }

  /**
   * Adds the given ammount to this Money and returns the result.
   *
   * @param that The amount to add.
   * @return A new Money with the result of the addition.
   * @throws IllegalArgumentException If the currencies don't match.
   */
  public Money plus(Money that) {
    if (!currency.equals(that.currency)) {
      throw new IllegalArgumentException("Money currencies [" + currency + " and " + that.currency +
        "] don't match. Cannot add Monies.");
    }

    return new Money(amount.add(that.amount), currency);
  }

  /**
   * Subtracts the given ammount to this Money and returns the result.
   *
   * @param that The amount to subtract .
   * @return A new Money with the result of the subtraction .
   * @throws IllegalArgumentException If the currencies don't match.
   */
  public Money minus(Money that) {
    if (!currency.equals(that.currency)) {
      throw new IllegalArgumentException("Money currencies [" + currency + " and " + that.currency +
        "] don't match. Cannot subtract Monies.");
    }

    return new Money(amount.subtract(that.amount), currency);
  }

  /**
   * Multiplies this Money by the given long and returns the result in a new Money.
   *
   * @param amount The amount to multiply by.
   * @return A new Money with the result of the multiplication.
   */
  public Money times(long amount) {
    return new Money(this.amount.multiply(new BigDecimal(amount)), currency);
  }

  /**
   * Multiplies this Money by the given Double and returns the result in a new Money.
   *
   * @param amount The amount to multiply by.
   * @return A new Money with the result of the multiplication.
   */
  public Money times(double amount) {
    return new Money(this.amount.multiply(new BigDecimal(amount)), currency);
  }

  /**
   * Divides this Money by the given long and returns the result in a new Money.
   *
   * @param amount The amount to divide by.
   * @return A new Money with the result of the division.
   */
  public Money dividedBy(long amount) {
    return new Money(this.amount.divide(new BigDecimal(amount)), currency);
  }

  /**
   * Divides this Money by the given double and returns the result in a new Money.
   *
   * @param amount The amount to divide by.
   * @return A new Money with the result of the division.
   */
  public Money dividedBy(double amount) {
    return new Money(this.amount.divide(new BigDecimal(amount)), currency);
  }

  /**
   * Returns the amount of this Money as a long. This is the result of truncating any decimal part of the amount and
   * returning it.
   *
   * @return The long value of the money.
   */
  public long longValue() {
    return amount.longValue();
  }

  /**
   * Returns this Money as a double. This conversion is not precise because it is the conversion from a BigDecimal to a
   * double, which is lossy. This method should be avoided at all costs.
   *
   * @return The double value of the Money.
   */
  public double doubleValue() {
    return amount.doubleValue();
  }

  /**
   * Returns the amount of this Money as a BigDecimal. The is the only precise way to get access to the amount of this
   * Money.
   *
   * @return The amount of the Money.
   */
  public BigDecimal toBigDecimal() {
    return amount;
  }

  /**
   * @return A String representation of the money that has been rounded to the currencies fractional digits.
   */
  public String toNumericString() {
    BigDecimal bd = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    return bd.toPlainString();
  }

  /**
   * @return A String of the money with the currency code for the current locale.
   */
  public String toString() {
    return currency.getSymbol() + toNumericString();
  }

  /**
   * @param locale The locale to use for displaying the currency code.
   * @return A String of the money with the currency code for the givan locale.
   */
  public String toString(Locale locale) {
    return currency.getSymbol(locale) + toNumericString();
  }

  /**
   * This method compares the rounded values, which nearly always be equivalent depending on how that value was
   * acquired. For example, 1.00/3 == 0.33. This is computed by converting the two Monies to rounded Strings and
   * comparing the Strings.
   *
   * @param money The Money to compare.
   * @return True if they are rounded and then equal, false if not.
   */
  public boolean equalsRounded(Money money) {
    if (this == money) return true;
    String str = toNumericString();
    String other = money.toNumericString();
    if (!str.equals(other)) return false;
    if (!currency.equals(money.currency)) return false;

    return true;
  }

  /**
   * This method compares the exact values, which often will not be equivalent depending on how that value was acquired.
   * For example, 1.00/3 != 0.33.
   *
   * @param money The Money to compare.
   * @return True if they are exactly equal, false if not.
   */
  public boolean equalsExact(Money money) {
    if (this == money) return true;
    if (amount.compareTo(money.amount) != 0) return false;
    if (!currency.equals(money.currency)) return false;

    return true;
  }

  /**
   * This throws an exception currently because of rounding issues.
   *
   * @param obj Not used.
   * @return Nothing.
   */
  public boolean equals(Object obj) {
    throw new AssertionError("Equals is not implemented on Money because of really hairy issues " +
      "with rounding and maintaining transitive/associative properties of this object.");
  }

  /**
   * @return True IFF the amount of this Money, when rounded to the number of fractional digits of the currency of the
   *         Money, is exactly equal to 0. For example, a Money value of <strong>0.003 USD</strong> (three tenths of a
   *         percentage of a penny in U.S. dollars) will return true because this is less than zero and when rounded up
   *         will still be zero.
   */
  public boolean isZero() {
    BigDecimal bd = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    return bd.compareTo(new BigDecimal("0")) == 0;
  }

  /**
   * @return True IFF the amount of this Money has a value that is exactly equal to 0. For example, a Money value of
   *         <strong>0.003 USD</strong> (three tenths of a percentage of a penny in U.S. dollars) will return false
   *         because this not exactly zero.
   */
  public boolean isZeroExact() {
    return amount.compareTo(new BigDecimal("0")) == 0;
  }

  /**
   * Compares the given Money to this Money. If the currencies are not the same, this throws an exception.
   *
   * @param o The Money to compare with this Money.
   * @return Positive number if this Money is more, negative it if is less, zero if they are equal.
   * @throws IllegalArgumentException If the currencies of this Money and the given Money aren't the same.
   */
  public int compareTo(Money o) {
    if (!currency.equals(o.currency)) {
      throw new IllegalArgumentException("Unable to compare monies with different currencies");
    }

    return amount.compareTo(o.amount);
  }

  /**
   * Determines if the given Money is less than this Money. If the currencies are not the same, this throws an
   * exception.
   *
   * @param money The Money to compare with this Money.
   * @return True if this Money is less than the given Money.
   * @throws IllegalArgumentException If the currencies of this Money and the given Money aren't the same.
   */
  public boolean isLessThan(Money money) {
    return compareTo(money) < 0;
  }

  /**
   * Determines if the given Money is less than or equal to this Money. If the currencies are not the same, this throws
   * an exception.
   *
   * @param money The Money to compare with this Money.
   * @return True if this Money is less than or equal to the given Money.
   * @throws IllegalArgumentException If the currencies of this Money and the given Money aren't the same.
   */
  public boolean isLessThanOrEqualTo(Money money) {
    return compareTo(money) <= 0;
  }

  /**
   * Determines if the given Money is greater than this Money. If the currencies are not the same, this throws an
   * exception.
   *
   * @param money The Money to compare with this Money.
   * @return True if this Money is greater than the given Money.
   * @throws IllegalArgumentException If the currencies of this Money and the given Money aren't the same.
   */
  public boolean isGreaterThan(Money money) {
    return compareTo(money) > 0;
  }

  /**
   * Determines if the given Money is greater than or equal to this Money. If the currencies are not the same, this
   * throws an exception.
   *
   * @param money The Money to compare with this Money.
   * @return True if this Money is greater than or equal to the given Money.
   * @throws IllegalArgumentException If the currencies of this Money and the given Money aren't the same.
   */
  public boolean isGreaterThanOrEqualTo(Money money) {
    return compareTo(money) >= 0;
  }

  public int hashCode() {
    int result;
    result = amount.hashCode();
    result = 31 * result + currency.hashCode();
    return result;
  }
}
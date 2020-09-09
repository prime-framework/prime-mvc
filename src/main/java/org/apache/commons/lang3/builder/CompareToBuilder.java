/*
 * Copyright (c) 2020, Inversoft Inc., All Rights Reserved
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
package org.apache.commons.lang3.builder;

import java.util.Comparator;

public class CompareToBuilder implements Builder<Integer> {

  /**
   * Current state of the comparison as appended fields are checked.
   */
  private int comparison;

  /**
   * <p>Constructor for CompareToBuilder.</p>
   *
   * <p>Starts off assuming that the objects are equal. Multiple calls are
   * then made to the various append methods, followed by a call to {@link #toComparison} to get the result.</p>
   */
  public CompareToBuilder() {
    super();
    comparison = 0;
  }

  //-----------------------------------------------------------------------

  /**
   * <p>Appends to the {@code builder} the comparison of
   * two {@code Object}s.</p>
   *
   * <ol>
   * <li>Check if {@code lhs == rhs}</li>
   * <li>Check if either {@code lhs} or {@code rhs} is {@code null},
   *     a {@code null} object is less than a non-{@code null} object</li>
   * <li>Check the object contents</li>
   * </ol>
   *
   * <p>{@code lhs} must either be an array or implement {@link Comparable}.</p>
   *
   * @param lhs left-hand object
   * @param rhs right-hand object
   * @return this - used to chain append calls
   * @throws ClassCastException if {@code rhs} is not assignment-compatible with {@code lhs}
   */
  public CompareToBuilder append(final Object lhs, final Object rhs) {
    return append(lhs, rhs, null);
  }

  /**
   * <p>Appends to the {@code builder} the comparison of
   * two {@code Object}s.</p>
   *
   * <ol>
   * <li>Check if {@code lhs == rhs}</li>
   * <li>Check if either {@code lhs} or {@code rhs} is {@code null},
   *     a {@code null} object is less than a non-{@code null} object</li>
   * <li>Check the object contents</li>
   * </ol>
   *
   * <p>If {@code lhs} is an array, array comparison methods will be used.
   * Otherwise {@code comparator} will be used to compare the objects.
   * If {@code comparator} is {@code null}, {@code lhs} must
   * implement {@link Comparable} instead.</p>
   *
   * @param lhs        left-hand object
   * @param rhs        right-hand object
   * @param comparator {@code Comparator} used to compare the objects, {@code null} means treat lhs as {@code
   *                   Comparable}
   * @return this - used to chain append calls
   * @throws ClassCastException if {@code rhs} is not assignment-compatible with {@code lhs}
   * @since 2.0
   */
  public CompareToBuilder append(final Object lhs, final Object rhs, final Comparator<?> comparator) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs == null) {
      comparison = -1;
      return this;
    }
    if (rhs == null) {
      comparison = 1;
      return this;
    }
    if (lhs.getClass().isArray()) {
      // factor out array case in order to keep method small enough to be inlined
      appendArray(lhs, rhs, comparator);
    } else {
      // the simple case, not an array, just test the element
      if (comparator == null) {
        @SuppressWarnings("unchecked") // assume this can be done; if not throw CCE as per Javadoc
        final Comparable<Object> comparable = (Comparable<Object>) lhs;
        comparison = comparable.compareTo(rhs);
      } else {
        @SuppressWarnings("unchecked") // assume this can be done; if not throw CCE as per Javadoc
        final Comparator<Object> comparator2 = (Comparator<Object>) comparator;
        comparison = comparator2.compare(lhs, rhs);
      }
    }
    return this;
  }

  /**
   * Appends to the {@code builder} the comparison of two {@code long}s.
   *
   * @param lhs left-hand value
   * @param rhs right-hand value
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final long lhs, final long rhs) {
    if (comparison != 0) {
      return this;
    }
    comparison = Long.compare(lhs, rhs);
    return this;
  }

  //-------------------------------------------------------------------------

  /**
   * Appends to the {@code builder} the comparison of two {@code int}s.
   *
   * @param lhs left-hand value
   * @param rhs right-hand value
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final int lhs, final int rhs) {
    if (comparison != 0) {
      return this;
    }
    comparison = Integer.compare(lhs, rhs);
    return this;
  }

  /**
   * Appends to the {@code builder} the comparison of two {@code short}s.
   *
   * @param lhs left-hand value
   * @param rhs right-hand value
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final short lhs, final short rhs) {
    if (comparison != 0) {
      return this;
    }
    comparison = Short.compare(lhs, rhs);
    return this;
  }

  /**
   * Appends to the {@code builder} the comparison of two {@code char}s.
   *
   * @param lhs left-hand value
   * @param rhs right-hand value
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final char lhs, final char rhs) {
    if (comparison != 0) {
      return this;
    }
    comparison = Character.compare(lhs, rhs);
    return this;
  }

  /**
   * Appends to the {@code builder} the comparison of two {@code byte}s.
   *
   * @param lhs left-hand value
   * @param rhs right-hand value
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final byte lhs, final byte rhs) {
    if (comparison != 0) {
      return this;
    }
    comparison = Byte.compare(lhs, rhs);
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the comparison of
   * two {@code double}s.</p>
   *
   * <p>This handles NaNs, Infinities, and {@code -0.0}.</p>
   *
   * <p>It is compatible with the hash code generated by
   * {@code HashCodeBuilder}.</p>
   *
   * @param lhs left-hand value
   * @param rhs right-hand value
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final double lhs, final double rhs) {
    if (comparison != 0) {
      return this;
    }
    comparison = Double.compare(lhs, rhs);
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the comparison of
   * two {@code float}s.</p>
   *
   * <p>This handles NaNs, Infinities, and {@code -0.0}.</p>
   *
   * <p>It is compatible with the hash code generated by
   * {@code HashCodeBuilder}.</p>
   *
   * @param lhs left-hand value
   * @param rhs right-hand value
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final float lhs, final float rhs) {
    if (comparison != 0) {
      return this;
    }
    comparison = Float.compare(lhs, rhs);
    return this;
  }

  /**
   * Appends to the {@code builder} the comparison of two {@code booleans}s.
   *
   * @param lhs left-hand value
   * @param rhs right-hand value
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final boolean lhs, final boolean rhs) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs) {
      comparison = 1;
    } else {
      comparison = -1;
    }
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the deep comparison of
   * two {@code Object} arrays.</p>
   *
   * <ol>
   *  <li>Check if arrays are the same using {@code ==}</li>
   *  <li>Check if for {@code null}, {@code null} is less than non-{@code null}</li>
   *  <li>Check array length, a short length array is less than a long length array</li>
   *  <li>Check array contents element by element using {@link #append(Object, Object, Comparator)}</li>
   * </ol>
   *
   * <p>This method will also will be called for the top level of multi-dimensional,
   * ragged, and multi-typed arrays.</p>
   *
   * @param lhs left-hand array
   * @param rhs right-hand array
   * @return this - used to chain append calls
   * @throws ClassCastException if {@code rhs} is not assignment-compatible with {@code lhs}
   */
  public CompareToBuilder append(final Object[] lhs, final Object[] rhs) {
    return append(lhs, rhs, null);
  }

  //-----------------------------------------------------------------------

  /**
   * <p>Appends to the {@code builder} the deep comparison of
   * two {@code Object} arrays.</p>
   *
   * <ol>
   *  <li>Check if arrays are the same using {@code ==}</li>
   *  <li>Check if for {@code null}, {@code null} is less than non-{@code null}</li>
   *  <li>Check array length, a short length array is less than a long length array</li>
   *  <li>Check array contents element by element using {@link #append(Object, Object, Comparator)}</li>
   * </ol>
   *
   * <p>This method will also will be called for the top level of multi-dimensional,
   * ragged, and multi-typed arrays.</p>
   *
   * @param lhs        left-hand array
   * @param rhs        right-hand array
   * @param comparator {@code Comparator} to use to compare the array elements, {@code null} means to treat {@code lhs}
   *                   elements as {@code Comparable}.
   * @return this - used to chain append calls
   * @throws ClassCastException if {@code rhs} is not assignment-compatible with {@code lhs}
   * @since 2.0
   */
  public CompareToBuilder append(final Object[] lhs, final Object[] rhs, final Comparator<?> comparator) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs == null) {
      comparison = -1;
      return this;
    }
    if (rhs == null) {
      comparison = 1;
      return this;
    }
    if (lhs.length != rhs.length) {
      comparison = lhs.length < rhs.length ? -1 : 1;
      return this;
    }
    for (int i = 0; i < lhs.length && comparison == 0; i++) {
      append(lhs[i], rhs[i], comparator);
    }
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the deep comparison of
   * two {@code long} arrays.</p>
   *
   * <ol>
   *  <li>Check if arrays are the same using {@code ==}</li>
   *  <li>Check if for {@code null}, {@code null} is less than non-{@code null}</li>
   *  <li>Check array length, a shorter length array is less than a longer length array</li>
   *  <li>Check array contents element by element using {@link #append(long, long)}</li>
   * </ol>
   *
   * @param lhs left-hand array
   * @param rhs right-hand array
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final long[] lhs, final long[] rhs) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs == null) {
      comparison = -1;
      return this;
    }
    if (rhs == null) {
      comparison = 1;
      return this;
    }
    if (lhs.length != rhs.length) {
      comparison = lhs.length < rhs.length ? -1 : 1;
      return this;
    }
    for (int i = 0; i < lhs.length && comparison == 0; i++) {
      append(lhs[i], rhs[i]);
    }
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the deep comparison of
   * two {@code int} arrays.</p>
   *
   * <ol>
   *  <li>Check if arrays are the same using {@code ==}</li>
   *  <li>Check if for {@code null}, {@code null} is less than non-{@code null}</li>
   *  <li>Check array length, a shorter length array is less than a longer length array</li>
   *  <li>Check array contents element by element using {@link #append(int, int)}</li>
   * </ol>
   *
   * @param lhs left-hand array
   * @param rhs right-hand array
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final int[] lhs, final int[] rhs) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs == null) {
      comparison = -1;
      return this;
    }
    if (rhs == null) {
      comparison = 1;
      return this;
    }
    if (lhs.length != rhs.length) {
      comparison = lhs.length < rhs.length ? -1 : 1;
      return this;
    }
    for (int i = 0; i < lhs.length && comparison == 0; i++) {
      append(lhs[i], rhs[i]);
    }
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the deep comparison of
   * two {@code short} arrays.</p>
   *
   * <ol>
   *  <li>Check if arrays are the same using {@code ==}</li>
   *  <li>Check if for {@code null}, {@code null} is less than non-{@code null}</li>
   *  <li>Check array length, a shorter length array is less than a longer length array</li>
   *  <li>Check array contents element by element using {@link #append(short, short)}</li>
   * </ol>
   *
   * @param lhs left-hand array
   * @param rhs right-hand array
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final short[] lhs, final short[] rhs) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs == null) {
      comparison = -1;
      return this;
    }
    if (rhs == null) {
      comparison = 1;
      return this;
    }
    if (lhs.length != rhs.length) {
      comparison = lhs.length < rhs.length ? -1 : 1;
      return this;
    }
    for (int i = 0; i < lhs.length && comparison == 0; i++) {
      append(lhs[i], rhs[i]);
    }
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the deep comparison of
   * two {@code char} arrays.</p>
   *
   * <ol>
   *  <li>Check if arrays are the same using {@code ==}</li>
   *  <li>Check if for {@code null}, {@code null} is less than non-{@code null}</li>
   *  <li>Check array length, a shorter length array is less than a longer length array</li>
   *  <li>Check array contents element by element using {@link #append(char, char)}</li>
   * </ol>
   *
   * @param lhs left-hand array
   * @param rhs right-hand array
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final char[] lhs, final char[] rhs) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs == null) {
      comparison = -1;
      return this;
    }
    if (rhs == null) {
      comparison = 1;
      return this;
    }
    if (lhs.length != rhs.length) {
      comparison = lhs.length < rhs.length ? -1 : 1;
      return this;
    }
    for (int i = 0; i < lhs.length && comparison == 0; i++) {
      append(lhs[i], rhs[i]);
    }
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the deep comparison of
   * two {@code byte} arrays.</p>
   *
   * <ol>
   *  <li>Check if arrays are the same using {@code ==}</li>
   *  <li>Check if for {@code null}, {@code null} is less than non-{@code null}</li>
   *  <li>Check array length, a shorter length array is less than a longer length array</li>
   *  <li>Check array contents element by element using {@link #append(byte, byte)}</li>
   * </ol>
   *
   * @param lhs left-hand array
   * @param rhs right-hand array
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final byte[] lhs, final byte[] rhs) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs == null) {
      comparison = -1;
      return this;
    }
    if (rhs == null) {
      comparison = 1;
      return this;
    }
    if (lhs.length != rhs.length) {
      comparison = lhs.length < rhs.length ? -1 : 1;
      return this;
    }
    for (int i = 0; i < lhs.length && comparison == 0; i++) {
      append(lhs[i], rhs[i]);
    }
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the deep comparison of
   * two {@code double} arrays.</p>
   *
   * <ol>
   *  <li>Check if arrays are the same using {@code ==}</li>
   *  <li>Check if for {@code null}, {@code null} is less than non-{@code null}</li>
   *  <li>Check array length, a shorter length array is less than a longer length array</li>
   *  <li>Check array contents element by element using {@link #append(double, double)}</li>
   * </ol>
   *
   * @param lhs left-hand array
   * @param rhs right-hand array
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final double[] lhs, final double[] rhs) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs == null) {
      comparison = -1;
      return this;
    }
    if (rhs == null) {
      comparison = 1;
      return this;
    }
    if (lhs.length != rhs.length) {
      comparison = lhs.length < rhs.length ? -1 : 1;
      return this;
    }
    for (int i = 0; i < lhs.length && comparison == 0; i++) {
      append(lhs[i], rhs[i]);
    }
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the deep comparison of
   * two {@code float} arrays.</p>
   *
   * <ol>
   *  <li>Check if arrays are the same using {@code ==}</li>
   *  <li>Check if for {@code null}, {@code null} is less than non-{@code null}</li>
   *  <li>Check array length, a shorter length array is less than a longer length array</li>
   *  <li>Check array contents element by element using {@link #append(float, float)}</li>
   * </ol>
   *
   * @param lhs left-hand array
   * @param rhs right-hand array
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final float[] lhs, final float[] rhs) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs == null) {
      comparison = -1;
      return this;
    }
    if (rhs == null) {
      comparison = 1;
      return this;
    }
    if (lhs.length != rhs.length) {
      comparison = lhs.length < rhs.length ? -1 : 1;
      return this;
    }
    for (int i = 0; i < lhs.length && comparison == 0; i++) {
      append(lhs[i], rhs[i]);
    }
    return this;
  }

  /**
   * <p>Appends to the {@code builder} the deep comparison of
   * two {@code boolean} arrays.</p>
   *
   * <ol>
   *  <li>Check if arrays are the same using {@code ==}</li>
   *  <li>Check if for {@code null}, {@code null} is less than non-{@code null}</li>
   *  <li>Check array length, a shorter length array is less than a longer length array</li>
   *  <li>Check array contents element by element using {@link #append(boolean, boolean)}</li>
   * </ol>
   *
   * @param lhs left-hand array
   * @param rhs right-hand array
   * @return this - used to chain append calls
   */
  public CompareToBuilder append(final boolean[] lhs, final boolean[] rhs) {
    if (comparison != 0) {
      return this;
    }
    if (lhs == rhs) {
      return this;
    }
    if (lhs == null) {
      comparison = -1;
      return this;
    }
    if (rhs == null) {
      comparison = 1;
      return this;
    }
    if (lhs.length != rhs.length) {
      comparison = lhs.length < rhs.length ? -1 : 1;
      return this;
    }
    for (int i = 0; i < lhs.length && comparison == 0; i++) {
      append(lhs[i], rhs[i]);
    }
    return this;
  }

  /**
   * Returns a negative Integer, a positive Integer, or zero as the {@code builder} has judged the "left-hand" side as
   * less than, greater than, or equal to the "right-hand" side.
   *
   * @return final comparison result as an Integer
   * @see #toComparison()
   * @since 3.0
   */
  @Override
  public Integer build() {
    return Integer.valueOf(toComparison());
  }

  //-----------------------------------------------------------------------

  /**
   * Returns a negative integer, a positive integer, or zero as the {@code builder} has judged the "left-hand" side as
   * less than, greater than, or equal to the "right-hand" side.
   *
   * @return final comparison result
   * @see #build()
   */
  public int toComparison() {
    return comparison;
  }

  private void appendArray(final Object lhs, final Object rhs, final Comparator<?> comparator) {
    // switch on type of array, to dispatch to the correct handler
    // handles multi dimensional arrays
    // throws a ClassCastException if rhs is not the correct array type
    if (lhs instanceof long[]) {
      append((long[]) lhs, (long[]) rhs);
    } else if (lhs instanceof int[]) {
      append((int[]) lhs, (int[]) rhs);
    } else if (lhs instanceof short[]) {
      append((short[]) lhs, (short[]) rhs);
    } else if (lhs instanceof char[]) {
      append((char[]) lhs, (char[]) rhs);
    } else if (lhs instanceof byte[]) {
      append((byte[]) lhs, (byte[]) rhs);
    } else if (lhs instanceof double[]) {
      append((double[]) lhs, (double[]) rhs);
    } else if (lhs instanceof float[]) {
      append((float[]) lhs, (float[]) rhs);
    } else if (lhs instanceof boolean[]) {
      append((boolean[]) lhs, (boolean[]) rhs);
    } else {
      // not an array of primitives
      // throws a ClassCastException if rhs is not an array
      append((Object[]) lhs, (Object[]) rhs, comparator);
    }
  }
}


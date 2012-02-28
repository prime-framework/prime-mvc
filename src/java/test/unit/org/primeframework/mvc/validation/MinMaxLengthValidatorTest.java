/*
 * Copyright (c) 2001-2011, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.validation;

import java.lang.annotation.Annotation;

import org.primeframework.mvc.validation.annotation.MinMaxLength;
import org.testng.annotations.Test;

import static java.util.Arrays.*;
import static net.java.util.CollectionTools.*;
import static org.testng.Assert.*;

/**
 * <p> This class tests the min max validator. </p>
 *
 * @author Brian Pontarelli
 */
public class MinMaxLengthValidatorTest {
  @Test
  public void collections() {
    MinMaxLengthValidator validator = new MinMaxLengthValidator();
    assertTrue(validator.validate(new MinMaxLengthImpl(0, 10), null, asList(1, 2, 3)));
    assertFalse(validator.validate(new MinMaxLengthImpl(0, 2), null, asList(1, 2, 3)));
    assertFalse(validator.validate(new MinMaxLengthImpl(4, 10), null, asList(1, 2, 3)));
  }

  @Test
  public void arrays() {
    MinMaxLengthValidator validator = new MinMaxLengthValidator();
    assertTrue(validator.validate(new MinMaxLengthImpl(0, 10), null, array(1, 2, 3)));
    assertFalse(validator.validate(new MinMaxLengthImpl(0, 2), null, array(1, 2, 3)));
    assertFalse(validator.validate(new MinMaxLengthImpl(4, 10), null, array(1, 2, 3)));

    assertTrue(validator.validate(new MinMaxLengthImpl(0, 10), null, new int[]{1, 2, 3}));
    assertFalse(validator.validate(new MinMaxLengthImpl(0, 2), null, new int[]{1, 2, 3}));
    assertFalse(validator.validate(new MinMaxLengthImpl(4, 10), null, new int[]{1, 2, 3}));
  }

  @Test
  public void maps() {
    MinMaxLengthValidator validator = new MinMaxLengthValidator();
    assertTrue(validator.validate(new MinMaxLengthImpl(0, 10), null, map("1", "2", "3", "4", "5", "6")));
    assertFalse(validator.validate(new MinMaxLengthImpl(0, 2), null, map("1", "2", "3", "4", "5", "6")));
    assertFalse(validator.validate(new MinMaxLengthImpl(4, 10), null, map("1", "2", "3", "4", "5", "6")));
  }

  @Test
  public void strings() {
    MinMaxLengthValidator validator = new MinMaxLengthValidator();
    assertTrue(validator.validate(new MinMaxLengthImpl(0, 10), null, "123"));
    assertFalse(validator.validate(new MinMaxLengthImpl(0, 2), null, "123"));
    assertFalse(validator.validate(new MinMaxLengthImpl(4, 10), null, "123"));
  }

  @Test
  public void other() {
    Object obj = new Object();
    MinMaxLengthValidator validator = new MinMaxLengthValidator();
    assertTrue(validator.validate(new MinMaxLengthImpl(0, 50), null, obj));
    assertFalse(validator.validate(new MinMaxLengthImpl(0, 20), null, obj));
    assertFalse(validator.validate(new MinMaxLengthImpl(60, 100), null, obj));
  }

  public class MinMaxLengthImpl implements MinMaxLength {
    private final int min;
    private final int max;

    public MinMaxLengthImpl(int min, int max) {
      this.min = min;
      this.max = max;
    }

    @Override
    public int min() {
      return min;
    }

    @Override
    public int max() {
      return max;
    }

    @Override
    public String key() {
      return null;
    }

    @Override
    public String[] groups() {
      return new String[0];
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return MinMaxLength.class;
    }
  }
}
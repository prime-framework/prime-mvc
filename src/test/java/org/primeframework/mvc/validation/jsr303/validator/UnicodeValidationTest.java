/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.validation.jsr303.validator;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author James Humphrey
 */
public class UnicodeValidationTest extends BaseValidatorUnitTest {

  public static final String UNICODE;

  static {
    int[] ia = new int[10];
    for (int i = 0; i < 10; i++) {
      ia[i] = 0x10000 + i;
    }
    UNICODE = new String(ia, 0, 10);
  }

  @Test
  public void isValid() {

    UnicodeValidator validator = new UnicodeValidator();

    // test true
    assertTrue(validator.isValid("foo", null));
    assertTrue(validator.isValid(null, null));

    // test false
    Assert.assertFalse(validator.isValid(UNICODE, null));
  }
}

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
package org.primeframework.mvc.parameter;

import org.primeframework.mvc.PrimeBaseTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author jhumphrey
 */
public class InternalParametersTest extends PrimeBaseTest {
  @Test
  public void stringTrue() {
    request.setParameter(InternalParameters.EXECUTE_VALIDATION, "true");
    boolean keyState = InternalParameters.is(request, InternalParameters.EXECUTE_VALIDATION);
    assertTrue(keyState);
  }

  @Test
  public void stringFalse() {
    request.setParameter(InternalParameters.EXECUTE_VALIDATION, "false");
    boolean keyState = InternalParameters.is(request, InternalParameters.EXECUTE_VALIDATION);
    assertFalse(keyState);
  }

  @Test
  public void stringBad() {
    try {
      request.setParameter(InternalParameters.EXECUTE_VALIDATION, "bad");
      InternalParameters.is(request, InternalParameters.EXECUTE_VALIDATION);
      fail("Should have failed");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void booleanTrue() {
    request.setAttribute(InternalParameters.EXECUTE_VALIDATION, true);
    boolean keyState = InternalParameters.is(request, InternalParameters.EXECUTE_VALIDATION);
    assertTrue(keyState);
  }

  @Test
  public void booleanFalse() {
    request.setAttribute(InternalParameters.EXECUTE_VALIDATION, false);
    boolean keyState = InternalParameters.is(request, InternalParameters.EXECUTE_VALIDATION);
    assertFalse(keyState);
  }
}

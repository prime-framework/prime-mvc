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

import com.google.inject.Inject;
import org.primeframework.mvc.PrimeBaseTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Brian Pontarelli
 */
public class InternalParametersTest extends PrimeBaseTest {
  @Inject InternalParameters internalParameters;

  @Test
  public void booleanFalse() {
    request.setAttribute(InternalParameters.EXECUTE_VALIDATION, false);
    boolean keyState = internalParameters.is(request, InternalParameters.EXECUTE_VALIDATION);
    assertFalse(keyState);
  }

  @Test
  public void booleanTrue() {
    request.setAttribute(InternalParameters.EXECUTE_VALIDATION, true);
    boolean keyState = internalParameters.is(request, InternalParameters.EXECUTE_VALIDATION);
    assertTrue(keyState);
  }

  @Test
  public void stringBad() {
    try {
      request.addURLParameter(InternalParameters.EXECUTE_VALIDATION, "bad");
      internalParameters.is(request, InternalParameters.EXECUTE_VALIDATION);
      fail("Should have failed");
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void stringFalse() {
    request.addURLParameter(InternalParameters.EXECUTE_VALIDATION, "false");
    boolean keyState = internalParameters.is(request, InternalParameters.EXECUTE_VALIDATION);
    assertFalse(keyState);
  }

  @Test
  public void stringTrue() {
    request.addURLParameter(InternalParameters.EXECUTE_VALIDATION, "true");
    boolean keyState = internalParameters.is(request, InternalParameters.EXECUTE_VALIDATION);
    assertTrue(keyState);
  }
}

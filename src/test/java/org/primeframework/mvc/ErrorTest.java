/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc;

import org.testng.annotations.Test;

/**
 * Tests the error handling (500s).
 *
 * @author Brian Pontarelli
 */
public class ErrorTest extends PrimeBaseTest {
  @Test
  public void all() throws Exception {
    test.simulate(() -> simulator.test("/throws-exception")
                                 .withParameter("message", "foobar")
                                 .post()
                                 .assertStatusCode(500)
                                 .assertBodyContains("An error happened. Message was foobar. Called"));
  }
}

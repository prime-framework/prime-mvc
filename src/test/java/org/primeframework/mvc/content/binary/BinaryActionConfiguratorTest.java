/*
 * Copyright (c) 2016, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.content.binary;

import org.example.action.KitchenSinkAction;
import org.primeframework.mvc.PrimeBaseTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Daniel DeGroff
 */
public class BinaryActionConfiguratorTest extends PrimeBaseTest {
  @Test
  public void configure() {
    BinaryActionConfiguration config = (BinaryActionConfiguration) new BinaryActionConfigurator().configure(KitchenSinkAction.class);
    assertEquals(config.requestMember, "binaryRequest");
    assertEquals(config.responseMember, "binaryResponse");
  }
}

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
package org.primeframework.mvc.content.json;

import org.example.action.KitchenSink;
import org.example.domain.User;
import org.example.domain.UserField;
import org.primeframework.mvc.PrimeBaseTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests the jackson configurator test.
 *
 * @author Brian Pontarelli
 */
public class JacksonActionConfiguratorTest extends PrimeBaseTest {
  @Test
  public void configure() {
    JacksonActionConfiguration config = (JacksonActionConfiguration) new JacksonActionConfigurator().configure(KitchenSink.class);
    assertEquals(config.requestMember, "jsonRequest");
    assertEquals(config.requestMemberType, UserField.class);
    assertEquals(config.responseMember, "jsonResponse");
    assertEquals(config.responseMemberType, User.class);
  }
}
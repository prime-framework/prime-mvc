/*
 * Copyright (c) 2001-2022, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.config;

import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.util.DefaultURIBuilder;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * This class tests the default action URI builder.
 *
 * @author Brian Pontarelli
 */
public class DefaultURIBuilderTest {
  /**
   * Tests the URI builder.
   */
  @Test
  public void build() {
    DefaultURIBuilder builder = new DefaultURIBuilder();
    assertEquals(builder.build(DefaultURIBuilderTest.class), "/config/default-uri-builder-test");
    assertEquals(builder.build(MyCustomAction.class), "/config/my-custom");
  }
}

@Action
class MyCustomAction {
  public String get() {
    return "input";
  }
}
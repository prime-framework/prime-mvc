/*
 * Copyright (c) 2025, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.test;

import java.nio.file.Paths;
import java.util.Map;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class BodyToolsTest {
  @Test
  public void processTemplateWithMap_all_variables_used() throws Exception {
    // arrange
    Map<String, Object> values = Map.of("message", "howdy");

    // act
    String result = BodyTools.processTemplateWithMap(Paths.get("src/test/web/templates/echo.ftl"),
                                                     values,
                                                     false);

    // assert
    assertEquals(result, "howdy");
  }

  @Test
  public void processTemplateWithMap_unused_variables() throws Exception {
    // arrange
    Map<String, Object> values = Map.of("message", "howdy", "othervariable", "value");

    // act + assert
    try {
      BodyTools.processTemplateWithMap(Paths.get("src/test/web/templates/echo.ftl"),
                                       values,
                                       false);
      fail("Expected an exception");
    } catch (Exception e) {
      assertEquals(e.getClass(), IllegalArgumentException.class,
                   "Expected this exception type");
      assertEquals(e.getMessage(), "The following variables are not used in the [src/test/web/templates/echo.ftl] template: othervariable",
                   "othervariable is in the map but is not used");
    }
  }
}

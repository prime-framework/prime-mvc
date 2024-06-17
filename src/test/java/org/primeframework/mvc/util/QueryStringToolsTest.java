/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.util;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel DeGroff
 */
public class QueryStringToolsTest {
  @Test
  public void parseQueryString() {
    // Value w/out equals in the value
    assertEquals(QueryStringTools.parseQueryString("foo=bar"), Map.of("foo", List.of("bar")));
    assertEquals(QueryStringTools.parseQueryString("foo=bar&bar=bar"), Map.of("foo", List.of("bar"),
                                                                              "bar", List.of("bar")));

    // Value that contain an equals sign
    assertEquals(QueryStringTools.parseQueryString("foo=bar="), Map.of("foo", List.of("bar=")));
    assertEquals(QueryStringTools.parseQueryString("foo=bar=&bar=bar="), Map.of("foo", List.of("bar="),
                                                                                "bar", List.of("bar=")));

    // Value that contains multiple equals signs
    assertEquals(QueryStringTools.parseQueryString("foo=bar="), Map.of("foo", List.of("bar=")));
    assertEquals(QueryStringTools.parseQueryString("foo=bar=baz=&bar=foo=baz="), Map.of("foo", List.of("bar=baz="),
                                                                                        "bar", List.of("foo=baz=")));
  }
}

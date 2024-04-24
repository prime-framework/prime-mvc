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
package org.primeframework.mvc.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class FormDataBodyHandlerTest {
  @Test
  public void post_formData_string() {
    Map<String, List<String>> parameters = new LinkedHashMap<>();
    parameters.put("test1", singletonList("value1"));
    parameters.put("test2", singletonList("value2"));
    // Handle null values
    parameters.put("test3", new ArrayList<>(Arrays.asList("value3", null)));
    parameters.put("test4", null);

    var handler = new FormDataBodyHandler(parameters);
    String actualBody = new String(handler.getBody());

    assertEquals(actualBody, "test1=value1&test2=value2&test3=value3&test3=&test4=");
  }

  @Test
  public void post_formData_string_excludeNullValues() {
    Map<String, List<String>> parameters = new LinkedHashMap<>();
    parameters.put("test1", singletonList("value1"));
    parameters.put("test2", singletonList("value2"));
    // Handle null values
    parameters.put("test3", new ArrayList<>(Arrays.asList("value3", null)));
    parameters.put("test4", null);

    var handler = new FormDataBodyHandler(parameters, true);
    String actualBody = new String(handler.getBody());

    assertEquals(actualBody, "test1=value1&test2=value2&test3=value3");
  }
}

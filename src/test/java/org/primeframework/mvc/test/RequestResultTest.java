/*
 * Copyright (c) 2018-2024, Inversoft Inc., All Rights Reserved
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.primeframework.mvc.PrimeBaseTest;
import org.testng.annotations.Test;

/**
 * @author Daniel DeGroff
 */
public class RequestResultTest extends PrimeBaseTest {
  @Test
  public void arrays() throws IOException {
    Path jsonFile1 = Path.of("src/test/resources/json/SortedJSONArrays1.json");
    Path jsonFile2 = Path.of("src/test/resources/json/SortedJSONArrays2.json");
    RequestResult.assertJSONEquals(new ObjectMapper(), Files.readString(jsonFile1), Files.readString(jsonFile2));
  }

  @Test
  public void assertContentTypeIsJSON_correct() throws IOException {
    simulator.test("/api-final")
             .post()
             .assertStatusCode(200)
             .assertJSONValuesAt("/bar", false);
  }

  @Test(expectedExceptions = AssertionError.class,
      expectedExceptionsMessageRegExp = "Content-Type \\[null] does not start with the expected value.*")
  public void assertContentTypeIsJSON_incorrect() throws IOException {
    simulator.test("/api/no-content-type-json")
             .get()
             .assertStatusCode(200)
             .assertJSONValuesAt("/hello", 123);
  }

  @Test(enabled = false)
  public void keys() throws IOException {
    Path jsonFile1 = Path.of("src/test/resources/json/SortedJSONKeys1.json");
    Path jsonFile2 = Path.of("src/test/resources/json/SortedJSONKeys2.json");
    RequestResult.assertJSONEquals(new ObjectMapper(), Files.readString(jsonFile1), Files.readString(jsonFile2));
  }
}

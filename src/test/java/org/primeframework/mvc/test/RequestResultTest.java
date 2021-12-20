/*
 * Copyright (c) 2018, Inversoft Inc., All Rights Reserved
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
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/**
 * @author Daniel DeGroff
 */
public class RequestResultTest {
  @Test
  public void arrays() throws IOException {
    Path jsonFile1 = Path.of("src/test/resources/json/SortedJSONArrays1.json");
    Path jsonFile2 = Path.of("src/test/resources/json/SortedJSONArrays2.json");
    RequestResult.assertJSONEquals(new ObjectMapper(), Files.readFile(jsonFile1.toFile()), Files.readFile(jsonFile2.toFile()));
  }

  @Test(enabled = false)
  public void keys() throws IOException {
    Path jsonFile1 = Path.of("src/test/resources/json/SortedJSONKeys1.json");
    Path jsonFile2 = Path.of("src/test/resources/json/SortedJSONKeys2.json");
    RequestResult.assertJSONEquals(new ObjectMapper(), Files.readFile(jsonFile1.toFile()), Files.readFile(jsonFile2.toFile()));
  }
}

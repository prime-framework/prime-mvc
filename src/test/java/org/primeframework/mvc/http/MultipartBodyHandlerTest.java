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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

@Test
public class MultipartBodyHandlerTest {
  @Test
  public void post_formData_multiPart() throws Exception {
    Map<String, List<String>> parameters = new LinkedHashMap<>();
    parameters.put("test1", singletonList("value1"));
    parameters.put("test2", singletonList("value2 with space"));

    List<MultipartFileUpload> files = new ArrayList<>();
    files.add(new MultipartFileUpload("text/plain", Paths.get("src/test/java/org/primeframework/mvc/http/test-file.txt"), "foo.bar.txt", "formField"));

    MultipartBodyHandler.Multiparts request = new MultipartBodyHandler.Multiparts(files, parameters);
    MultipartBodyHandler bodyHandler = new MultipartBodyHandler(request);

    // Build the expected request
    String expectedBody = "--" + bodyHandler.boundary + "\r\n" +
                          "Content-Disposition: form-data; name=\"formField\"; filename=\"foo.bar.txt\"; filename*=UTF-8''foo.bar.txt\r\n" +
                          "Content-Type: text/plain\r\n\r\n" +
                          "1234\n\r\n" +
                          "--" + bodyHandler.boundary + "\r\n" +
                          "Content-Disposition: form-data; name=\"test1\"\r\n\r\n" +
                          "value1\r\n" +
                          "--" + bodyHandler.boundary + "\r\n" +
                          "Content-Disposition: form-data; name=\"test2\"\r\n\r\n" +
                          "value2 with space\r\n" +
                          "--" + bodyHandler.boundary + "--";

    byte[] actualBody = bodyHandler.getBody();
    assertEquals(new String(actualBody), expectedBody);
  }
}

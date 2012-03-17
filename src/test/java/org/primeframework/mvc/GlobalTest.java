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
package org.primeframework.mvc;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.primeframework.mvc.test.RequestSimulator;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * This class tests the MVC from a high level perspective.
 *
 * @author Brian Pontarelli
 */
public class GlobalTest extends PrimeBaseTest {
  @Test
  public void renderFTL() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator(context, new TestModule());
    simulator.test("/user/edit").get();
    String result = simulator.response.getOutputStream().toString();
    assertEquals(FileUtils.readFileToString(new File("src/test/java/org/primeframework/mvc/edit-output.txt")), result);
  }

  @Test
  public void nonFormFields() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator(context, new TestModule());
    simulator.test("/user/details-fields").get();
    assertEquals(FileUtils.readFileToString(new File("src/test/java/org/primeframework/mvc/details-fields-output.txt")),
      simulator.response.getOutputStream().toString());
  }
}
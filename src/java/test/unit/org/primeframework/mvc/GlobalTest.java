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
import java.io.IOException;

import org.primeframework.mvc.test.JCatapultBaseTest;
import org.primeframework.mvc.test.RequestSimulator;
import org.testng.annotations.Test;

import net.java.io.FileTools;

import static org.testng.Assert.*;

/**
 * <p> This class tests the MVC from a high level perspective. </p>
 *
 * @author Brian Pontarelli
 */
public class GlobalTest extends JCatapultBaseTest {
  @Test
  public void testRenderFTL() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator();
    simulator.test("/user/edit").get();
    assertEquals(FileTools.read("src/java/test/unit/org/primeframework/mvc/edit-output.txt").toString(),
      simulator.response.getStream().toString());
  }

  @Test
  public void testNonFormFields() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator();
    simulator.test("/user/details-fields").get();
    assertEquals(FileTools.read("src/java/test/unit/org/primeframework/mvc/details-fields-output.txt").toString(),
      simulator.response.getStream().toString());
  }
}
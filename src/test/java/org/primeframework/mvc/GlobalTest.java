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
import org.primeframework.mvc.parameter.el.BeanExpressionException;
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

  @Test
  public void postRender() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator(context, new TestModule());
    simulator.test("/post").post();
    String result = simulator.response.getOutputStream().toString();
    assertTrue(result.contains("Brian Pontarelli"));
    assertTrue(result.contains("35"));
    assertTrue(result.contains("Broomfield"));
    assertTrue(result.contains("CO"));
  }

  @Test
  public void invalidJavaBeanSetter() throws IOException, ServletException {
    // Testing that invalid JavaBean setter cause the whole thing to fail
    RequestSimulator simulator = new RequestSimulator(context, new TestModule());
    try {
      simulator.test("/invalid-java-bean-setter").
        get();
      fail("Should have thrown an exception");
    } catch (BeanExpressionException e) {
      // Expected
    }
  }

  @Test
  public void invalidJavaBeanGetter() throws IOException, ServletException {
    // Testing that invalid JavaBean getter cause the whole thing to fail
    RequestSimulator simulator = new RequestSimulator(context, new TestModule());
    try {
      simulator.test("/invalid-java-bean-getter").
        get();
      fail("Should have thrown an exception");
    } catch (BeanExpressionException e) {
      // Expected
    }
  }

  @Test
  public void scopeStorage() throws IOException, ServletException {
    // Tests that the expression evaluator safely gets skipped while looking for values and Prime then checks the
    // HttpServletRequest and finds the value
    RequestSimulator simulator = new RequestSimulator(context, new TestModule());
    simulator.test("/scope-storage").
      post();

    assertNotNull(simulator.session.getAttribute("sessionObject"));
  }

  @Test
  public void expressionEvaluatorSkippedUsesRequest() throws IOException, ServletException {
    // Tests that the expression evaluator safely gets skipped while looking for values and Prime then checks the
    // HttpServletRequest and finds the value
    RequestSimulator simulator = new RequestSimulator(context, new TestModule());
    simulator.test("/value-in-request").
      get();

    assertEquals(simulator.response.getOutputStream().toString(), "baz");
    assertEquals(simulator.request.getAttribute("bar"), "baz");
  }

  @Test
  public void actionlessRequest() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator(context, new TestModule());
    simulator.test("/actionless").
      get();

    assertEquals(simulator.response.getOutputStream().toString(), "Hello Actionless World");
  }
}
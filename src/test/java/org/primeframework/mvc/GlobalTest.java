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
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.container.ContainerResolver;
import org.primeframework.mvc.guice.MVCModule;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.test.RequestSimulator;
import org.primeframework.mvc.util.URIBuilder;
import org.testng.annotations.Test;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import freemarker.template.Configuration;
import static org.testng.Assert.*;

/**
 * This class tests the MVC from a high level perspective.
 *
 * @author Brian Pontarelli
 */
public class GlobalTest extends PrimeBaseTest {
  @Test
  public void renderFTL() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });
    simulator.test("/user/edit").get();
    String result = simulator.response.getOutputStream().toString();
    assertEquals(FileUtils.readFileToString(new File("src/test/java/org/primeframework/mvc/edit-output.txt")), result);
  }

  @Test
  public void nonFormFields() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });
    simulator.test("/user/details-fields").get();
    assertEquals(FileUtils.readFileToString(new File("src/test/java/org/primeframework/mvc/details-fields-output.txt")),
      simulator.response.getOutputStream().toString());
  }

  @Test
  public void postRender() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });
    simulator.test("/post").post();
    String result = simulator.response.getOutputStream().toString();
    assertTrue(result.contains("Brian Pontarelli"));
    assertTrue(result.contains("35"));
    assertTrue(result.contains("Broomfield"));
    assertTrue(result.contains("CO"));
  }

  @Test
  public void scopeStorage() throws IOException, ServletException {
    // Tests that the expression evaluator safely gets skipped while looking for values and Prime then checks the
    // HttpServletRequest and finds the value
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });
    simulator.test("/scope-storage").
      post();

    assertNotNull(simulator.session.getAttribute("sessionObject"));
  }

  @Test
  public void expressionEvaluatorSkippedUsesRequest() throws IOException, ServletException {
    // Tests that the expression evaluator safely gets skipped while looking for values and Prime then checks the
    // HttpServletRequest and finds the value
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });
    simulator.test("/value-in-request").
      get();

    assertEquals(simulator.response.getOutputStream().toString(), "baz");
    assertEquals(simulator.request.getAttribute("bar"), "baz");
  }

  @Test
  public void actionlessRequest() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });
    simulator.test("/actionless").
      get();

    assertEquals(simulator.response.getOutputStream().toString(), "Hello Actionless World");
  }

  @Test
  public void singletons() throws IOException, ServletException {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });
    assertSingleton(simulator, ActionConfigurationProvider.class);
    assertSingleton(simulator, Configuration.class);
    assertSingleton(simulator, ResourceBundle.Control.class);
    assertSingleton(simulator, ResourceBundle.Control.class);
    assertSingleton(simulator, Validator.class);
    assertSingleton(simulator, ContainerResolver.class);
    assertSingleton(simulator, ConverterProvider.class);
    assertSingleton(simulator, ExpressionEvaluator.class);
    assertSingleton(simulator, URIBuilder.class);
    assertSingletonConverter(simulator, Boolean.class);
    assertSingletonConverter(simulator, boolean.class);
    assertSingletonConverter(simulator, Character.class);
    assertSingletonConverter(simulator, char.class);
    assertSingletonConverter(simulator, Number.class);
    assertSingletonConverter(simulator, int.class);
    assertSingletonConverter(simulator, long.class);
    assertSingletonConverter(simulator, double.class);
    assertSingletonConverter(simulator, float.class);
    assertSingletonConverter(simulator, BigDecimal.class);
    assertSingletonConverter(simulator, BigInteger.class);
    assertSingletonConverter(simulator, Collection.class);
    assertSingletonConverter(simulator, DateTime.class);
    assertSingletonConverter(simulator, Enum.class);
    assertSingletonConverter(simulator, File.class);
    assertSingletonConverter(simulator, LocalDate.class);
    assertSingletonConverter(simulator, Locale.class);
    assertSingletonConverter(simulator, String.class);
  }

  private void assertSingleton(RequestSimulator simulator, Class<?> type) {
    assertSame(simulator.injector.getInstance(type), simulator.injector.getInstance(type));
  }

  private void assertSingletonConverter(RequestSimulator simulator, Class<?> type) {
    Map<Class<?>, GlobalConverter> converters = simulator.injector.getInstance(Key.get(new TypeLiteral<Map<Class<?>, GlobalConverter>>(){}));
    assertSame(converters.get(type), converters.get(type));
  }
}
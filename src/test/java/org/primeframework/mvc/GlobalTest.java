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

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.container.ContainerResolver;
import org.primeframework.mvc.guice.MVCModule;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.MissingPropertyExpressionException;
import org.primeframework.mvc.test.RequestResult;
import org.primeframework.mvc.test.RequestSimulator;
import org.primeframework.mvc.util.URIBuilder;
import org.testng.annotations.Test;

import javax.validation.Validator;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.testng.Assert.*;

/**
 * This class tests the MVC from a high level perspective.
 *
 * @author Brian Pontarelli
 */
public class GlobalTest extends PrimeBaseTest {
  @Test
  public void actionlessRequest() throws Exception {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });

    RequestResult result = simulator.test("/actionless").get();
    assertEquals(result.response.getOutputStream().toString(), "Hello Actionless World");
  }

  @Test
  public void apiJSONBothWays() throws Exception {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });

    String json = "{" +
        "  \"active\":true," +
        "  \"addresses\":{" +
        "    \"home\":{" +
        "      \"city\":\"Broomfield\"," +
        "      \"state\":\"Colorado\"," +
        "      \"zipcode\":\"80023\"" +
        "    }," +
        "    \"work\":{" +
        "      \"city\":\"Denver\"," +
        "      \"state\":\"Colorado\"," +
        "      \"zipcode\":\"80202\"" +
        "    }" +
        "  }," +
        "  \"age\":37," +
        "  \"favoriteMonth\":5," +
        "  \"favoriteYear\":1976," +
        "  \"ids\":{" +
        "    \"0\":1," +
        "    \"1\":2" +
        "  }," +
        "  \"lifeStory\":\"Hello world\"," +
        "  \"locale\":\"en_US\"," +
        "  \"securityQuestions\":[\"one\",\"two\",\"three\",\"four\"]," +
        "  \"siblings\":[" +
        "    {" +
        "      \"active\":false," +
        "      \"name\":\"Brett\"" +
        "    }," +
        "    {" +
        "      \"active\":false," +
        "      \"name\":\"Beth\"" +
        "    }" +
        "  ]," +
        "  \"type\":\"COOL\"" +
        "}";

    RequestResult result = simulator.test("/api")
        .withContentType("application/json")
        .withBody(json.getBytes())
        .post();

    assertEquals(result.body, json.replace("  ", ""));
  }

  @Test
  public void developmentExceptions() throws Exception {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });

    // Bad annotation @Action("{id}") it should be @Action("{uuid}")
    try {
      simulator.test("/invalid-api/42").get();
      fail("Should have thrown");
    } catch (MissingPropertyExpressionException e) {
      // Should throw
    }

    // Bad parameter (i.e. /invalid-api?bad-param=42
    try {
      simulator.test("/invalid-api").withParameter("bad-param", "42").get();
      fail("Should have thrown");
    } catch (MissingPropertyExpressionException e) {
      // Should throw
    }
  }

  @Test
  public void expressionEvaluatorSkippedUsesRequest() throws Exception {
    // Tests that the expression evaluator safely gets skipped while looking for values and Prime then checks the
    // HttpServletRequest and finds the value
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });

    RequestResult result = simulator.test("/value-in-request").get();
    assertEquals(result.body, "baz");
    assertEquals(result.request.getAttribute("bar"), "baz");
  }

  @Test
  public void fullFormWithAllAttributes() throws Exception {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });

    RequestResult result = simulator.test("/user/full-form").get();
    assertEquals(result.body, FileUtils.readFileToString(new File("src/test/java/org/primeframework/mvc/full-form-output.txt")));
  }

  @Test
  public void nonFormFields() throws Exception {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });

    RequestResult result = simulator.test("/user/details-fields").get();
    assertEquals(result.body, FileUtils.readFileToString(new File("src/test/java/org/primeframework/mvc/details-fields-output.txt")));
  }

  @Test
  public void postRender() throws Exception {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });

    RequestResult result = simulator.test("/post").post();
    String html = result.response.getOutputStream().toString();
    assertTrue(html.contains("Brian Pontarelli"));
    assertTrue(html.contains("35"));
    assertTrue(html.contains("Broomfield"));
    assertTrue(html.contains("CO"));
  }

  @Test
  public void renderFTL() throws Exception {
    RequestSimulator simulator = new RequestSimulator(context, new MVCModule() {
      @Override
      protected void configure() {
        super.configure();
        install(new TestModule());
      }
    });

    RequestResult result = simulator.test("/user/edit").get();
    assertEquals(result.body, FileUtils.readFileToString(new File("src/test/java/org/primeframework/mvc/edit-output.txt")));
  }

  @Test
  public void scopeStorage() throws Exception {
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
  public void singletons() throws Exception {
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
    Map<Class<?>, GlobalConverter> converters = simulator.injector.getInstance(Key.get(new TypeLiteral<Map<Class<?>, GlobalConverter>>() {
    }));
    assertSame(converters.get(type), converters.get(type));
  }
}
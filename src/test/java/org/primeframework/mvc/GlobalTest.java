/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.example.domain.UserField;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.container.ContainerResolver;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.MissingPropertyExpressionException;
import org.primeframework.mvc.test.RequestSimulator;
import org.primeframework.mvc.util.URIBuilder;
import org.testng.annotations.Test;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import freemarker.template.Configuration;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

/**
 * This class tests the MVC from a high level perspective.
 *
 * @author Brian Pontarelli
 */
public class GlobalTest extends PrimeBaseTest {

  @Test
  public void get() throws Exception {
    simulator.test("/user/edit")
             .get()
             .assertStatusCode(200)
             .assertBodyFile(Paths.get("src/test/resources/html/edit.html"));
  }

  @Test
  public void get_JSONView() throws Exception {
    simulator.test("/views/entry/api")
             .get()
             .assertStatusCode(200)
             .assertJSONFile(Paths.get("src/test/resources/json/views/entry/entry-api.json"));

    simulator.test("/views/entry/export")
             .get()
             .assertStatusCode(200)
             .assertJSONFile(Paths.get("src/test/resources/json/views/entry/entry-export.json"));
  }

  @Test
  public void get_developmentExceptions() throws Exception {
    // Bad annotation @Action("{id}") it should be @Action("{uuid}")
    simulator.test("/invalid-api/42")
             .expectException(MissingPropertyExpressionException.class)
             .get()
             .assertStatusCode(500);

    // Bad parameter (i.e. /invalid-api?bad-param=42
    simulator.test("/invalid-api")
             .withParameter("bad-param", "42")
             .expectException(MissingPropertyExpressionException.class)
             .get()
             .assertStatusCode(500);
  }

  @Test
  public void get_expressionEvaluatorSkippedUsesRequest() throws Exception {
    // Tests that the expression evaluator safely gets skipped while looking for values and Prime then checks the
    // HttpServletRequest and finds the value
    simulator.test("/value-in-request")
             .get()
             .assertBodyContains("baz")
             .assertRequestContainsAttribute("bar", "baz");
  }

  @Test
  public void get_fullFormWithAllAttributes() throws Exception {
    simulator.test("/user/full-form")
             .get()
             .assertBodyFile(Paths.get("src/test/resources/html/full-form.html"));
  }

  @Test
  public void get_jwtAuthorized() throws Exception {
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withParameter("authorized", true)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .get()
                                 .assertStatusCode(200));
  }

  @Test
  public void get_jwtDisabledJwtAuthentication() throws Exception {
    // Send in a JWT Authorization header when the Action has JWT disabled. Should always get a 401. When a JWT is provided, the action expects JWT to be enabled.
    test.simulate(() -> simulator.test("/jwt-authorized-disabled")
                                 .withParameter("authorized", true)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_jwtExpired() throws Exception {
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withParameter("authorized", true)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0NDUxMDA3MzF9.K18gIegEBfxgj8rU4D2WDh3CzEmRUmy8qBS7SWAcG9w")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_jwtInvalidSignature() throws Exception {
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withParameter("authorized", true)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.aaabbbcccddd")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_jwtMissingAuthorizeHeader() throws Exception {
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withParameter("authorized", true)
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_jwtNotAuthorized() throws Exception {
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withParameter("authorized", false)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_jwtNotBefore() throws Exception {
    // Validating the JWT registered claim 'nbf' (Not Before). The JWT is validly signed, but it is instructed not to be valid before some point in the future. Expecting a 401.
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withParameter("authorized", true)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYmYiOjQ2MzIzOTY2NjV9.mRvvyJXvDD8RQ_PM1TadZdZNYXRa9CjOx62Tk866538")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_metrics() throws Exception {
    simulator.test("/user/full-form")
             .get()
             .assertBodyFile(Paths.get("src/test/resources/html/full-form.html"));

    Map<String, Timer> timers = metricRegistry.getTimers();
    assertEquals(timers.get("prime-mvc.[/user/full-form].requests").getCount(), 1);
  }

  @Test
  public void get_metricsErrors() throws Exception {
    simulator.test("/execute-method-throws-exception")
             .expectException(IllegalArgumentException.class)
             .get()
             .assertStatusCode(500);

    Map<String, Timer> timers = metricRegistry.getTimers();
    assertEquals(timers.get("prime-mvc.[/execute-method-throws-exception].requests").getCount(), 1);

    Map<String, Meter> meters = metricRegistry.getMeters();
    assertEquals(meters.get("prime-mvc.[/execute-method-throws-exception].errors").getCount(), 1);
  }

  @Test
  public void get_nonFormFields() throws Exception {
    simulator.test("/user/details-fields")
             .get()
             .assertBodyFile(Paths.get("src/test/resources/html/details-fields.html"));
  }

  @Test
  public void head() throws Exception {
    simulator.test("/head")
             .head()
             .assertStatusCode(200)
             .assertBodyIsEmpty();
  }

  @Test
  public void head_jwtAuthorized() throws Exception {
    // This test will pass if we call the JWT authorize method or not....
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withParameter("authorized", true)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .head()
                                 .assertStatusCode(200));
  }

  @Test
  public void head_jwtNotAuthorized() throws Exception {
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withParameter("authorized", false)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .head()
                                 .assertStatusCode(401));
  }

  @Test
  public void notImplemented() throws Exception {
    simulator.test("/not-implemented")
             .get()
             .assertStatusCode(501);

    simulator.test("/not-implemented")
             .post()
             .assertStatusCode(501);

    simulator.test("/not-implemented")
             .put()
             .assertStatusCode(501);

    simulator.test("/not-implemented")
             .delete()
             .assertStatusCode(501);

    // head is implemented
    simulator.test("/not-implemented")
             .head()
             .assertStatusCode(200);
  }

  @Test
  public void post() throws Exception {
    simulator.test("/post")
             .post()
             .assertStatusCode(200)
             .assertBodyContains("Brian Pontarelli", "35", "Broomfield", "CO");
  }

  @Test
  public void multipleJSONRequestMembers() throws Exception {
    simulator.test("/multiple-json-request")
             .post()
             .assertStatusCode(200);

    simulator.test("/multiple-json-request")
             .withJSON(new Object())
             .post()
             .assertStatusCode(201);

    simulator.test("/multiple-json-request")
             .withJSON(new Object())
             .delete()
             .assertStatusCode(202);

  }

  @Test
  public void post_JSONWithActual() throws Exception {
    Path json = Paths.get("src/test/resources/json/api-jsonWithActual-post.json");
    simulator.test("/api")
             .withJSONFile(json)
             .post()
             .assertJSONFileWithActual(UserField.class, Paths.get("src/test/resources/json/api-jsonWithActual-post-response.json"));
  }

  @Test
  public void post_anyContentType() throws Exception {
    test.createFile("Hello World")
        .simulate(() -> simulator.test("/file-upload")
                                 .withFile("dataAnyType", test.tempFile.toFile(), "text/plain")
                                 .post()
                                 .assertStatusCode(200))
        .simulate(() -> simulator.test("/file-upload")
                                 .withFile("dataAnyType", test.tempFile.toFile(), "text/html")
                                 .post()
                                 .assertStatusCode(200))
        .simulate(() -> simulator.test("/file-upload")
                                 .withFile("dataAnyType", test.tempFile.toFile(), "application/octet-stream")
                                 .post()
                                 .assertStatusCode(200));
  }

  @Test
  public void post_apiJSONBothWays() throws Exception {
    Path json = Paths.get("src/test/resources/json/api-jsonBothWays-post.json");
    simulator.test("/api")
             .withJSONFile(json)
             .post()
             .assertJSONFile(json);
  }

  @Test
  public void post_binary() throws Exception {
    test.simulate(() -> simulator.test("/binary")
                                 .withParameter("expected", "Hello World")
                                 .withBody("Hello World")
                                 .withContentType("application/octet-stream")
                                 .post()
                                 .assertStatusCode(200));
  }

  @Test
  public void post_onlyAllowTextHTML() throws Exception {
    test.createFile("<strong>Hello World</strong>")
        .simulate(() -> simulator.test("/file-upload")
                                 .withFile("dataTextHtml", test.tempFile.toFile(), "text/plain")
                                 .post()
                                 .assertStatusCode(400))
        .simulate(() -> simulator.test("/file-upload")
                                 .withFile("dataTextHtml", test.tempFile.toFile(), "text/html")
                                 .post()
                                 .assertStatusCode(200));
  }

  @Test
  public void post_scopeStorage() throws Exception {
    // Tests that the expression evaluator safely gets skipped while looking for values and Prime then checks the
    // HttpServletRequest and finds the value
    simulator.test("/scope-storage")
             .post();

    assertNotNull(simulator.session.getAttribute("sessionObject"));
  }

  @Test
  public void singletons() throws Exception {
    assertSingleton(simulator, ActionConfigurationProvider.class);
    assertSingleton(simulator, Configuration.class);
    assertSingleton(simulator, ResourceBundle.Control.class);
    assertSingleton(simulator, ResourceBundle.Control.class);
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
    assertSingletonConverter(simulator, ZonedDateTime.class);
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
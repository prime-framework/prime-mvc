/*
 * Copyright (c) 2001-2020, Inversoft Inc., All Rights Reserved
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
import java.util.UUID;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import freemarker.template.Configuration;
import org.example.action.JwtAuthorizedAction;
import org.example.domain.UserField;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.container.ContainerResolver;
import org.primeframework.mvc.freemarker.FreeMarkerRenderException;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.MissingPropertyExpressionException;
import org.primeframework.mvc.test.RequestSimulator;
import org.primeframework.mvc.util.URIBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * This class tests the MVC from a high level perspective.
 *
 * @author Brian Pontarelli
 */
public class GlobalTest extends PrimeBaseTest {
  private Path jsonDir;

  @BeforeClass
  public void beforeClass() {
    jsonDir = Paths.get("src/test/resources/json");
  }

  @Test
  public void embeddedFormHandling() throws Exception {
    // Ensure this 'required' parameter for PageOne does not mess up PageTwo which does not have an Id field.
    test.simulate(() -> simulator.test("/scope/page-one")
                                 .withURLSegment("IdOnlyForPageOne")
                                 .get()
                                 .assertStatusCode(200));

    // Ensure the @FileUpload in the PageOneAction does not mess up PageTwo
    test.createFile()
        .simulate(() -> simulator.test("/scope/page-one")
                                 .withFile("file", test.tempFile.toFile(), "text/plain")
                                 .get()
                                 .assertStatusCode(200));
  }

  @Test
  public void escapePathSegmentsWithWildCard() throws Exception {
    test.simulate(() -> test.simulator.test("/escaped-path-segments")
                                      .withURLSegment("foo bar")
                                      .withURLSegment("foobar")
                                      .withURLSegment("foo bar")
                                      .withURLSegment("foo@bar")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains(
                                          "Success!",
                                          "parm=foo bar",
                                          "theRest=foobar,foo bar,foo@bar"));

    test.simulate(() -> test.simulator.test("/escaped-path-segments")
                                      .withURLSegment("<foo>")
                                      .withURLSegment("foo bar")
                                      .withURLSegment("foobar")
                                      .withURLSegment("foo bar")
                                      .withURLSegment("foo@bar")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains(
                                          "Success!",
                                          "parm=&lt;foo&gt;",
                                          "theRest=foo bar,foobar,foo bar,foo@bar"));
  }

  @Test
  public void get() throws Exception {
    simulator.test("/user/edit")
             .get()
             .setup(r -> r.container.getResponse().setHeader("Referer", "http://localhost"))
             .assertStatusCode(200)
             // header name is not case sensitive
             .assertHeaderContains("referer", "http://localhost")
             .assertHeaderContains("Referer", "http://localhost")
             .assertHeaderDoesNotContain("Potato")
             .assertBodyFile(Paths.get("src/test/resources/html/edit.html"));
  }

  @Test
  public void get_JSONView() throws Exception {
    test.simulate(() -> simulator.test("/views/entry/api")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSONFile(jsonDir.resolve("views/entry/entry-api.json")));

    test.simulate(() -> simulator.test("/views/entry/export")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSONFile(jsonDir.resolve("views/entry/entry-export.json")));

    // Serialize an object using @JSONResponse when no view is specified for an object that has only annotated fields
    // The DEFAULT_VIEW_INCLUSION is the default value, but explicitly configured in case the default prime configuration changes
    test.configureObjectMapper(om -> objectMapper.enable(MapperFeature.DEFAULT_VIEW_INCLUSION))
        .simulate(() -> simulator.test("/views/entry/no-view-defined")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSONFile(jsonDir.resolve("views/entry/entry-no-view-defined.json")));

    // Default view inclusion is enabled and if we serialize a @JSONResponse with a view that has no fields in the object - empty response
    test.simulate(() -> simulator.test("/views/entry/wrong-view-defined")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSON("{}"));

    // Ensure we get a response even when we disable the default view inclusion if we do not specify a view
    test.configureObjectMapper(om -> objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION))
        .simulate(() -> simulator.test("/views/entry/no-view-defined")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSONFile(jsonDir.resolve("views/entry/entry-no-view-defined.json")));

    // Default view inclusion is disabled and if we serialize a @JSONResponse with a view that has no fields in the object - empty response.
    test.simulate(() -> simulator.test("/views/entry/wrong-view-defined")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSON("{}"));
  }

  @Test
  public void get_action_package_collision() throws Exception {
    test.simulate(() -> test.simulator.test("/foo/view/bar/baz")
                                      .withURLSegment("42")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view/bar/baz!", "42"))

        .simulate(() -> test.simulator.test("/foo/view/bar/baz")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view/bar/baz!", "empty"))

        .simulate(() -> test.simulator.test("/foo/view/bar")
                                      .withURLSegment("42")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view/bar!", "42"))

        .simulate(() -> test.simulator.test("/foo/view/bar")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view/bar!", "empty"))

        .simulate(() -> test.simulator.test("/foo/view")
                                      .withURLSegment("42")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view!", "42"))

        .simulate(() -> test.simulator.test("/foo/view")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view!", "empty"))

        .simulate(() -> test.simulator.test("/foo")
                                      .withURLSegment("42")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo!", "42"))

        .simulate(() -> test.simulator.test("/foo")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo!", "empty"));
  }

  @Test
  public void get_collectionConverter() throws Exception {
    // Both of these will fail because the action has a List<String> as the backing values for this form, and the input field is a text field.
    test.expectException(ConverterStateException.class,
        () -> test.simulate(() -> simulator.test("/collection-converter")
                                           .withParameter("string", "foo,bar,baz")
                                           .get()));

    test.expectException(ConverterStateException.class,
        () -> simulator.test("/collection-converter")
                       .withParameter("string", "bar")
                       .withParameter("string", "baz")
                       .get());

    // It will work if we use a backing collection with an iterator in the form to build multiple form fields
    test.simulate(() -> simulator.test("/collection-converter")
                                 .withParameter("strings", "bar")
                                 .withParameter("strings", "baz")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyDoesNotContain("__empty2__', '__empty3__")
                                 .assertBodyContains("__empty1__")
                                 .assertBodyContains("[bar, baz]")
                                 .assertBodyContains("<input type=\"text\" id=\"string\" name=\"string\"/>")
                                 .assertBodyContains("<input type=\"text\" id=\"strings\" name=\"strings\" value=\"bar\"/")
                                 .assertBodyContains("<input type=\"text\" id=\"strings\" name=\"strings\" value=\"baz\"/"));

    // Single string containing commas, output contains the same string
    test.simulate(() -> simulator.test("/collection-converter")
                                 .withParameter("strings", "foo,bar,baz")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyDoesNotContain("__empty2__', '__empty3__")
                                 .assertBodyContains("__empty1__")
                                 .assertBodyContains("[foo,bar,baz]")
                                 .assertBodyContains("<input type=\"text\" id=\"string\" name=\"string\"/>")
                                 .assertBodyContains("<input type=\"text\" id=\"strings\" name=\"strings\" value=\"foo,bar,baz\"/"));
  }

  @Test
  public void get_developmentExceptions() {
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
  public void get_execute_redirect() throws Exception {
    // Follow the redirect and another redirect and assert on that response as well - and ensure a message set in the first redirect gets all the way to the end
    test.simulate(() -> simulator.test("/temp-redirect")
                                 .get()
                                 .assertStatusCode(302)
                                 // Messages are in the store
                                 .assertContainsGeneralMessageCodes(MessageType.ERROR, "[ERROR]")
                                 .assertContainsGeneralMessageCodes(MessageType.INFO, "[INFO]")
                                 .assertContainsGeneralMessageCodes(MessageType.WARNING, "[WARNING]")
                                 .assertRedirect("/temp-redirect-target")
                                 .executeRedirect(response -> response.assertStatusCode(302)
                                                                      // Message is still in the store
                                                                      .assertContainsGeneralMessageCodes(MessageType.ERROR, "[ERROR]")
                                                                      .assertContainsGeneralMessageCodes(MessageType.INFO, "[INFO]")
                                                                      .assertContainsGeneralMessageCodes(MessageType.WARNING, "[WARNING]")
                                                                      .assertRedirect("/temp-redirect-target-target")
                                                                      .executeRedirect(subResponse -> subResponse.assertStatusCode(200)
                                                                                                                 .assertBodyContains("Look Ma, I'm redirected.")
                                                                                                                 // Message is still in the store and also rendered on the page
                                                                                                                 .assertContainsGeneralMessageCodes(MessageType.ERROR, "[ERROR]")
                                                                                                                 .assertContainsGeneralMessageCodes(MessageType.INFO, "[INFO]")
                                                                                                                 .assertContainsGeneralMessageCodes(MessageType.WARNING, "[WARNING]")
                                                                                                                 .assertBodyContainsMessagesFromKey("[ERROR]", "[INFO]", "[WARNING]")
                                                                                                                 .assertBodyContains("Error 3", "Info 3", "Warning 3")

                                                                                                                 // Execute the form POST in the response body
                                                                                                                 .executeFormPostInResponseBody("form", formResponse ->
                                                                                                                     formResponse.assertStatusCode(200)
                                                                                                                                 .assertBodyContains(
                                                                                                                                     "textValue",
                                                                                                                                     "disabledEmpty", // This will be missing so the 'Empty' value will be rendered
                                                                                                                                     "hiddenValue",
                                                                                                                                     "radioValue2",
                                                                                                                                     "checkboxValue2",
                                                                                                                                     "textareaValue")
                                                                                                                                 .assertBodyDoesNotContain(
                                                                                                                                     "disabledValue",
                                                                                                                                     "radioValue1",
                                                                                                                                     "checkboxValue1")))));
  }

  @Test
  public void get_execute_relativeRedirect() throws Exception {
    // Follow the redirect and another redirect and assert on that response as well - and ensure a message set in the first redirect gets all the way to the end
    test.simulate(() -> simulator.test("/temp-relative-redirect")
                                 .get()
                                 .assertStatusCode(302)
                                 // Messages are in the store
                                 .assertContainsGeneralMessageCodes(MessageType.ERROR, "[ERROR]")
                                 .assertContainsGeneralMessageCodes(MessageType.INFO, "[INFO]")
                                 .assertContainsGeneralMessageCodes(MessageType.WARNING, "[WARNING]")
                                 .assertRedirect("temp-redirect-target")
                                 .executeRedirect(response -> response.assertStatusCode(302)
                                                                      // Message is still in the store
                                                                      .assertContainsGeneralMessageCodes(MessageType.ERROR, "[ERROR]")
                                                                      .assertContainsGeneralMessageCodes(MessageType.INFO, "[INFO]")
                                                                      .assertContainsGeneralMessageCodes(MessageType.WARNING, "[WARNING]")
                                                                      .assertRedirect("/temp-redirect-target-target")
                                                                      .executeRedirect(subResponse -> subResponse.assertStatusCode(200)
                                                                                                                 .assertBodyContains("Look Ma, I'm redirected.")
                                                                                                                 // Message is still in the store and also rendered on the page
                                                                                                                 .assertContainsGeneralMessageCodes(MessageType.ERROR, "[ERROR]")
                                                                                                                 .assertContainsGeneralMessageCodes(MessageType.INFO, "[INFO]")
                                                                                                                 .assertContainsGeneralMessageCodes(MessageType.WARNING, "[WARNING]")
                                                                                                                 .assertBodyContainsMessagesFromKey("[ERROR]", "[INFO]", "[WARNING]")
                                                                                                                 .assertBodyContains("Error 3", "Info 3", "Warning 3")

                                                                                                                 // Execute the form POST in the response body
                                                                                                                 .executeFormPostInResponseBody("form", formResponse ->
                                                                                                                     formResponse.assertStatusCode(200)
                                                                                                                                 .assertBodyContains(
                                                                                                                                     "textValue",
                                                                                                                                     "disabledEmpty", // This will be missing so the 'Empty' value will be rendered
                                                                                                                                     "hiddenValue",
                                                                                                                                     "radioValue",
                                                                                                                                     "checkboxValue",
                                                                                                                                     "textareaValue")
                                                                                                                                 .assertBodyDoesNotContain("disabledValue")))));
  }

  @Test
  public void get_expressionEvaluatorSkippedUsesRequest() {
    // Tests that the expression evaluator safely gets skipped while looking for values and Prime then checks the
    // HttpServletRequest and finds the value
    simulator.test("/value-in-request")
             .get()
             .assertBodyContains("baz")
             .assertRequestContainsAttribute("bar", "baz");
  }

  @Test
  public void get_freemarker_double_escape() {
    simulator.test("/freemarker/double-escape")
             .expectException(FreeMarkerRenderException.class)
             .get();
  }

  @Test(dataProvider = "get_freemarker_escape_parameters")
  public void get_freemarker_escape(String mode, boolean shouldBeEscaped) {
    if (shouldBeEscaped) {
      // Test from user data
      simulator.test("/freemarker/escape")
               .withParameter("mode", mode)
               .get()
               .assertStatusCode(200)
               .assertBodyContains("Output format: HTML",
                   "Auto-escaping: true",
                   "Select\u2026",
                   ",\u0020",
                   "&lt;p&gt;Are you sure?&lt;/p&gt;",
                   "Hello, to access your account go to &lt;a href=&quot;https://foo.com&quot;&gt;foo.com&lt;/a&gt;.",
                   "Dismiss",
                   "Ignore")
               .assertBodyDoesNotContain("<p>Are you sure?</p>",
                   "Hello, to access your account go to <a href=\"https://foo.com\">foo.com</a>.",
                   "freemarker.core.TemplateHTMLOutputModel"); // Check to make sure that we don't toString an output model
    } else {
      simulator.test("/freemarker/escape")
               .withParameter("mode", mode)
               .get()
               .assertStatusCode(200)
               .assertBodyContains("Output format: HTML",
                   "Auto-escaping: true",
                   "Select\u2026",
                   ",\u0020",
                   "<p>Are you sure?</p>",
                   "Hello, to access your account go to <a href=\"https://foo.com\">foo.com</a>.",
                   "Dismiss",
                   "Ignore")
               .assertBodyDoesNotContain("&lt;p&gt;Are you sure?&lt;/p&gt;",
                   "Hello, to access your account go to &lt;a href=&quot;https://foo.com&quot;&gt;foo.com&lt;/a&gt;.",
                   "freemarker.core.TemplateHTMLOutputModel"); // Check to make sure that we don't toString an output model
    }
  }

  @DataProvider
  public Object[][] get_freemarker_escape_parameters() {
    return new Object[][]{
        {"message", false}, // We explicitly set the control to not escape anything
        {"function", true}, // This is a direct function call
        {"functionUnescaped", false}, // Direct function call wrapped in noautoesc
        {"directProperties", true}, // This is a direct property access
        {"indirectProperties", true} // This is an indirect property access using ?eval
    };
  }

  @Test
  public void get_fullFormWithAllAttributes() throws Exception {
    simulator.test("/user/full-form")
             .get()
             .assertBodyFile(Paths.get("src/test/resources/html/full-form.html"));
  }

  @Test
  public void get_index() throws Exception {
    test.simulate(() -> simulator.test("/user/")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Yeah!"));
    test.simulate(() -> simulator.test("/user")
                                 .get()
                                 .assertStatusCode(301)
                                 .assertRedirect("/user/"));
  }

  @Test
  public void get_jwtAuthorized() throws Exception {
    JwtAuthorizedAction.authorized = true;

    // Test with JWT scheme
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .get()
                                 .assertStatusCode(200));

    // Test with Bearer scheme
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
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

    // Same, use Bearer scheme
    test.simulate(() -> simulator.test("/jwt-authorized-disabled")
                                 .withParameter("authorized", true)
                                 .withHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
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
    JwtAuthorizedAction.authorized = true;
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_jwtNotAuthorized() throws Exception {
    JwtAuthorizedAction.authorized = false;
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_jwtNotBefore() throws Exception {
    // Validating the JWT registered claim 'nbf' (Not Before). The JWT is validly signed, but it is instructed not to be valid before some point in the future. Expecting a 401.
    JwtAuthorizedAction.authorized = true;
    test.simulate(() -> simulator.test("/jwt-authorized")
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
  public void get_metricsErrors() {
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
  public void get_nested_parameters() throws Exception {
    test.simulate(() -> test.simulator.test("/nested")
                                      .withURLSegment("42")
                                      .withURLSegment("99")
                                      .withURLSegment("parameter")
                                      .withURLSegment("foo")
                                      .withURLSegment("bar")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("Success!", "preParam1=42", "preParam2=99", "endParam1=foo", "endParam2=bar"));
  }

  @Test
  public void get_nonFormFields() throws Exception {
    simulator.test("/user/details-fields")
             .get()
             .assertBodyFile(Paths.get("src/test/resources/html/details-fields.html"));
  }

  @Test
  public void get_objectMapValues() throws Exception {
    // Testing ?eval against a generic map
    test.simulate(() -> simulator.test("/object-map-values")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains(
                                     "Name:foo.data.preferences.coffee.style",
                                     ":Value::Value:"));
  }

  @Test
  public void get_onlyKnownParameters() {
    // Action w/out @UnknownParameters
    configuration.allowUnknownParameters = false;
    test.expectException(MissingPropertyExpressionException.class, () ->
        simulator.test("/only-known-parameters")
                 .withParameter("foo", "bar")
                 .withParameter("foo", "baz")
                 .withParameter("foo.bar", "baz")
                 .withParameter("foo/0/bar/bam", "purple")
                 .post()
                 .assertStatusCode(200)
                 .assertBodyContains(
                     "foo => [bar,baz]",
                     "foo.bar => [baz]",
                     "foo/0/bar/bam => [purple]"
                 ));
  }

  @Test
  public void get_percent_encoded_segment() throws Exception {
    test.simulate(() -> test.simulator.test("/foo/view")
                                      .withURLSegment("<strong>foo</strong>")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view!", "id=&lt;strong&gt;foo&lt;/strong&gt;"));

    test.simulate(() -> test.simulator.test("/foo/view/%3Cstrong%3Efoo%3C%2Fstrong%3E")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view!", "id=&lt;strong&gt;foo&lt;/strong&gt;"));
  }

  @Test
  public void get_postParameterBeforeFormPrepare() throws Exception {
    // Ensure we hit PostParameterMethods in an action when we build a new action based upon hitting a
    // form tag that has a different action then the current action invocation.

    test.simulate(() -> simulator.test("/scope/page-two")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("postParameterMethodCalled:first")
                                 .assertBodyContains("formPrepareMethodCalled:second"))

        // Now hit /api/page-one which contains a form tag with an action of /scope/page-two
        .simulate(() -> simulator.test("/scope/page-one")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("postParameterMethodCalled:first")
                                 .assertBodyContains("formPrepareMethodCalled:second"));
  }

  @Test
  public void get_redirect() throws Exception {
    // Contains no parameters
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withParameter("redirectURI", "/foo")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo"));

    // Contains a single parameter, calling beginQuery is optional
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withParameter("redirectURI", "/foo?bar=baz")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz")
                                 .assertRedirect("/foo", params -> params.with("bar", "baz")));

    // Contains a single parameter with calling beginQuery()
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withParameter("redirectURI", "/foo?bar=baz")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz")
                                 .assertRedirect("/foo", params -> params.beginQuery()
                                                                         .with("bar", "baz")));

    // Contains multiple parameters
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withParameter("redirectURI", "/foo?bar=baz&boom=dynamite")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz&boom=dynamite")
                                 .assertRedirect("/foo", params -> params.beginQuery()
                                                                         .with("bar", "baz")
                                                                         .with("boom", "dynamite")));

    // Contains a single parameter after a fragment
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withParameter("redirectURI", "/foo#bar=baz")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo#bar=baz")
                                 .assertRedirect("/foo", params -> params.beginFragment()
                                                                         .with("bar", "baz")));

    // Contains multiple parameters after a fragment
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withParameter("redirectURI", "/foo#bar=baz&boom=dynamite")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo#bar=baz&boom=dynamite")
                                 .assertRedirect("/foo", params -> params.beginFragment()
                                                                         .with("bar", "baz")
                                                                         .with("boom", "dynamite")));

    // Contains a single parameter and a single parameter after a fragment
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withParameter("redirectURI", "/foo?bar=baz#middle=out")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz#middle=out")
                                 .assertRedirect("/foo", params -> params.beginQuery()
                                                                         .with("bar", "baz")
                                                                         .beginFragment()
                                                                         .with("middle", "out")));

    // Contains multiple parameters and multiple parameters after a fragment
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withParameter("redirectURI", "/foo?bar=baz&boom=dynamite#middle=out&not=hotdog")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz&boom=dynamite#middle=out&not=hotdog")
                                 .assertRedirect("/foo", params -> params.beginQuery()
                                                                         .with("bar", "baz")
                                                                         .with("boom", "dynamite")
                                                                         .beginFragment()
                                                                         .with("middle", "out")
                                                                         .with("not", "hotdog")));

    // URL has multiple parameters
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withParameter("redirectURI", "/foo?bar=baz&q=foo&code=bar")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz&q=foo&code=bar")
                                 .assertRedirect("/foo", params -> params.beginQuery()
                                                                         .with("bar", "baz")
                                                                         .with("q", "foo")
                                                                         .with("code", "bar")));
  }

  @Test
  public void get_redirect_withActual() throws Exception {
    // Contains no parameters
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withParameter("redirectURI", "/foo?bing=bam&instant=" + System.currentTimeMillis())
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo", params -> params.withActual("instant")
                                                                         .with("bing", "bam")));
  }

  @Test
  public void get_secure() throws Exception {
    test.simulate(() -> test.simulator.test("/secure")
                                      .get()
                                      .assertStatusCode(401));
  }

  @Test
  public void get_sessionStorageInFormTag() throws Exception {
    // Ensure we fill out scope storage in an action when we build a new action based upon hitting a
    // form tag that has a different action then the current action invocation.

    // Page2 has a session variable and a form, set it, assert it stays in the session.
    test.simulate(() -> simulator.test("/scope/page-two")
                                 .withParameter("searchText", "42") // @Session
                                 .withParameter("searchType", "meaning") // @ActionSession
                                 .post()
                                 .assertStatusCode(200))

        .simulate(() -> simulator.test("/scope/page-two")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("42", "meaning"))

        // Now hit /api/page-one which contains a form tag with an action of /scope/page-two
        .simulate(() -> simulator.test("/scope/page-one")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("42", "meaning"));
  }

  @Test
  public void get_underscore() {
    simulator.test("/test_underscore")
             .get()
             .assertStatusCode(200);
  }

  @Test
  public void get_unknownParameters() throws Exception {
    configuration.allowUnknownParameters = false;
    test.simulate(() -> simulator.test("/unknown-parameters")
                                 .withParameter("foo", "bar")
                                 .withParameter("foo", "baz")
                                 .withParameter("foo.bar", "baz")
                                 .withParameter("foo/0/bar/bam", "purple")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains(
                                     "foo => [bar,baz]",
                                     "foo.bar => [baz]",
                                     "foo/0/bar/bam => [purple]"
                                 ));
  }

  @Test
  public void get_wellKnownDotPrefixed() throws Exception {
    test.simulate(() -> simulator.test("/.well-known/openid-configuration")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSON(new Object()));

    test.simulate(() -> simulator.test("/.well-known/well-known/openid-configuration")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSON(new Object()));

    test.simulate(() -> simulator.test("/.well-known/well-known/.well-known/openid-configuration")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSON(new Object()));

    test.expectException(UnsupportedOperationException.class,
        () -> test.simulate(() -> simulator.test("/.well-known/.well-known/openid-configuration")
                                           .get()));
  }

  @Test
  public void hacked() {
    // Make sure we don't invoke 'freemarker.template.utility.Execute"
    simulator.test("/hacked")
             .expectException(FreeMarkerRenderException.class)
             .get()
             .assertStatusCode(500)
             .assertBodyContains("Instantiating freemarker.template.utility.Execute is not allowed in the template for security reasons.");
  }

  @Test
  public void head() {
    simulator.test("/head")
             .head()
             .assertStatusCode(200)
             .assertBodyIsEmpty();
  }

  @Test
  public void head_jwtAuthorized() throws Exception {
    // This test will pass if we call the JWT authorize method or not....
    JwtAuthorizedAction.authorized = true;
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .head()
                                 .assertStatusCode(200));
  }

  @Test
  public void head_jwtNotAuthorized() throws Exception {
    JwtAuthorizedAction.authorized = false;
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .head()
                                 .assertStatusCode(401));
  }

  @DataProvider(name = "methodOverrides")
  public Object[][] methodOverrides() {
    return new Object[][]{
        {"X-HTTP-Method-Override"},
        {"x-http-method-override"},
        {"X-Method-Override"},
        {"x-method-override"}
    };
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
  public void notAllowed() {
    simulator.test("/not-allowed")
             .get()
             .assertStatusCode(405);

    simulator.test("/not-allowed")
             .post()
             .assertStatusCode(405);

    simulator.test("/not-allowed")
             .put()
             .assertStatusCode(405);

    simulator.test("/not-allowed")
             .delete()
             .assertStatusCode(405);

    // head is allowed
    simulator.test("/not-allowed")
             .head()
             .assertStatusCode(200);
  }

  @Test
  public void notImplemented() {
    simulator.test("/not-allowed")
             .method("POTATO")
             .assertStatusCode(501);
  }

  @Test(dataProvider = "methodOverrides")
  public void patch_MethodOverride(String overrideHeaderName) throws Exception {
    simulator.test("/patch/test")
             .withJSONFile(Paths.get("src/test/resources/json/patch/test-patch.json"))
             .withHeader(overrideHeaderName, "PATCH")
             .post()
             .assertStatusCode(200)
             .assertJSONFile(jsonDir.resolve("patch/test-response.json"), "config", "patched");
  }

  @Test
  public void patch_testing() throws Exception {
    // POST no big deal
    simulator.test("/patch/test")
             .withJSONFile(Paths.get("src/test/resources/json/patch/test.json"), "config", "post")
             .post()
             .assertStatusCode(200)
             .assertJSONFile(jsonDir.resolve("patch/test-response.json"), "config", "post");

    // PUT no big deal
    simulator.test("/patch/test")
             .withJSONFile(Paths.get("src/test/resources/json/patch/test.json"), "config", "put")
             .put()
             .assertStatusCode(200)
             .assertJSONFile(jsonDir.resolve("patch/test-response.json"), "config", "put");

    // PATCH damn that is cool
    simulator.test("/patch/test")
             .withJSONFile(Paths.get("src/test/resources/json/patch/test-patch.json"))
             .patch()
             .assertStatusCode(200)
             .assertJSONFile(jsonDir.resolve("patch/test-response.json"), "config", "patched");
  }

  @Test
  public void post() {
    simulator.test("/post")
             .post()
             .assertStatusCode(200)
             .assertBodyContains("Brian Pontarelli", "35", "Broomfield", "CO");
  }

  @Test
  public void post_JSONWithActual() throws Exception {
    simulator.test("/api")
             .withJSONFile(Paths.get("src/test/resources/json/api-jsonWithActual-post.json"))
             .post()
             .assertJSONFileWithActual(UserField.class, Paths.get("src/test/resources/json/api-jsonWithActual-post-response.ftl"));

    // Test a final field (the Jackson handler will put the JSON into the final field)
    simulator.test("/api-final")
             .withJSONFile(Paths.get("src/test/resources/json/api-jsonWithActual-post.json"))
             .post()
             .assertJSONFileWithActual(UserField.class, Paths.get("src/test/resources/json/api-jsonWithActual-post-response.ftl"));
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
    Path jsonFile = Paths.get("src/test/resources/json/api-jsonBothWays-post.json");
    simulator.test("/api")
             .withJSONFile(jsonFile)
             .post()
             .assertJSONFile(jsonFile);
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
  public void post_collectionConverter() throws Exception {
    // Single string containing commas, output contains the same string
    test.simulate(() -> simulator.test("/collection-converter")
                                 .withParameter("strings", "foo,bar,baz")
                                 .post()
                                 .assertStatusCode(200));

    // Multiple values, output contains these two values in a collection
    test.simulate(() -> simulator.test("/collection-converter")
                                 .withParameter("strings", "bar")
                                 .withParameter("strings", "baz")
                                 .post()
                                 .assertStatusCode(200));
  }

  @Test
  public void post_couldNotConvert() throws Exception {
    // Could not convert, no specific message for key
    test.simulate(() -> simulator.test("/could-not-convert")
                                 .withParameter("integerMap1['foo']", "bar")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertContainsFieldErrors("integerMap1['foo']")
                                 .assertBodyContainsMessagesFromKeys("[couldNotConvert]integerMap1[]"))

        // Could not convert, specific message for this key
        .simulate(() -> simulator.test("/could-not-convert")
                                 .withParameter("integerMap1['bar']", "baz")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertContainsFieldErrors("integerMap1['bar']")
                                 .assertBodyContainsMessagesFromKeys("[couldNotConvert]integerMap1['bar']"))

        // Could not convert, using generic message from Prime
        .simulate(() -> simulator.test("/could-not-convert")
                                 .withParameter("integerMap2['baz']", "bing")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertContainsFieldErrors("integerMap2['baz']")
                                 .assertBodyContains("Value Baz (java.lang.NumberFormatException: For input string: &quot;bing&quot;"))

        // Unquoted versions

        .simulate(() -> simulator.test("/could-not-convert")
                                 .withParameter("integerMap1[foo2]", "bar")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertContainsFieldErrors("integerMap1[foo2]")
                                 .assertBodyContainsMessagesFromKeys("[couldNotConvert]integerMap1[]"))

        // Could not convert, specific message for this key
        .simulate(() -> simulator.test("/could-not-convert")
                                 .withParameter("integerMap1[bar2]", "baz")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertContainsFieldErrors("integerMap1[bar2]")
                                 .assertBodyContainsMessagesFromKeys("[couldNotConvert]integerMap1[bar2]"))

        // Could not convert, using generic message from Prime
        .simulate(() -> simulator.test("/could-not-convert")
                                 .withParameter("integerMap2[baz2]", "bing")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertContainsFieldErrors("integerMap2[baz2]")
                                 .assertBodyContains("Value Baz (java.lang.NumberFormatException: For input string: &quot;bing&quot;"))

        // Numeric

        // Could not convert, no specific message for this index in a list
        .simulate(() -> simulator.test("/could-not-convert")
                                 .withParameter("integerList1[0]", "bar")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertContainsFieldErrors("integerList1[0]")
                                 .assertBodyContainsMessagesFromKeys("[couldNotConvert]integerList1[]"))

        // Could not convert, specific message for this index in a list
        .simulate(() -> simulator.test("/could-not-convert")
                                 .withParameter("integerList1[1]", "baz")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertContainsFieldErrors("integerList1[1]")
                                 .assertBodyContainsMessagesFromKeys("[couldNotConvert]integerList1[1]"))

        // Could not convert, using generic message from Prime for the list
        .simulate(() -> simulator.test("/could-not-convert")
                                 .withParameter("integerList2[0]", "bing")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertContainsFieldErrors("integerList2[0]")
                                 .assertBodyContains("List 2 - Int 1 (java.lang.NumberFormatException: For input string: &quot;bing&quot;"));
  }

  @Test
  public void post_dateConversion() throws Exception {
    // Multiple LocalDate formats
    test.forEach(
        "01-01-2018",
        "01-01-2018",
        "1-1-2018",
        "1/01/2018",
        "01/1/2018")
        .test(date -> simulator.test("/date-time-converter")
                               .withParameter("localDate", date)
                               .withParameter("localDate@dateTimeFormat", "[MM/dd/yyyy][M/dd/yyyy][M/d/yyyy][MM-dd-yyyy][M-dd-yyyy][M-d-yyyy]")
                               .post()
                               .assertContainsNoFieldMessages()
                               .assertStatusCode(200));

    // Single LocalDate format
    test.simulate(() -> simulator.test("/date-time-converter")
                                 .withParameter("localDate", "01/01/2018")
                                 .withParameter("localDate@dateTimeFormat", "MM/dd/yyyy")
                                 .post()
                                 .assertContainsNoFieldMessages()
                                 .assertStatusCode(200));

    // Multiple ZonedDateTime formats
    test.forEach(
        "07-08-2008 10:13:34 AM -0800",
        "07/08/2008 10:13:34 AM -0800",
        "7-8-2008 10:13:34 AM -0800",
        "7/8/2008 10:13:34 AM -0800")
        .test(zoneDateTime -> simulator.test("/date-time-converter")
                                       .withParameter("zonedDateTime", zoneDateTime)
                                       .withParameter("zonedDateTime@dateTimeFormat", "[MM-dd-yyyy hh:mm:ss a Z][MM/dd/yyyy hh:mm:ss a Z][M/d/yyyy hh:mm:ss a Z][M-d-yyyy hh:mm:ss a Z]")
                                       .post()
                                       .assertContainsNoFieldMessages()
                                       .assertStatusCode(200));

    // Single ZonedDateTime format
    test.simulate(() -> simulator.test("/date-time-converter")
                                 .withParameter("zonedDateTime", "07-08-2008 10:13:34 AM -0800")
                                 .withParameter("zonedDateTime@dateTimeFormat", "MM-dd-yyyy hh:mm:ss a Z")
                                 .post()
                                 .assertContainsNoFieldMessages()
                                 .assertStatusCode(200));
  }

  // Test that the control behaves as expected
  @Test
  public void post_freemarker_escape() throws Exception {
    simulator.test("/freemarker/escape")
             .withParameter("listTest", "none")
             .withParameter("listTest2", "none")
             .post()
             .assertStatusCode(200)
             .assertHTML(html -> html.assertElementExists("input[name=listTest][value=none][checked]")
                                     .assertElementExists("input[name=listTest2][value=none][checked]"));
  }

  @Test
  public void post_generics() throws Exception {
    test.simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "one")
                                 .withParameter("typedObject.mapOfTypes[49e0f299-a2b0-4439-b0d5-3e2cc8949675].one", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Map one = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.mapOfTypes[eee47c8b-4134-4c4d-ab28-cacaeed84cdb].two", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Map two = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.fullyGenericMapOfTypes[eee47c8b-4134-4c4d-ab28-cacaeed84cdb].two", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Map key/value = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.listOfStrings[0]", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("List string = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.listOfTypes[0].two", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("List two = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.singleString", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Single string = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.singleType.two", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Single two = value"))

        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "one")
                                 .withParameter("typedObject.privateMapOfTypes[49e0f299-a2b0-4439-b0d5-3e2cc8949675].one", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Private Map one = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.privateMapOfTypes[eee47c8b-4134-4c4d-ab28-cacaeed84cdb].two", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Private Map two = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.privateFullyGenericMapOfTypes[eee47c8b-4134-4c4d-ab28-cacaeed84cdb].two", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Private Map key/value = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.privateListOfStrings[0]", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Private List string = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.privateListOfTypes[0].two", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Private List two = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.privateSingleString", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Private Single string = value"))
        .simulate(() -> simulator.test("/generics")
                                 .withParameter("type", "two")
                                 .withParameter("typedObject.privateSingleType.two", "value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("Private Single two = value"));
  }

  @Test
  public void post_invalidJSON() throws Exception {
    test.simulate(() -> test.simulator.test("/invalid-json")
                                      .withJSONFile(Paths.get("src/test/resources/json/InvalidJsonAction.json"))
                                      .post()
                                      .assertStatusCode(400)
                                      .assertContainsFieldErrors("active")
                                      .assertContentType("application/json")
                                      .assertJSONFile(jsonDir.resolve("InvalidJsonAction-response.json")));
  }

  @Test
  public void post_objectMapValues() throws Exception {
    // Dot notation, set into typed map of Map<String, Object>
    test.simulate(() -> simulator.test("/object-map-values")
                                 .withParameter("foo.bar.baz", "bing")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertJSONFile(jsonDir.resolve("ObjectMapValues-post-test-1-response.json")));

    // Bracketed notation - functionally the same as dot. This should produce a map.
    test.simulate(() -> simulator.test("/object-map-values")
                                 .withParameter("foo.bar['baz']", "bing")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertJSONFile(jsonDir.resolve("ObjectMapValues-post-test-2-response.json")));

    // Bracketed notation - functionally the same as dot. This should produce a map.
    test.simulate(() -> simulator.test("/object-map-values")
                                 .withParameter("foo.bar['baz']", "bing")
                                 .withParameter("foo.bar['baz']", "boom")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertJSONFile(jsonDir.resolve("ObjectMapValues-post-test-3-response.json")));

    // Bracketed notation using an integer, this is an array.
    test.simulate(() -> simulator.test("/object-map-values")
                                 .withParameter("foo.bar[0]", "bing")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertJSONFile(jsonDir.resolve("ObjectMapValues-post-test-4-response.json")));

    // Bracketed notation using an integer, this is an array.
    test.simulate(() -> simulator.test("/object-map-values")
                                 .withParameter("foo.bar[0]", "bing")
                                 .withParameter("foo.bar[1]", "boom")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertJSONFile(jsonDir.resolve("ObjectMapValues-post-test-5-response.json")));

    // Nested properties
    test.simulate(() -> simulator.test("/object-map-values")
                                 .withParameter("foo.data.preferences.coffee.style", "with cream")
                                 .withParameter("foo.data.preferences.cars.count", 2)
                                 .withParameter("foo.data.preferences.fruit", "oranges")
                                 .withParameter("foo.data.preferences.fruit", "apples")
                                 .withParameter("foo.data.preferences.fruit", "strawberries")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertJSONFile(jsonDir.resolve("ObjectMapValues-post-test-6-response.json")));
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
    test.simulate(() -> test.simulator.test("/scope-storage")
                                      .post())
        .assertContextAttributeNotNull("contextObject")
        .assertRequestAttributeNotNull("requestObject")
        .assertActionSessionAttributeNotNull("org.example.action.ScopeStorageAction", "actionSessionObject")
        .assertSessionAttributeNotNull("sessionObject");
  }

  @Test
  public void post_scopeStorageInBaseClass() throws Exception {
    // Set values during POST - fields exist in base abstract class
    test.simulate(() -> test.simulator.test("/extended-scope-storage")
                                      .post())
        .assertContextAttributeNotNull("contextObject")
        .assertRequestAttributeNotNull("requestObject")
        .assertActionSessionAttributeNotNull("org.example.action.ExtendedScopeStorage", "actionSessionObject")
        .assertSessionAttributeNotNull("sessionObject")

        // Call [GET] -- assume everything is set from prior request except request attribute is null.
        .simulate(() -> test.simulator.test("/extended-scope-storage")
                                      .get())
        .assertContextAttributeNotNull("contextObject")
        .assertRequestAttributeIsNull("requestObject")
        .assertActionSessionAttributeNotNull("org.example.action.ExtendedScopeStorage", "actionSessionObject")
        .assertSessionAttributeNotNull("sessionObject")

        // Call [GET] on a different action -- only action and context attributes come over, request and action session are null.
        .simulate(() -> test.simulator.test("/another-extended-scope-storage")
                                      .get())
        .assertContextAttributeNotNull("contextObject")
        .assertRequestAttributeIsNull("requestObject")
        .assertActionSessionAttributeIsNull("org.example.action.AnotherExtendedScopeStorage", "actionSessionObject")
        .assertSessionAttributeNotNull("sessionObject");
  }

  @Test
  public void post_withURLParameter_IndexAmbiguity() {
    // https://github.com/FusionAuth/fusionauth-issues/issues/434

    // Add a trailing slash when the URL parameter is {userId} which is a UUID
    // - Ok, it is ignored and there are no conversion exceptions
    simulator.test("/api/action-value/login/")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/action-value/login", "potato:null", "userId:null");

    // Manually added the 'index' suffix, expect a couldNotConvert error because this is not a UUID
    simulator.test("/api/action-value/login/index")
             .post()
             .assertStatusCode(200)
             .assertContainsFieldErrors("userId")
             .assertBodyContains("/api/action-value/login", "potato:null", "userId:null");

    // Leave a trailing slash and pass an invalid type for userId as a request parameter
    simulator.test("/api/action-value/login/")
             .withParameter("userId", "index")
             .post()
             .assertStatusCode(200)
             .assertContainsFieldErrors("userId")
             .assertBodyContains("/api/action-value/login", "potato:null", "userId:null");

    // No trailing slash and pass an invalid type for userId as a request parameter
    simulator.test("/api/action-value/login")
             .withParameter("userId", "index")
             .post()
             .assertStatusCode(200)
             .assertContainsFieldErrors("userId")
             .assertBodyContains("/api/action-value/login", "potato:null", "userId:null");

    // Leave a trailing slash and use a separate variable with the name 'index'
    simulator.test("/api/action-value/login/")
             .withParameter("potato", "index")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/action-value/login", "potato:index", "userId:null");

    // Correct usage, URL parameter with the correct type.
    UUID userId = UUID.randomUUID();
    simulator.test("/api/action-value/login")
             .withURLSegment(userId)
             .withParameter("potato", "index")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/action-value/login", "potato:index", "userId:" + userId);

    // With a real Index page, add a URL parameter, add a separate request parameter with a value of index
    simulator.test("/api/action-value/index")
             .withParameter("potato", "index")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/action-value/index", "potato:index", "userId:null");

    // Real Index page, add a trailing slash when the URL parameter is a UUID
    // - The trailing slash should be ignored, no errors
    simulator.test("/api/action-value/index/")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/action-value/index", "potato:null", "userId:null");

    // Real Index page, with a legit UUID on the URL
    simulator.test("/api/action-value/index/" + userId)
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/action-value/index", "potato:null", "userId:" + userId);

    // Real Index page, with a legit UUID using a URL segment and a trailing slash
    simulator.test("/api/action-value/index/")
             .withURLSegment(userId)
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/action-value/index", "potato:null", "userId:" + userId);

    // Real Index page, with a legit UUID using a URL segment and no trailing slash
    simulator.test("/api/action-value/index")
             .withURLSegment(userId)
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/action-value/index", "potato:null", "userId:" + userId);
  }

  @Test
  public void post_withoutURLParameter_IndexAmbiguity() {
    // https://github.com/FusionAuth/fusionauth-issues/issues/434

    // Add a trailing slash when the URL parameter is {userId} which is a UUID
    // - Ok, it is ignored and there are no conversion exceptions
    simulator.test("/api/no-action-value/login/")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/login", "potato:null", "userId:null");

    // All of these will be ignored
    simulator.test("/api/no-action-value/login/foo/bar/baz")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/login", "potato:null", "userId:null");

    // Manually added the 'index' suffix, not expect a URL segment, so it is ignored.
    simulator.test("/api/no-action-value/login/index")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/login", "potato:null", "userId:null");

    // Leave a trailing slash and pass an invalid type for userId as a request parameter
    simulator.test("/api/no-action-value/login/")
             .withParameter("userId", "index")
             .post()
             .assertStatusCode(200)
             .assertContainsFieldErrors("userId")
             .assertBodyContains("/api/no-action-value/login", "potato:null", "userId:null");

    // No trailing slash and pass an invalid type for userId as a request parameter
    simulator.test("/api/no-action-value/login")
             .withParameter("userId", "index")
             .post()
             .assertStatusCode(200)
             .assertContainsFieldErrors("userId")
             .assertBodyContains("/api/no-action-value/login", "potato:null", "userId:null");

    // Leave a trailing slash and use a separate variable with the name 'index'
    simulator.test("/api/no-action-value/login/")
             .withParameter("potato", "index")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/login", "potato:index", "userId:null");

    // URL parameter is ignored since the Action is not configured to take a URL parameter
    UUID userId = UUID.randomUUID();
    simulator.test("/api/no-action-value/login")
             .withURLSegment(userId)
             .withParameter("potato", "index")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/login", "potato:index", "userId:null");

    // userId and potato are taking as request parameters, bonzai!
    simulator.test("/api/no-action-value/login")
             .withURLSegment(userId)
             .withParameter("potato", "index")
             .withParameter("userId", userId)
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/login", "potato:index", "userId:" + userId);

    // With a real Index page, add a URL parameter, add a separate request parameter with a value of index
    simulator.test("/api/no-action-value/index")
             .withParameter("potato", "index")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/index", "potato:index", "userId:null");

    // Real Index page, add a trailing slash when the URL parameter is a UUID
    // - The trailing slash should be ignored, no errors
    simulator.test("/api/no-action-value/index/")
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/index", "potato:null", "userId:null");

    // Real Index page, with a legit UUID on the URL but it is ignored because are not capturing URL parameters
    simulator.test("/api/no-action-value/index/" + userId)
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/index", "potato:null", "userId:null");

    // Real Index page, with a legit UUID using a URL segment and a trailing slash, ignored.
    simulator.test("/api/no-action-value/index/")
             .withURLSegment(userId)
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/index", "potato:null", "userId:null");

    // Real Index page, with a legit UUID using a URL segment and no trailing slash, ignored
    simulator.test("/api/no-action-value/index")
             .withURLSegment(userId)
             .post()
             .assertStatusCode(200)
             .assertContainsNoFieldMessages()
             .assertBodyContains("/api/no-action-value/index", "potato:null", "userId:null");
  }

  @Test
  public void singletons() {
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

  @Test
  public void uriParameters() throws Exception {
    test.simulate(() -> test.simulator.test("/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff")
                                      .post()
                                      .assertStatusCode(200)
                                      .assertBodyContains("firstName=brian", "lastName=pontarelli", "theRest=then,a,bunch,of,stuff"));
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
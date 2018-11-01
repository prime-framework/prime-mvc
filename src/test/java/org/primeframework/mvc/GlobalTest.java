/*
 * Copyright (c) 2001-2018, Inversoft Inc., All Rights Reserved
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
import org.primeframework.mvc.parameter.convert.ConverterStateException;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.parameter.el.MissingPropertyExpressionException;
import org.primeframework.mvc.test.RequestSimulator;
import org.primeframework.mvc.util.URIBuilder;
import org.testng.annotations.Test;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import freemarker.template.Configuration;
import static org.testng.Assert.assertEquals;
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
  public void get_action_package_collision() throws Exception {
    test.simulate(() -> test.simulator.test("/foo/view/bar/baz")
                                      .withUrlSegment("42")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view/bar/baz!", "42"))

        .simulate(() -> test.simulator.test("/foo/view/bar/baz")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view/bar/baz!", "empty"))

        .simulate(() -> test.simulator.test("/foo/view/bar")
                                      .withUrlSegment("42")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view/bar!", "42"))

        .simulate(() -> test.simulator.test("/foo/view/bar")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view/bar!", "empty"))

        .simulate(() -> test.simulator.test("/foo/view")
                                      .withUrlSegment("42")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view!", "42"))

        .simulate(() -> test.simulator.test("/foo/view")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo/view!", "empty"))

        .simulate(() -> test.simulator.test("/foo")
                                      .withUrlSegment("42")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo!", "42"))

        .simulate(() -> test.simulator.test("/foo")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("/foo!", "empty"));
  }

  @Test
  public void escapePathSegmentsWithWildCard() throws Exception {
    test.simulate(() -> test.simulator.test("/escaped-path-segments")
                                      .withUrlSegment("foo%20bar")
                                      .withUrlSegment("foobar")
                                      .withUrlSegment("foo%20bar")
                                      .withUrlSegment("foo@bar")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("Success!", "parm=foo bar", "theRest=foobar,foo bar,foo@bar"));
  }

  @Test
  public void get_nested_parameters() throws Exception {
    test.simulate(() -> test.simulator.test("/nested")
                                      .withUrlSegment("42")
                                      .withUrlSegment("99")
                                      .withUrlSegment("parameter")
                                      .withUrlSegment("foo")
                                      .withUrlSegment("bar")
                                      .get()
                                      .assertStatusCode(200)
                                      .assertBodyContains("Success!", "preParam1=42", "preParam2=99", "endParam1=foo", "endParam2=bar"));
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
  public void embeddedFormHandling() throws Exception {
    // Ensure this 'required' parameter for PageOne does not mess up PageTwo which does not have an Id field.
    test.simulate(() -> simulator.test("/scope/page-one")
                                 .withUrlSegment("IdOnlyForPageOne")
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
  public void get_JSONView() throws Exception {
    test.simulate(() -> simulator.test("/views/entry/api")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSONFile(Paths.get("src/test/resources/json/views/entry/entry-api.json")));

    test.simulate(() -> simulator.test("/views/entry/export")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSONFile(Paths.get("src/test/resources/json/views/entry/entry-export.json")));

    // Serialize an object using @JSONResponse when no view is specified for an object that has only annotated fields
    // The DEFAULT_VIEW_INCLUSION is the default value, but expliclty configured in case the default prime configuration changes
    test.configureObjectMapper(om -> objectMapper.enable(MapperFeature.DEFAULT_VIEW_INCLUSION))
        .simulate(() -> simulator.test("/views/entry/no-view-defined")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSONFile(Paths.get("src/test/resources/json/views/entry/entry-no-view-defined.json")));

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
                                 .assertJSONFile(Paths.get("src/test/resources/json/views/entry/entry-no-view-defined.json")));

    // Default view inclusion is disabled and if we serialize a @JSONResponse with a view that has no fields in the object - empty response.
    test.simulate(() -> simulator.test("/views/entry/wrong-view-defined")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSON("{}"));
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
  public void get_jwtDisabledJwtAuthentication() throws Exception {
    // Send in a JWT Authorization header when the Action has JWT disabled. Should always get a 401. When a JWT is provided, the action expects JWT to be enabled.
    test.simulate(() -> simulator.test("/jwt-authorized-disabled")
                                 .withParameter("authorized", true)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_execute_redirect() throws Exception {
    // Follow the redirect and assert on that response as well
    test.simulate(() -> simulator.test("/temp-redirect")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/temp-redirect-target")
                                 .executeRedirect(response -> response.assertStatusCode(200)
                                                                      .assertBodyContains("Look Ma, I'm redirected.")));
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
    simulator.test("/api")
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
  public void get_secure() throws Exception {
    test.simulate(() -> test.simulator.test("/secure")
                                      .get()
                                      .assertStatusCode(401));
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
  public void uriParameters() throws Exception {
    test.simulate(() -> test.simulator.test("/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff")
                                      .post()
                                      .assertStatusCode(200)
                                      .assertBodyContains("firstName=brian", "lastName=pontarelli", "theRest=then,a,bunch,of,stuff"));
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

  private void assertSingleton(RequestSimulator simulator, Class<?> type) {
    assertSame(simulator.injector.getInstance(type), simulator.injector.getInstance(type));
  }

  private void assertSingletonConverter(RequestSimulator simulator, Class<?> type) {
    Map<Class<?>, GlobalConverter> converters = simulator.injector.getInstance(Key.get(new TypeLiteral<Map<Class<?>, GlobalConverter>>() {
    }));
    assertSame(converters.get(type), converters.get(type));
  }
}
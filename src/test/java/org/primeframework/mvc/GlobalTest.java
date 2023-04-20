/*
 * Copyright (c) 2001-2023, Inversoft Inc., All Rights Reserved
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
import io.fusionauth.http.HTTPValues.Headers;
import io.fusionauth.http.HTTPValues.Methods;
import org.example.action.JwtAuthorizedAction;
import org.example.action.LotsOfMessagesAction;
import org.example.action.OverrideMeAction;
import org.example.action.store.BaseStoreAction;
import org.example.action.user.EditAction;
import org.example.domain.UserField;
import org.primeframework.mvc.action.config.ActionConfigurationProvider;
import org.primeframework.mvc.container.ContainerResolver;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.parameter.convert.ConverterProvider;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.util.URIBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;

/**
 * This class tests the MVC from a (mile) high level perspective. (see what I did there?)
 *
 * @author Brian Pontarelli
 */
public class GlobalTest extends PrimeBaseTest {
  private Path jsonDir;

  @BeforeClass
  public void beforeClass() {
    jsonDir = Path.of("src/test/resources/json");
  }

  @Test
  public void cache_control_disabled() throws Exception {
    // Disable cache control managed by the result handler.
    test.simulate(() -> simulator.test("/cache-control-disabled")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertHeaderDoesNotContain("Cache-Control"));
  }

  @Test
  public void cache_control_override() throws Exception {
    // Override the default cache control settings, values are not validated to be valid.
    test.simulate(() -> simulator.test("/cache-control-override")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-store"));
  }

  @Test
  public void custom_constraints() throws Exception {
    // Using @ConstraintOverride on the delete method.
    test.loginUserWithRole("delete-only")
        .simulate(() -> simulator.test("/secure")
                                 .delete()
                                 .assertStatusCode(200))

        // But fails for put
        .simulate(() -> simulator.test("/secure")
                                 .put()
                                 .assertStatusCode(403));

    // Using the @ConstraintOverrideMethod
    test.loginUserWithRole("put-only")
        .simulate(() -> simulator.test("/secure")
                                 .put()
                                 .assertStatusCode(200))

        // But fails for delete
        .simulate(() -> simulator.test("/secure")
                                 .delete()
                                 .assertStatusCode(403));
  }

  @Test
  public void embeddedFormHandling() throws Exception {
    // Ensure this 'required' parameter for PageOne does not mess up PageTwo which does not have an Id field.
    test.simulate(() -> simulator.test("/scope/page-one")
                                 .withURLSegment("IdOnlyForPageOne")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache"));

    // Ensure the @FileUpload in the PageOneAction does not mess up PageTwo
    test.createFile()
        .simulate(() -> simulator.test("/scope/page-one")
                                 .withFile("file", test.tempFile, "text/plain")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache"));
  }

  @Test
  public void escapePathSegmentsWithWildCard() throws Exception {
    test.simulate(() -> simulator.test("/escaped-path-segments")
                                 .withURLSegment("foo bar")
                                 .withURLSegment("foobar")
                                 .withURLSegment("foo bar")
                                 .withURLSegment("foo@bar")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertBodyContains(
                                     "Success!",
                                     "parm=foo bar",
                                     "theRest=foobar,foo bar,foo@bar"));

    test.simulate(() -> simulator.test("/escaped-path-segments")
                                 .withURLSegment("<foo>")
                                 .withURLSegment("foo bar")
                                 .withURLSegment("foobar")
                                 .withURLSegment("foo bar")
                                 .withURLSegment("foo@bar")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertBodyContains(
                                     "Success!",
                                     "parm=&lt;foo&gt;",
                                     "theRest=foo bar,foobar,foo bar,foo@bar"));
  }

  @Test
  public void get() throws Exception {
    // Not called yet
    assertEquals(MockMVCWorkflowFinalizer.Called.get(), 0);

    simulator.test("/user/edit")
             .get()
             .assertStatusCode(200)
             // header name is not case-sensitive
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertHeaderContains("cache-control", "no-cache")
             .assertHeaderDoesNotContain("Potato")
             .assertBodyFile(Path.of("src/test/resources/html/edit.html"));

    // 1 call! Ah ah ah...
    assertEquals(MockMVCWorkflowFinalizer.Called.get(), 1);

    EditAction.getCalled = false;
    simulator.test("/user/edit")
             .withHeader(Headers.MethodOverride, Methods.GET)
             .get()
             .assertStatusCode(200);

    assertTrue(EditAction.getCalled);

    // 2 calls! Ah ah ah...
    assertEquals(MockMVCWorkflowFinalizer.Called.get(), 2);
  }

  @Test
  public void get_ContentTypeOverride() {
    simulator.test("/content-type-override")
             .get()
             .assertStatusCode(200)
             .assertContentType("application/json+scim");

    // Override from the JSON annotation
    simulator.test("/content-type-override")
             .withURLParameter("status", 400)
             .get()
             .assertStatusCode(400)
             .assertContentType("application/json+error");
  }

  @Test
  public void get_JSONView() throws Exception {
    test.simulate(() -> simulator.test("/views/entry/api")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertJSONFile(jsonDir.resolve("views/entry/entry-api.json")));

    test.simulate(() -> simulator.test("/views/entry/export")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertJSONFile(jsonDir.resolve("views/entry/entry-export.json")));

    // Serialize an object using @JSONResponse when no view is specified for an object that has only annotated fields
    // The DEFAULT_VIEW_INCLUSION is the default value, but explicitly configured in case the default prime configuration changes
    test.configureObjectMapper(om -> objectMapper.enable(MapperFeature.DEFAULT_VIEW_INCLUSION))
        .simulate(() -> simulator.test("/views/entry/no-view-defined")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertJSONFile(jsonDir.resolve("views/entry/entry-no-view-defined.json")));

    // Default view inclusion is enabled and if we serialize a @JSONResponse with a view that has no fields in the object - empty response
    test.simulate(() -> simulator.test("/views/entry/wrong-view-defined")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertJSON("{}"));

    // Ensure we get a response even when we disable the default view inclusion if we do not specify a view
    test.configureObjectMapper(om -> objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION))
        .simulate(() -> simulator.test("/views/entry/no-view-defined")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertJSONFile(jsonDir.resolve("views/entry/entry-no-view-defined.json")));

    // Default view inclusion is disabled and if we serialize a @JSONResponse with a view that has no fields in the object - empty response.
    test.simulate(() -> simulator.test("/views/entry/wrong-view-defined")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertJSON("{}"));
  }

  @Test
  public void get_action_backed_template_slashes() {
    // Ok
    simulator.test("/freemarker/action-backed")
             .get()
             .assertStatusCode(200)
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertBodyContains("Yo, nice template, I have an action.");

    // Double slash, redirect to the correct location
    simulator.test("/freemarker//action-backed")
             .get()
             .assertStatusCode(200)
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertBodyContains("Yo, nice template, I have an action.");

    // Triple slashes, redirect to the correct location
    simulator.test("/freemarker///action-backed")
             .get()
             .assertStatusCode(200)
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertBodyContains("Yo, nice template, I have an action.");

    // Triple slashes, redirect to the correct location
    try {
      simulator.test("///bing.com")
               .get();
      fail("Whoa!! We should have failed so hard it isn't even funny.");
    } catch (Throwable e) {
      assertEquals(e.getClass(), AssertionError.class);
    }
  }

  @Test
  public void get_action_package_collision() throws Exception {
    test.simulate(() -> simulator.test("/foo/view/bar/baz")
                                 .withURLSegment("42")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertBodyContains("/foo/view/bar/baz!", "42"))

        .simulate(() -> simulator.test("/foo/view/bar/baz")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("/foo/view/bar/baz!", "empty"))

        .simulate(() -> simulator.test("/foo/view/bar")
                                 .withURLSegment("42")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("/foo/view/bar!", "42"))

        .simulate(() -> simulator.test("/foo/view/bar")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("/foo/view/bar!", "empty"))

        .simulate(() -> simulator.test("/foo/view")
                                 .withURLSegment("42")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("/foo/view!", "42"))

        .simulate(() -> simulator.test("/foo/view")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("/foo/view!", "empty"))

        .simulate(() -> simulator.test("/foo")
                                 .withURLSegment("42")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("/foo!", "42"))

        .simulate(() -> simulator.test("/foo")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains("/foo!", "empty"));
  }

  @Test
  public void get_collectionConverter() throws Exception {
    // Both of these will fail because the action has a List<String> as the backing values for this form, and the input field is a text field.
    test.simulate(() -> simulator.test("/collection-converter")
                                 .withURLParameter("string", "foo,bar,baz")
                                 .get()
                                 .assertStatusCode(500));

    test.simulate(() -> simulator.test("/collection-converter")
                                 .withURLParameter("string", "bar")
                                 .withURLParameter("string", "baz")
                                 .get()
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertStatusCode(500));

    // It will work if we use a backing collection with an iterator in the form to build multiple form fields
    test.simulate(() -> simulator.test("/collection-converter")
                                 .withURLParameter("strings", "bar")
                                 .withURLParameter("strings", "baz")
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
                                 .withURLParameter("strings", "foo,bar,baz")
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
             .get()
             .assertStatusCode(500);

    // Bad parameter (i.e. /invalid-api?bad-param=42
    simulator.test("/invalid-api")
             .withParameter("bad-param", "42")
             .get()
             .assertStatusCode(500);
  }

  @Test
  public void get_execute_redirect() throws Exception {
    // Follow the redirect and another redirect and assert on that response as well - and ensure a message set in the first redirect gets all the way to the end
    test.simulate(() -> simulator.test("/temp-redirect")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertHeaderContains("Cache-Control", "no-cache")
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
                                                                                                                                     "noNameEmpty", // Missing because it has no name attribute
                                                                                                                                     "radioValue2",
                                                                                                                                     "checkboxValue2",
                                                                                                                                     "selectedValueOptionB",
                                                                                                                                     "textareaValue")
                                                                                                                                 .assertBodyDoesNotContain(
                                                                                                                                     "disabledValue",
                                                                                                                                     "radioValue1",
                                                                                                                                     "checkboxValue1",
                                                                                                                                     "noNameValue",
                                                                                                                                     "selectEmpty")))));
  }

  @Test
  public void get_execute_relativeRedirect() throws Exception {
    // Follow the redirect and another redirect and assert on that response as well - and ensure a message set in the first redirect gets all the way to the end
    test.simulate(() -> simulator.test("/temp-relative-redirect")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertHeaderContains("Cache-Control", "no-cache")
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
                                                                                                                                     "noNameEmpty", // Missing because it has no name attribute
                                                                                                                                     "hiddenValue",
                                                                                                                                     "radioValue",
                                                                                                                                     "checkboxValue",
                                                                                                                                     "textareaValue")
                                                                                                                                 .assertBodyDoesNotContain(
                                                                                                                                     "disabledValue",
                                                                                                                                     "noNameValue")))));
  }

  @Test(enabled = false)
  public void get_expressionEvaluatorSkippedUsesRequest() throws Exception {
    // Tests that the expression evaluator safely gets skipped while looking for values and Prime then checks the
    // HttpServletRequest and finds the value
    test.simulate(() -> simulator.test("/value-in-request")
                                 .get()
                                 .assertBodyContains("baz"));

    // TODO : Can't really assert on the request attributes. The request is made in a separate request/thread.
    //        We could just delete this test, or come up with a way to keep a copy of the request around to assert on.
    //        It looks like this test is supposed to be doing more than just checking request attributes. So
    //        we should probably review this code and see if we are doing what this test originally was written
    //        for.
  }

  @Test
  public void get_freemarker_double_escape() {
    simulator.test("/freemarker/double-escape")
             .get()
             .assertStatusCode(500);
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
             .assertBodyFile(Path.of("src/test/resources/html/full-form.html"));
  }

  @Test
  public void get_index() throws Exception {
    test.simulate(() -> simulator.test("/user/")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
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
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertStatusCode(200));

    // Test with Bearer scheme
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .get()
                                 .assertStatusCode(200));

    // Missing JWT w/ Bearer scheme
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "Bearer ")
                                 .get()
                                 .assertStatusCode(401));


    // Missing JWT w/ Bearer scheme, no space after scheme
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "Bearer")
                                 .get()
                                 .assertStatusCode(401));

    // Bad JWT w/ Bearer scheme
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "Bearer Foo")
                                 .get()
                                 .assertStatusCode(401));

    // Missing JWT w/ JWT scheme
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "JWT ")
                                 .get()
                                 .assertStatusCode(401));


    // Missing JWT w/ JWT scheme, no space after scheme
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "JWT")
                                 .get()
                                 .assertStatusCode(401));

    // Bad JWT w/ JWT scheme
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "JWT Foo")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_jwtDisabledJwtAuthentication() throws Exception {
    // Send in a JWT Authorization header when the Action has JWT disabled. Should always get a 401. When a JWT is provided, the action expects JWT to be enabled.
    test.simulate(() -> simulator.test("/jwt-authorized-disabled")
                                 .withURLParameter("authorized", true)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .get()
                                 .assertStatusCode(401));

    // Same, use Bearer scheme
    test.simulate(() -> simulator.test("/jwt-authorized-disabled")
                                 .withURLParameter("authorized", true)
                                 .withHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_jwtExpired() throws Exception {
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withURLParameter("authorized", true)
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE0NDUxMDA3MzF9.K18gIegEBfxgj8rU4D2WDh3CzEmRUmy8qBS7SWAcG9w")
                                 .get()
                                 .assertStatusCode(401));
  }

  @Test
  public void get_jwtInvalidSignature() throws Exception {
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withURLParameter("authorized", true)
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
  public void get_largeFTL() {
    simulator.test("/large-ftl")
             .get()
             .assertStatusCode(200)
             .assertBodyContains("large FTL");
  }

  @Test
  public void get_message_callback() throws Exception {
    // call an action that adds messages that calls another action that adds messages to ensure we can assert on the message store properly.
    test.simulate(() -> simulator.test("/callback")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertContainsGeneralMessageCodes(MessageType.ERROR, "[ERROR]")
                                 .assertContainsGeneralMessageCodes(MessageType.INFO, "[INFO]")
                                 .assertContainsGeneralMessageCodes(MessageType.WARNING, "[WARNING]"));
  }

  @Test
  public void get_metrics() throws Exception {
    simulator.test("/user/full-form")
             .get()
             .assertBodyFile(Path.of("src/test/resources/html/full-form.html"));

    Map<String, Timer> timers = metricRegistry.getTimers();
    assertEquals(timers.get("prime-mvc.[/user/full-form].requests").getCount(), 1);
  }

  @Test
  public void get_metricsErrors() {
    simulator.test("/execute-method-throws-exception")
             .get()
             .assertStatusCode(500);

    Map<String, Timer> timers = metricRegistry.getTimers();
    assertEquals(timers.get("prime-mvc.[/execute-method-throws-exception].requests").getCount(), 1);

    Map<String, Meter> meters = metricRegistry.getMeters();
    assertEquals(meters.get("prime-mvc.[/execute-method-throws-exception].errors").getCount(), 1);
  }

  @Test
  public void get_nested_parameters() throws Exception {
    test.simulate(() -> simulator.test("/nested")
                                 .withURLSegment("42")
                                 .withURLSegment("99")
                                 .withURLSegment("parameter")
                                 .withURLSegment("foo")
                                 .withURLSegment("bar")
                                 .get()
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertStatusCode(200)
                                 .assertBodyContains("Success!", "preParam1=42", "preParam2=99", "endParam1=foo", "endParam2=bar"));
  }

  @Test
  public void get_nonFormFields() throws Exception {
    simulator.test("/user/details-fields")
             .get()
             .assertBodyFile(Path.of("src/test/resources/html/details-fields.html"));
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
  public void get_onlyKnownParameters() throws Exception {
    // Action w/out @UnknownParameters
    configuration.allowUnknownParameters = false;
    test.simulate(() -> simulator.test("/only-known-parameters")
                                 .withParameter("foo", "bar")
                                 .withParameter("foo", "baz")
                                 .withParameter("foo.bar", "baz")
                                 .withParameter("foo/0/bar/bam", "purple")
                                 .post()
                                 .assertStatusCode(500)
                                 .assertHeaderContains("Cache-Control", "no-cache"));
  }

  @Test
  public void get_overrideClassNameForURI() throws Exception {
    test.simulate(() -> simulator.test("/OverrideMe")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyIsEmpty());

    assertTrue(OverrideMeAction.invoked);

    // Reset
    OverrideMeAction.invoked = false;

    boolean succeeded = false;

    try {
      test.simulate(() -> simulator.test("/override-me")
                                   .get()
                                   .assertStatusCode(404)
                                   .assertBodyIsEmpty());
      succeeded = true;
    } catch (Error expected) {
    }

    if (succeeded) {
      fail("Expected a failure!");
    }
  }

  @Test
  public void get_percent_encoded_segment() throws Exception {
    test.simulate(() -> simulator.test("/foo/view")
                                 .withURLSegment("<strong>foo</strong>")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertBodyContains("/foo/view!", "id=&lt;strong&gt;foo&lt;/strong&gt;"));

    test.simulate(() -> simulator.test("/foo/view/%3Cstrong%3Efoo%3C%2Fstrong%3E")
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
                                 .assertHeaderContains("Cache-Control", "no-cache")
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
  public void get_preRender() {
    simulator.test("/pre-render-method")
             .withParameter("result", "forward")
             .get()
             .assertStatusCode(200)
             .assertContentType("text/html; charset=UTF-8")
             .assertBodyContains("Forward_Yep!", "JSON_Nope!", "Noop_Nope!");

    simulator.test("/pre-render-method")
             .withURLParameter("result", "json")
             .get()
             .assertStatusCode(200)
             .assertContentType("application/json; charset=UTF-8")
             .assertBodyContains("trust me it is json");

    simulator.test("/pre-render-method")
             .withURLParameter("result", "noop")
             .get()
             .assertStatusCode(201)
             .assertContentType("application/potato")
             .assertBodyContains("You've been no-oped!");
  }

  @Test
  public void get_redirect() throws Exception {
    // Contains no parameters
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withURLParameter("redirectURI", "/foo")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertRedirect("/foo"));

    // Contains a single parameter, calling beginQuery is optional
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withURLParameter("redirectURI", "/foo?bar=baz")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz")
                                 .assertRedirect("/foo", params -> params.with("bar", "baz")));

    // Contains a single parameter with calling beginQuery()
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withURLParameter("redirectURI", "/foo?bar=baz")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz")
                                 .assertRedirect("/foo", params -> params.beginQuery()
                                                                         .with("bar", "baz")));

    // Contains multiple parameters
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withURLParameter("redirectURI", "/foo?bar=baz&boom=dynamite")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz&boom=dynamite")
                                 .assertRedirect("/foo", params -> params.beginQuery()
                                                                         .with("bar", "baz")
                                                                         .with("boom", "dynamite")));

    // Contains a single parameter after a fragment
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withURLParameter("redirectURI", "/foo#bar=baz")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo#bar=baz")
                                 .assertRedirect("/foo", params -> params.beginFragment()
                                                                         .with("bar", "baz")));

    // Contains multiple parameters after a fragment
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withURLParameter("redirectURI", "/foo#bar=baz&boom=dynamite")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo#bar=baz&boom=dynamite")
                                 .assertRedirect("/foo", params -> params.beginFragment()
                                                                         .with("bar", "baz")
                                                                         .with("boom", "dynamite")));

    // Contains a single parameter and a single parameter after a fragment
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withURLParameter("redirectURI", "/foo?bar=baz#middle=out")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz#middle=out")
                                 .assertRedirect("/foo", params -> params.beginQuery()
                                                                         .with("bar", "baz")
                                                                         .beginFragment()
                                                                         .with("middle", "out")));

    // Contains multiple parameters and multiple parameters after a fragment
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withURLParameter("redirectURI", "/foo?bar=baz&boom=dynamite#middle=out&not=hotdog")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertRedirect("/foo?bar=baz&boom=dynamite#middle=out&not=hotdog")
                                 .assertRedirect("/foo", params -> params.beginQuery()
                                                                         .with("bar", "baz")
                                                                         .with("boom", "dynamite")
                                                                         .beginFragment()
                                                                         .with("middle", "out")
                                                                         .with("not", "hotdog")));

    // URL has multiple parameters
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withURLParameter("redirectURI", "/foo?bar=baz&q=foo&code=bar")
                                 .get()
                                 .assertStatusCode(302)
                                 .assertRedirect("/foo?bar=baz&q=foo&code=bar")
                                 .assertRedirect("/foo", params -> params.beginQuery()
                                                                         .with("bar", "baz")
                                                                         .with("q", "foo")
                                                                         .with("code", "bar")));

    // When we expect parameters, assert that the URL does not have parameters. This tests for an edge case
    // that we have fixed in the RequestResult.
    try {
      test.simulate(() -> simulator.test("/complex-redirect")
                                   .withURLParameter("redirectURI", "/foo?bar=baz&q=foo&code=bar")
                                   .get()
                                   .assertStatusCode(302)
                                   .assertRedirect("/foo"));
      fail("Expected a failure.");
    } catch (Error e) {
      assertEquals(e.getClass(), AssertionError.class);
    }
  }

  @Test
  public void get_redirect_withActual() throws Exception {
    // Contains no parameters
    test.simulate(() -> simulator.test("/complex-redirect")
                                 .withURLParameter("redirectURI", "/foo?bing=bam&instant=" + System.currentTimeMillis())
                                 .get()
                                 .assertStatusCode(302)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertRedirect("/foo", params -> params.withActual("instant")
                                                                         .with("bing", "bam")));
  }

  @Test
  public void get_secure() throws Exception {
    test.simulate(() -> simulator.test("/secure")
                                 .get()
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertStatusCode(401));
  }

  @Test
  public void get_template_noAction() throws Exception {
    // Ok
    simulator.test("/freemarker/stand-alone-template")
             .get()
             .assertStatusCode(200)
             .assertBodyContains("Yo, nice template.");

    // Double slash, redirect to the correct location
    simulator.test("/freemarker//stand-alone-template")
             .get()
             .assertStatusCode(200)
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertBodyContains("Yo, nice template.");

    // Triple slashes, redirect to the correct location
    simulator.test("/freemarker///stand-alone-template")
             .get()
             .assertStatusCode(200)
             .assertBodyContains("Yo, nice template.");

    // Double slash, on two paths, redirect to the correct location
    simulator.test("/freemarker//sub//stand-alone-template")
             .get()
             .assertStatusCode(200)
             .assertBodyContains("Yo, nice sub-directory template.");

    try {
      // Invalid path, double slash, redirect, still invalid.
      simulator.test("/freemarker//does-not-exist")
               .get()
               .assertStatusCode(301)
               .assertRedirect("/freemarker/does-not-exist");
      fail("Expected a failure.");
    } catch (Error e) {
      assertEquals(e.getClass(), AssertionError.class);
    }

    try {
      // Index template, ensure we clean up the '/index/ suffix that will get added during action mapping
      simulator.test("/freemarker/sub//")
               .get()
               .assertStatusCode(301)
               .assertRedirect("/freemarker/sub/")
               // A 301 will not contain these headers
               .assertHeaderDoesNotContain("Cache-Control")
               .executeRedirect(result -> result.assertStatusCode(200)
                                                .assertBodyContains("Yo, nice sub-directory index template."));
      fail("Expected a failure.");
    } catch (Error e) {
      assertEquals(e.getClass(), AssertionError.class);
    }
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
                                 .assertJSON("{\"called\": \"/.well-known/openid-configuration\"}"));

    // The nested directory does not have a package-modifier, so we will get the actual path of 'well-known' instead of '.well-known'.
    test.simulate(() -> simulator.test("/.well-known/well-known/openid-configuration")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertJSON("{\"called\": \"/.well-known/well-known/openid-configuration\"}"));

    // Testing two levels deep, but only the 1st and 3rd level have a package modifier.
    // .well-known -> well-known -> .well-known. The package modifier causes us to ignore the 'potato' package name.
    test.simulate(() -> simulator.test("/.well-known/well-known/.well-known/openid-configuration")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertJSON("{\"called\": \"/.well-known/well-known/potato/openid-configuration\"}"));

    // This is an invalid mapping, so it will result in a 404
    test.simulate(() -> simulator.test("/.well-known/.well-known/openid-configuration")
                                 .get()
                                 .assertStatusCode(404));
  }

  @Test
  public void hacked() {
    // Make sure we don't invoke 'freemarker.template.utility.Execute"
    simulator.test("/hacked")
             .get()
             .assertStatusCode(500)
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertBodyContains("Instantiating freemarker.template.utility.Execute is not allowed in the template for security reasons.");
  }

  @Test
  public void head() {
    simulator.test("/head")
             .head()
             .assertStatusCode(200)
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertBodyIsEmpty();
  }

  @Test
  public void head_jwtAuthorized() throws Exception {
    // This test will pass if we call the JWT authorize method or not....
    JwtAuthorizedAction.authorized = true;
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .head()
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertStatusCode(200));
  }

  @Test
  public void head_jwtNotAuthorized() throws Exception {
    JwtAuthorizedAction.authorized = false;
    test.simulate(() -> simulator.test("/jwt-authorized")
                                 .withHeader("Authorization", "JWT eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkifQ.qHdut1UR4-2FSAvh7U3YdeRR5r5boVqjIGQ16Ztp894")
                                 .head()
                                 .assertStatusCode(401)
                                 .assertHeaderContains("Cache-Control", "no-cache"));
  }

  @Test(enabled = false)
  public void manual_tooManyOpenFiles() throws Exception {
    // Cause a connection reset to the HTTP server, and only close every 100th connection.
    // - This will cause the connection reset and eventually also cause a 'Too many open files' exception.
    for (int i = 0; i < 250_000; i++) {
      if (i % 5_000 == 0) {
        System.out.println("Iteration [" + i + "]....");
      }

      try {
        Socket socket = new Socket();
        socket.setSoLinger(true, 0);
        socket.connect(new InetSocketAddress("localhost", 8080));
        OutputStream os = socket.getOutputStream();
        // Build an HTTP request body
        os.write((
            "POST /inform HTTP/1.1\r\n" +
                "Host: 192.168.1.44:8080\r\n" +
                "Accept: */*\r\n" +
                "Content-Length: 8588\r\n" +
                "\r\n"
        ).getBytes(StandardCharsets.UTF_8));

        os.flush();
        if (i % 100 == 0) {
          socket.close();
        }

      } catch (Exception e) {
        System.out.println("[Test Exception] [" + e.getMessage() + "]");
      }
    }

    boolean finish = false;
    while (!finish) {
      System.out.println("Waiting.... kill the test, or pause the debugger and set finish = true.");
      Thread.sleep(10_000);
    }
  }

  @Test
  public void missing() {
    // Direct action invocation
    simulator.test("/missing")
             .get()
             .assertStatusCode(404)
             .assertBodyContains("The page is missing!");

    // A traditional 404
    simulator.test("/also-missing")
             .get()
             .assertStatusCode(404)
             .assertBodyContains("The page is missing!");
  }

  @Test
  public void multipleJSONRequestMembers() throws Exception {
    simulator.test("/multiple-json-request")
             .post()
             .assertStatusCode(200)
             .assertHeaderContains("Cache-Control", "no-cache");

    simulator.test("/multiple-json-request")
             .withJSON(new Object())
             .post()
             .assertStatusCode(201)
             .assertHeaderContains("Cache-Control", "no-cache");

    simulator.test("/multiple-json-request")
             .withJSON(new Object())
             .delete()
             .assertStatusCode(202)
             .assertHeaderContains("Cache-Control", "no-cache");
  }

  @Test
  public void notAllowed() {
    simulator.test("/not-allowed")
             .get()
             .assertStatusCode(405)
             .assertHeaderContains("Cache-Control", "no-cache");

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
             .assertStatusCode(200)
             .assertHeaderContains("Cache-Control", "no-cache");
  }

  @Test
  public void notImplemented() {
    simulator.test("/not-allowed")
             .method("POTATO")
             .assertStatusCode(405) // Not allowed since we can handle any name but the action doesn't have that method
             .assertHeaderContains("Cache-Control", "no-cache");
  }

  @Test
  public void parameter_order_of_operation() throws Exception {
    // Test the order of operation when we have parameters in a body, URI segment, and query string.

    // Provide segment, body and parameter, additive - parameter + body
    test.simulate(() -> simulator.test("/parameter-handler")
                                 .withURLSegment("segment")
                                 .withURLParameter("value", "parameter")
                                 .withContentType("application/x-www-form-urlencoded")
                                 .withBody("""
                                     value=body
                                     """)
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("value:parameter,body"));

    // Segment and parameter, parameter wins
    test.simulate(() -> simulator.test("/parameter-handler")
                                 .withURLSegment("segment")
                                 .withURLParameter("value", "parameter")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("value:parameter"));

    // Segment and body, body wins
    test.simulate(() -> simulator.test("/parameter-handler")
                                 .withURLSegment("segment")
                                 .withContentType("application/x-www-form-urlencoded")
                                 .withBody("""
                                     value=body
                                     """)
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("value:body"));

    // Parameter and body, additive - parameter + body
    test.simulate(() -> simulator.test("/parameter-handler")
                                 .withURLParameter("value", "parameter")
                                 .withContentType("application/x-www-form-urlencoded")
                                 .withBody("""
                                     value=body
                                     """)
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("value:parameter,body"));

    // Body only, body
    test.simulate(() -> simulator.test("/parameter-handler")
                                 .withContentType("application/x-www-form-urlencoded")
                                 .withBody("""
                                     value=body
                                     """)
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("value:body"));

    // Segment only, segment
    test.simulate(() -> simulator.test("/parameter-handler")
                                 .withURLSegment("segment")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("value:segment"));

    // Parameter only, parameter
    test.simulate(() -> simulator.test("/parameter-handler")
                                 .withURLParameter("value", "parameter")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("value:parameter"));
  }

  @Test
  public void post() {
    simulator.test("/post")
             .post()
             .assertStatusCode(200)
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertBodyContains("Brian Pontarelli", "35", "Broomfield", "CO");
  }

  @Test
  public void post_JSONWithActual() throws Exception {
    simulator.test("/api")
             .withJSONFile(Path.of("src/test/resources/json/api-jsonWithActual-post.json"))
             .post()
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertJSONFileWithActual(UserField.class, Path.of("src/test/resources/json/api-jsonWithActual-post-response.ftl"));

    // Test a final field (the Jackson handler will put the JSON into the final field)
    simulator.test("/api-final")
             .withJSONFile(Path.of("src/test/resources/json/api-jsonWithActual-post.json"))
             .post()
             .assertJSONFileWithActual(UserField.class, Path.of("src/test/resources/json/api-jsonWithActual-post-response.ftl"));
  }

  @Test
  public void post_anyContentType() throws Exception {
    test.createFile("Hello World")
        .simulate(() -> simulator.test("/file-upload")
                                 .withFile("dataAnyType", test.tempFile, "text/plain")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache"))

        .simulate(() -> simulator.test("/file-upload")
                                 .withFile("dataAnyType", test.tempFile, "text/html")
                                 .post()
                                 .assertStatusCode(200))

        .simulate(() -> simulator.test("/file-upload")
                                 .withFile("dataAnyType", test.tempFile, "application/octet-stream")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache"));
  }

  @Test
  public void post_apiJSONBothWays() throws Exception {
    Path jsonFile = Path.of("src/test/resources/json/api-jsonBothWays-post.json");
    simulator.test("/api")
             .withJSONFile(jsonFile)
             .post()
             .assertJSONFile(jsonFile);
  }

  @Test
  public void post_binary() throws Exception {
    test.simulate(() -> simulator.test("/binary")
                                 .withURLParameter("expected", "Hello World")
                                 .withBody("Hello World")
                                 .withContentType("application/octet-stream")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertHeaderContains("Cache-Control", "no-cache"));
  }

  @Test
  public void post_chunked_json() throws Exception {
    // Our simulator does not chunk. This test uses the Java HTTP client to test chunking with Content-Type: application/json
    HttpClient client = HttpClient.newBuilder()
                                  .connectTimeout(Duration.of(500, ChronoUnit.MILLIS))
                                  .build();

    var response = client.send(
        HttpRequest.newBuilder()
                   .uri(URI.create("http://localhost:9080/api"))
                   .header(Headers.ContentType, "application/json")
                   .POST(BodyPublishers.ofInputStream(() -> new ByteArrayInputStream(readBytes("src/test/resources/json/api-jsonWithActual-post.json"))))
                   .build(),
        r -> BodySubscribers.ofString(StandardCharsets.UTF_8)
    );

    assertEquals(response.statusCode(), 200);
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
  public void post_cookies() throws Exception {
    // Test cookie propagation between invocations within the same test
    test.simulate(() -> simulator.test("/cookie")
                                 .withParameter("name", "token")
                                 .withParameter("value", "secret")
                                 .withParameter("saveMe", "save a value")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyIsEmpty()
                                 // Assert the cookie is set
                                 .assertCookie("token", "secret")
                                 // Assert we dropped a cookie for the @BrowserActionSession
                                 .assertContainsCookie("org.example.action.CookieAction$saveMe"))

        // Now make a GET request to the same action and verify the cookies were picked up on the request.
        .simulate(() -> simulator.test("/cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains(
                                     "Count:2",
                                     "token:secret",
                                     "org.example.action.CookieAction$saveMe:") // the value will be encoded
                                 // Cookie is not written back on this response
                                 .assertDoesNotContainsCookie("token"))

        // Clear the value set by @CookieAction by asking the action to set the value to 'null'
        .simulate(() -> simulator.test("/cookie")
                                 .withParameter("clearSaveMe", true)
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyIsEmpty()
                                 // Cookie is written out with a null
                                 .assertContainsCookie("org.example.action.CookieAction$saveMe"))

        // Retrieve again, value will be gone.
        .simulate(() -> simulator.test("/cookie")
                                 .get()
                                 .assertBodyContains(
                                     "Count:1",
                                     "token:secret")
                                 .assertBodyDoesNotContain(
                                     "org.example.action.CookieAction$saveMe:") // the value will be encoded
                                 // Cookie is not written back on this response
                                 .assertDoesNotContainsCookie("org.example.action.CookieAction$saveMe"))

        // Set a generic type
        .simulate(() -> simulator.test("/cookie")
                                 .withParameter("u.bar", "baz")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyIsEmpty()
                                 // Assert we dropped a cookie for the @BrowserActionSession
                                 .assertContainsCookie("org.example.action.CookieAction$u"))

        // Call get and see the value is set back into the action
        .simulate(() -> simulator.test("/cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains(
                                     "Count:2",
                                     "token:secret",
                                     "org.example.action.CookieAction$u:") // the value will be encoded
                                 // Cookie is not written back on this response
                                 .assertDoesNotContainsCookie("token"))

        // Set a generic collection
        .simulate(() -> simulator.test("/cookie")
                                 .withParameter("list[0]bar", "bing")
                                 .withParameter("list[1]bar", "boom")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyIsEmpty()
                                 // Assert we dropped a cookie for the @BrowserActionSession
                                 .assertContainsCookie("org.example.action.CookieAction$list"))

        .simulate(() -> simulator.test("/cookie")
                                 .get()
                                 .assertStatusCode(200)
                                 .assertBodyContains(
                                     "Count:3",
                                     "token:secret",
                                     "org.example.action.CookieAction$list:", // the value will be encoded
                                     "org.example.action.CookieAction$u:") // the value will be encoded
                                 // Cookie is not written back on this response
                                 .assertDoesNotContainsCookie("token"))

        // Blow chunks and get an error in the message store that will use the cookie lash message scope.
        .simulate(() -> simulator.test("/cookie")
                                 .withURLParameter("blowChunks", true)
                                 .get()
                                 .assertStatusCode(200)
                                 .assertContainsGeneralErrorMessageCodes("[CookieErrorException]")
                                 .assertBodyContainsMessagesFromKey("[CookieErrorException]")
                                 .assertBodyContains("Error count:1"))

        // Add a message to the message store on a post / redirect and ensure we only end up with a single message in the store.
        .simulate(() -> simulator.test("/cookie")
                                 .withURLParameter("addMessage", true)
                                 .post()
                                 .assertStatusCode(302)
                                 .assertRedirect("/cookie")
                                 .assertContainsGeneralInfoMessageCodes("[NobodyDrinkTheBeer]")

                                 // Execute the redirect and ensure we don't have duplicate messages
                                 .executeRedirect(result -> result.assertStatusCode(200)
                                                                  .assertBodyContains(
                                                                      "Count:3",
                                                                      "token:secret",
                                                                      "org.example.action.CookieAction$list:", // the value will be encoded
                                                                      "org.example.action.CookieAction$u:") // the value will be encoded
                                                                  .assertContainsGeneralInfoMessageCodes("[NobodyDrinkTheBeer]")));
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
    test.simulate(() -> simulator.test("/invalid-json")
                                 .withJSONFile(Path.of("src/test/resources/json/InvalidJsonAction.json"))
                                 .post()
                                 .assertStatusCode(400)
                                 .assertContainsFieldErrors("active")
                                 // The actual exception seems to vary a bit, so instead we are asserting the content type and then that some specific info is in the body.
                                 .assertContentType("application/json; charset=UTF-8")
                                 .assertBodyContains(
                                     "[invalidJSON]",
                                     "Unable to parse JSON. The property [active] was invalid. The error was [Possible conversion error]. The detailed exception was ["));
  }

  @Test
  public void post_jsonContentType() throws Exception {
    // Content-Type: application/scim+json
    simulator.test("/json-content-type")
             .withContentType("application/test+json")
             .withBody("""
                 {
                   "foo": "bar"
                 }
                 """)
             .post()
             .assertStatusCode(400)
             .assertJSON("""
                 {
                   "fieldErrors" : { },
                   "generalErrors" : [ {
                     "code" : "[InvalidContentType]",
                     "message" : "Invalid [Content-Type] HTTP request header value of [application/test+json]. Supported values for this request include [application/json]."
                   } ]
                 }
                  """);

    // Patch not supported on this endpoint
    simulator.test("/json-content-type-no-restriction")
             .withContentType("application/json-patch+json")
             .withBody("""
                 {
                   "foo": "bar"
                 }
                 """)
             .post()
             .assertStatusCode(400)
             .assertJSON("""
                 {
                   "fieldErrors" : { },
                   "generalErrors" : [ {
                     "code" : "[PatchNotSupported]",
                     "message" : "The [Content-Type] HTTP request header value of [application/json-patch+json] is not supported for this request."
                   } ]
                 }
                  """);

    // Missing Content-Type Header
    simulator.test("/json-content-type")
             .withContentType("")
             .withBody("""
                 {
                   "foo": "bar"
                 }
                 """)
             .post()
             .assertStatusCode(400)
             .assertJSON("""
                  {
                    "fieldErrors" : { },
                    "generalErrors" : [ {
                      "code" : "[MissingContentType]",
                      "message" : "Missing required [Content-Type] HTTP request header."
                    } ]
                  }
                 """);

    // Supported Content-Type, but invalid for this request, however, because a body is provided, it will be allowed.
    simulator.test("/json-content-type")
             .withContentType("application/x-www-form-urlencoded")
             .post()
             .assertStatusCode(200)
             .assertBodyIsEmpty();

    // Not supported in general, but this action has restrictions, so we'll get a validation error.
    simulator.test("/json-content-type")
             .withContentType("application/klingon")
             .withBody("""
                 {
                   "foo": "bar"
                 }
                 """)
             .post()
             .assertStatusCode(400)
             .assertJSON("""
                 {
                   "fieldErrors" : { },
                   "generalErrors" : [ {
                     "code" : "[InvalidContentType]",
                     "message" : "Invalid [Content-Type] HTTP request header value of [application/klingon]. Supported values for this request include [application/json]."
                   } ]
                 }
                  """);

    // Not supported in general
    simulator.test("/json-content-type-no-restriction")
             .withContentType("application/klingon")
             .withBody("""
                 {
                   "foo": "bar"
                 }
                 """)
             .post()
             .assertStatusCode(400)
             .assertJSON("""
                  {
                    "fieldErrors" : { },
                    "generalErrors" : [ {
                      "code" : "[UnsupportedContentType]",
                          "message" : "Unsupported [Content-Type] HTTP request header value of [application/klingon]."
                    } ]
                  }
                 """);

    // Not supported in general, URL does not exist, a fuzzer. Expect a 404
    simulator.test("/hack-the-planet")
             .withContentType("application/klingon")
             .withBody("""
                 {
                   "foo": "bar"
                 }
                 """)
             .post()
             .assertStatusCode(404)
             .assertBodyContains("The page is missing!");
  }

  @Test
  public void post_lotsOfMessagesFromKeys() throws Exception {
    test.simulate(() -> simulator.test("/lots-of-messages")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContainsMessagesFromKeys(
                                     "message1",
                                     "message2",
                                     "message3",
                                     "message4",
                                     "message5",
                                     "message6",
                                     "message7",
                                     "message8",
                                     "message9",
                                     "message10"));

    // Once for the API call and another for the message lookup
    assertEquals(LotsOfMessagesAction.invocationCount.get(), 2);
  }

  @Test
  public void post_namedParameters() {
    // Use annotation on a field and method
    simulator.test("/named-parameter-handler")
             .withParameter("x-field", "value-field-a")
             .withParameter("x-method", "value-method-a")
             .withParameter("secondField", "value-field-b")
             .withParameter("methodB", "value-method-b")
             .post()
             .assertStatusCode(200)
             .assertBodyContains("""
                 fieldA:value-field-a
                 fieldB:value-field-b
                 methodA:value-method-a
                 methodB:value-method-b
                 """);
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
                                 .withFile("dataTextHtml", test.tempFile, "text/plain")
                                 .post()
                                 .assertStatusCode(400))
        .simulate(() -> simulator.test("/file-upload")
                                 .withFile("dataTextHtml", test.tempFile, "text/html")
                                 .post()
                                 .assertStatusCode(200));
  }

  @Test
  public void post_savedRequest() throws Exception {
    // Post to a page that requires authentication
    BaseStoreAction.loggedIn = false;
    test.simulate(() -> simulator.test("/store/purchase")
                                 .withParameter("item", "beer")
                                 .post()
                                 .assertStatusCode(302)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertRedirect("/store/login")

                                 // Redirected to login
                                 .executeRedirect(
                                     req -> req.assertStatusCode(200)
                                               .assertHeaderContains("Cache-Control", "no-cache")
                                               .assertBodyContains("Login")

                                               // Post on Login, get a session, and redirect back to the cart which completes the beer purchase
                                               .executeFormPostInResponseBody("form",
                                                   postReq -> postReq.assertStatusCode(302)
                                                                     .assertHeaderContains("Cache-Control", "no-cache")
                                                                     .assertRedirect("/store/purchase")

                                                                     .executeRedirect(
                                                                         redirReq -> redirReq.assertStatusCode(200).assertBodyContains("Buy:beer")))));
  }

  @Test
  public void post_savedRequest_cookieExpired_orCannotBeDeserialized() throws Exception {
    // Post to a page that requires authentication
    BaseStoreAction.loggedIn = false;
    test.simulate(() -> simulator.test("/store/purchase")
                                 .withParameter("item", "beer")
                                 .post()
                                 .assertStatusCode(302)
                                 .assertHeaderContains("Cache-Control", "no-cache")
                                 .assertRedirect("/store/login")

                                 // Redirected to login
                                 .executeRedirect(
                                     req -> req.assertStatusCode(200)
                                               .assertHeaderContains("Cache-Control", "no-cache")
                                               .assertBodyContains("Login")

                                               // "expire" the flash cookie by rolling the encryption key
                                               .setup(() -> configuration.regenerateCookieEncryptionKey())

                                               // Post on Login, get a session, and redirect back to the cart which completes the beer purchase
                                               .executeFormPostInResponseBody("form",
                                                   postReq -> postReq.assertStatusCode(302)
                                                                     .assertHeaderContains("Cache-Control", "no-cache")
                                                                     .assertRedirect("/store/")

                                                                     .executeRedirect(
                                                                         redirReq -> redirReq.assertStatusCode(200)
                                                                                             // We'll end up on the /store/index instead of completing the save request,
                                                                                             // we're logged in, but the beer purchase failed. Sad.
                                                                                             .assertBodyContains(
                                                                                                 "/store/index",
                                                                                                 "IsLoggedIn:true")
                                                                                             .assertBodyDoesNotContain("Buy:beer")))));
  }

  @Test
  public void post_scopeStorage() throws Exception {
    // Tests that the expression evaluator safely gets skipped while looking for values and Prime then checks the
    // HttpServletRequest and finds the value
    test.simulate(() -> simulator.test("/scope-storage")
                                 .post())
        .assertContextAttributeNotNull("contextObject");
  }

  @Test
  public void post_scopeStorageInBaseClass() throws Exception {
    // Set values during POST - fields exist in base abstract class
    test.simulate(() -> simulator.test("/extended-scope-storage")
                                 .post())
        .assertContextAttributeNotNull("contextObject")

        // Call [GET] -- assume everything is set from prior request except request attribute is null.
        .simulate(() -> simulator.test("/extended-scope-storage")
                                 .get())
        .assertContextAttributeNotNull("contextObject")

        // Call [GET] on a different action -- only action and context attributes come over, request and action session are null.
        .simulate(() -> simulator.test("/another-extended-scope-storage")
                                 .get())
        .assertContextAttributeNotNull("contextObject");
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
    assertSingleton(ActionConfigurationProvider.class);
    assertSingleton(Configuration.class);
    assertSingleton(ResourceBundle.Control.class);
    assertSingleton(ResourceBundle.Control.class);
    assertSingleton(ContainerResolver.class);
    assertSingleton(ConverterProvider.class);
    assertSingleton(ExpressionEvaluator.class);
    assertSingleton(URIBuilder.class);
    assertSingletonConverter(Boolean.class);
    assertSingletonConverter(boolean.class);
    assertSingletonConverter(Character.class);
    assertSingletonConverter(char.class);
    assertSingletonConverter(Number.class);
    assertSingletonConverter(int.class);
    assertSingletonConverter(long.class);
    assertSingletonConverter(double.class);
    assertSingletonConverter(float.class);
    assertSingletonConverter(BigDecimal.class);
    assertSingletonConverter(BigInteger.class);
    assertSingletonConverter(Collection.class);
    assertSingletonConverter(ZonedDateTime.class);
    assertSingletonConverter(Enum.class);
    assertSingletonConverter(File.class);
    assertSingletonConverter(LocalDate.class);
    assertSingletonConverter(Locale.class);
    assertSingletonConverter(String.class);
  }

  @Test
  public void uriParameters() throws Exception {
    test.simulate(() -> simulator.test("/complex-rest/brian/static/pontarelli/then/a/bunch/of/stuff")
                                 .post()
                                 .assertStatusCode(200)
                                 .assertBodyContains("firstName=brian", "lastName=pontarelli", "theRest=then,a,bunch,of,stuff"));
  }

  private void assertSingleton(Class<?> type) {
    assertSame(injector.getInstance(type), injector.getInstance(type));
  }

  private void assertSingletonConverter(Class<?> type) {
    // Adding a noinspection, there is a bug in the JDK that is exposed when you take the suggestion of IJ and replace this TypeLiteral type info with <>
    // Ask me how I know... see https://stackoverflow.com/questions/50885335/java-10-compilaton-null-pointer-exception
    //noinspection Convert2Diamond
    Map<Class<?>, GlobalConverter> converters = injector.getInstance(Key.get(new TypeLiteral<Map<Class<?>, GlobalConverter>>() {
    }));
    assertSame(converters.get(type), converters.get(type));
  }

  private byte[] readBytes(String path) {
    try {
      return Files.readAllBytes(Path.of(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

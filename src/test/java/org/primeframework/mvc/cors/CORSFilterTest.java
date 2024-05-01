/*
 * Copyright (c) 2022-2024, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.cors;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;

import io.fusionauth.http.HTTPMethod;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.test.RequestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

/**
 * @author Brian Pontarelli
 */
public class CORSFilterTest extends PrimeBaseTest {
  @DataProvider(name = "get_included_path_pattern")
  private static Object[][] getIncludeExcludes() {
    return new Object[][]{
        {"/foo", false},
        {"/admin/foo", true}
    };
  }

  @AfterMethod
  public void afterMethod() {
    super.afterMethod();
    corsConfiguration = null;
  }

  @BeforeMethod
  public void beforeMethod() {
    super.beforeMethod();
    corsConfiguration = new CORSConfiguration().withAllowCredentials(true)
                                               .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                               .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                               .withAllowedOrigins(URI.create("*"))
                                               .withExcludedPathPattern(Pattern.compile(("^/account.*|^/admin.*|^/support.*|^/ajax.*|^/css/.*|^/fonts/.*|^/images/.*|^/js/.*")))
                                               .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                               .withPreflightMaxAgeInSeconds(1800);
  }

  @DataProvider(name = "excludedURIs")
  public Object[][] excludedURIs() {
    return new Object[][]{
        // One URI for each of the excluded URI paths
        {"/admin/foo"},
        {"/admin/nested/foo"},
        {"/ajax/foo"}
    };
  }

  @Test
  public void get() throws Exception {
    simulator.test("/api/status")
             .withHeader("Origin", "https://jackinthebox.com")
             .get()
             .assertStatusCode(200)
             .assertHeaderContains("Access-Control-Allow-Credentials", "true")
             .assertHeaderContains("Access-Control-Expose-Headers", "Access-Control-Allow-Origin,Access-Control-Allow-Credentials")
             .assertHeaderContains("Access-Control-Allow-Origin", "https://jackinthebox.com")
             .assertHeaderContains("Vary", "Origin")
             .assertHeaderDoesNotContain("Access-Control-Allow-Methods")
             .assertHeaderDoesNotContain("Access-Control-Allow-Headers")
             .assertHeaderDoesNotContain("Access-Control-Max-Age");
  }

  @Test
  public void get_reactNativeOrigin() throws Exception {
    simulator.test("/api/status")
             .withHeader("Origin", "file://foo/index.html")
             .get()
             .assertStatusCode(200)
             .assertHeaderContains("Access-Control-Allow-Credentials", "true")
             .assertHeaderContains("Access-Control-Expose-Headers", "Access-Control-Allow-Origin,Access-Control-Allow-Credentials")
             .assertHeaderContains("Access-Control-Allow-Origin", "file://foo/index.html")
             .assertHeaderContains("Vary", "Origin")
             .assertHeaderDoesNotContain("Access-Control-Allow-Methods")
             .assertHeaderDoesNotContain("Access-Control-Allow-Headers")
             .assertHeaderDoesNotContain("Access-Control-Max-Age");


    // Handle file://, see https://github.com/FusionAuth/fusionauth-issues/issues/414 & https://bz.apache.org/bugzilla/show_bug.cgi?id=60008
    simulator.test("/api/status")
             .withHeader("Origin", "file://")
             .get()
             .assertStatusCode(200)
             .assertHeaderContains("Access-Control-Allow-Credentials", "true")
             .assertHeaderContains("Access-Control-Expose-Headers", "Access-Control-Allow-Origin,Access-Control-Allow-Credentials")
             .assertHeaderContains("Access-Control-Allow-Origin", "file://")
             .assertHeaderContains("Vary", "Origin")
             .assertHeaderDoesNotContain("Access-Control-Allow-Methods")
             .assertHeaderDoesNotContain("Access-Control-Allow-Headers")
             .assertHeaderDoesNotContain("Access-Control-Max-Age");
  }

  @Test
  public void get_sameOrigin() throws Exception {
    // Remove all HTTP methods from the CORS configuration, this would otherwise brick the action, but it is same-site
    corsConfiguration.allowedOrigins.clear();

    var response = simulator.test("/api/status")
                            .withHeader("Origin", "http://localhost:9080")
                            .get()
                            .assertStatusCode(200);

    assertNoCORSHeaders(response);

    // Same request, different origin - allowed origin, but still missing HTTP POST method from the config, expecting 403
    corsConfiguration.withAllowedOrigins(URI.create("http://jackinthebox.com"))
                     .withAllowedMethods(HTTPMethod.GET);
    response = simulator.test("/api/status")
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Origin", "http://jackinthebox.com")
                        .post()
                        .assertStatusCode(403);
    assertNoCORSHeaders(response);
  }

  @Test
  public void get_validateDisallowedMethod() throws Exception {
    corsConfiguration.withAllowedMethods(HTTPMethod.POST)
                     .withAllowedOrigins(URI.create("*"));
    assertUnauthorized();
  }

  @Test
  public void get_validateDisallowedOrigin() throws Exception {
    corsConfiguration.withAllowedMethods(HTTPMethod.GET)
                     .withAllowedOrigins(URI.create("http://foo.com"), URI.create("https://bar.com"));
    assertUnauthorized();
  }

  @Test(dataProvider = "excludedURIs")
  public void get_validateExcludedURIs(String uri) throws Exception {
    // Ensure we receive a 200 without the 'Access-Control-Allow-Origin' header in the response
    // - No origin header means this is not a CORS request
    var response = simulator.test(uri)
                            .get()
                            .assertStatusCode(200);

    assertNoCORSHeaders(response);

    // Origin header, this is a Simple CORS request, ensure we get a 200 with the correct CORS headers
    response = simulator.test(uri)
                        .withHeader("Origin", "http://foo.com")
                        .get()
                        .assertStatusCode(200);
    assertNoCORSHeaders(response);
  }

  @Test
  public void included_and_excluded_uri_checker_supplied() {
    var exception = expectThrows(IllegalStateException.class, () -> new CORSConfiguration().withAllowCredentials(true)
                                                                                           .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                                                                           .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                                                                           .withAllowedOrigins(URI.create("*"))
                                                                                           .withIncludeURIPredicate(s -> true)
                                                                                           .withExcludeURIPredicate(s -> true)
                                                                                           .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                                                                           .withPreflightMaxAgeInSeconds(1800));
    assertEquals(exception.getMessage(),
                 "You cannot use both withIncludeURIPredicate and withExcludeURIPredicate. You must use one or the other.");
  }

  @Test
  public void included_path_and_excluded_path_supplied() {
    // arrange + act + assert
    var exception = expectThrows(IllegalStateException.class, () -> new CORSConfiguration().withAllowCredentials(true)
                                                                                           .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                                                                           .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                                                                           .withAllowedOrigins(URI.create("*"))
                                                                                           .withExcludedPathPattern(Pattern.compile("does not matter"))
                                                                                           .withIncludedPathPattern(Pattern.compile("does not matter"))
                                                                                           .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                                                                           .withPreflightMaxAgeInSeconds(1800));
    assertEquals(exception.getMessage(),
                 "You cannot use both withExcludedPathPattern and withIncludedPathPattern. You must use one or the other.");
  }

  @Test
  public void options() throws Exception {
    // Pass through with default configuration
    simulator.test("/api/status")
             .withHeader("Access-Control-Request-Method", "POST")
             .withHeader("Access-Control-Request-Headers", "X-FusionAuth-TenantId")
             .withHeader("Origin", "https://jackinthebox.com")
             .options()
             .assertStatusCode(204)
             .assertHeaderContains("Access-Control-Allow-Credentials", "true")
             .assertHeaderContains("Access-Control-Allow-Headers", "Accept,Access-Control-Request-Headers,Access-Control-Request-Method,Authorization,Content-Type,Last-Modified,Origin,X-FusionAuth-TenantId,X-Requested-With")
             .assertHeaderContains("Access-Control-Allow-Methods", "POST")
             .assertHeaderContains("Access-Control-Max-Age", "1800")
             .assertHeaderContains("Access-Control-Allow-Origin", "https://jackinthebox.com")
             .assertHeaderDoesNotContain("Access-Control-Expose-Headers")
             .assertHeaderContains("Vary", "Origin");
  }

  @Test(dataProvider = "get_included_path_pattern")
  public void options_excluded_uri_checker(String path, boolean expectCorsAllow) throws Exception {
    // arrange
    corsConfiguration = new CORSConfiguration().withAllowCredentials(true)
                                               .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                               .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                               .withAllowedOrigins(URI.create("*"))
                                               .withExcludeURIPredicate(u -> u.startsWith("/foo"))
                                               .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                               .withPreflightMaxAgeInSeconds(1800);

    // act + assert
    simulator.test(path)
             .withHeader("Access-Control-Request-Method", "GET")
             .withHeader("Origin", "https://jackinthebox.com")
             .options()
             .assertStatusCode(expectCorsAllow ? 204 : 403);
  }

  @Test(dataProvider = "get_included_path_pattern")
  public void options_included_path_pattern(String path, boolean expectCorsAllow) throws Exception {
    // arrange
    corsConfiguration = new CORSConfiguration().withAllowCredentials(true)
                                               .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                               .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                               .withAllowedOrigins(URI.create("*"))
                                               .withIncludedPathPattern(Pattern.compile("^/admin/foo"))
                                               .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                               .withPreflightMaxAgeInSeconds(1800);

    // act + assert
    var result = simulator.test(path)
                          .withHeader("Access-Control-Request-Method", "GET")
                          .withHeader("Origin", "https://jackinthebox.com")
                          .options()
                          .assertStatusCode(expectCorsAllow ? 204 : 403);
    if (!expectCorsAllow) {
      return;
    }
    result.assertHeaderContains("Access-Control-Allow-Credentials", "true")
          .assertHeaderContains("Access-Control-Allow-Headers", "Accept,Access-Control-Request-Headers,Access-Control-Request-Method,Authorization,Content-Type,Last-Modified,Origin,X-FusionAuth-TenantId,X-Requested-With")
          .assertHeaderContains("Access-Control-Allow-Methods", "GET")
          .assertHeaderContains("Access-Control-Max-Age", "1800")
          .assertHeaderContains("Access-Control-Allow-Origin", "https://jackinthebox.com")
          .assertHeaderDoesNotContain("Access-Control-Expose-Headers")
          .assertHeaderContains("Vary", "Origin");
  }

  @Test(dataProvider = "get_included_path_pattern")
  public void options_included_uri_checker(String path, boolean expectCorsAllow) throws Exception {
    // arrange
    corsConfiguration = new CORSConfiguration().withAllowCredentials(true)
                                               .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                               .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                               .withAllowedOrigins(URI.create("*"))
                                               .withIncludeURIPredicate(u -> u.startsWith("/admin/foo"))
                                               .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                               .withPreflightMaxAgeInSeconds(1800);

    // act + assert
    var result = simulator.test(path)
                          .withHeader("Access-Control-Request-Method", "GET")
                          .withHeader("Origin", "https://jackinthebox.com")
                          .options()
                          .assertStatusCode(expectCorsAllow ? 204 : 403);
    if (!expectCorsAllow) {
      return;
    }
    result.assertHeaderContains("Access-Control-Allow-Credentials", "true")
          .assertHeaderContains("Access-Control-Allow-Headers", "Accept,Access-Control-Request-Headers,Access-Control-Request-Method,Authorization,Content-Type,Last-Modified,Origin,X-FusionAuth-TenantId,X-Requested-With")
          .assertHeaderContains("Access-Control-Allow-Methods", "GET")
          .assertHeaderContains("Access-Control-Max-Age", "1800")
          .assertHeaderContains("Access-Control-Allow-Origin", "https://jackinthebox.com")
          .assertHeaderDoesNotContain("Access-Control-Expose-Headers")
          .assertHeaderContains("Vary", "Origin");
  }

  @Test
  public void options_validateDisallowedHeader() throws Exception {
    var response = simulator.test("/api/status")
                            .withHeader("Access-Control-Request-Method", "GET")
                            .withHeader("Access-Control-Request-Headers", "X-Foo")
                            .withHeader("Origin", "https://jackinthebox.com")
                            .options()
                            .assertStatusCode(403);

    assertNoCORSHeaders(response);
  }

  @Test
  public void options_validateDisallowedMethod() throws Exception {
    corsConfiguration.withAllowedMethods(HTTPMethod.POST);
    var response = simulator.test("/api/status")
                            .withHeader("Access-Control-Request-Method", "GET")
                            .withHeader("Origin", "https://jackinthebox.com")
                            .options()
                            .assertStatusCode(403);

    assertNoCORSHeaders(response);
  }

  @Test
  public void options_validateDisallowedOrigin() throws Exception {
    corsConfiguration.withAllowedOrigins(URI.create("http://foo.com"));
    var response = simulator.test("/api/status")
                            .withHeader("Access-Control-Request-Method", "GET")
                            .withHeader("Origin", "https://jackinthebox.com")
                            .options()
                            .assertStatusCode(403);

    assertNoCORSHeaders(response);
  }

  @Test
  public void options_validateExcludedURIs() throws Exception {
    // Options request of blocked URI pattern bypasses CORS filter and is blocked by Prime MVC with 405
    var response = simulator.test("/admin/foo")
                            .withHeader("Origin", "http://foo.com")
                            .options()
                            .assertStatusCode(405);

    assertNoCORSHeaders(response);
  }

  @Test
  public void options_validateWildcardOriginDisallowCredentials() throws Exception {
    corsConfiguration.withAllowedMethods(HTTPMethod.GET)
                     .withAllowedOrigins(URI.create("*"))
                     .withAllowCredentials(false);

    simulator.test("/api/status")
             .withHeader("Access-Control-Request-Method", "GET")
             .withHeader("Origin", "https://jackinthebox.com")
             .options()
             .assertStatusCode(204)
             .assertHeaderDoesNotContain("Access-Control-Allow-Credentials")
             .assertHeaderContains("Access-Control-Allow-Origin", "*")
             .assertHeaderContains("Access-Control-Allow-Headers", "Accept,Access-Control-Request-Headers,Access-Control-Request-Method,Authorization,Content-Type,Last-Modified,Origin,X-FusionAuth-TenantId,X-Requested-With")
             .assertHeaderContains("Access-Control-Allow-Methods", "GET")
             .assertHeaderDoesNotContain("Access-Control-Expose-Headers")
             .assertHeaderContains("Access-Control-Max-Age", "1800");
  }

  @Test
  public void path_and_checker_supplied() {
    var exception = expectThrows(IllegalStateException.class, () -> new CORSConfiguration().withAllowCredentials(true)
                                                                                           .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                                                                           .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                                                                           .withAllowedOrigins(URI.create("*"))
                                                                                           .withExcludedPathPattern(Pattern.compile("does not matter"))
                                                                                           .withExcludeURIPredicate(s -> true)
                                                                                           .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                                                                           .withPreflightMaxAgeInSeconds(1800));
    assertEquals(exception.getMessage(),
                 "You cannot use both a path (withIncludedPathPattern/withExcludedPathPattern) and predicate based (withIncludeURIPredicate/withExcludeURIPredicate). You must use one or the other.");
  }

  @Test
  public void post() throws Exception {
    simulator.test("/api/status")
             .withHeader("Content-Type", "application/json")
             .withHeader("Origin", "https://jackinthebox.com")
             .post()
             .assertStatusCode(200)
             .assertHeaderContains("Access-Control-Allow-Credentials", "true")
             .assertHeaderContains("Access-Control-Allow-Origin", "https://jackinthebox.com")
             .assertHeaderDoesNotContain("Access-Control-Allow-Headers")
             .assertHeaderDoesNotContain("Access-Control-Allow-Methods")
             .assertHeaderContains("Access-Control-Expose-Headers", "Access-Control-Allow-Origin,Access-Control-Allow-Credentials")
             .assertBodyDoesNotContain("Access-Control-Max-Age");

    // No Content-Type header currently results in an INVALID_CORS handling
    // TODO : Is this correct?
    var response = simulator.test("/api/status")
                            .withHeader("Origin", "https://jackinthebox.com")
                            .post()
                            .assertStatusCode(403);

    assertNoCORSHeaders(response);
  }

  @Test
  public void post_validateDisallowedMethod() throws Exception {
    corsConfiguration.withAllowedMethods(HTTPMethod.GET);

    var response = simulator.test("/api/status")
                            .withHeader("Origin", "https://jackinthebox.com")
                            .post()
                            .assertStatusCode(403);

    assertNoCORSHeaders(response);
  }

  @Test
  public void post_validateDisallowedOrigin() throws Exception {
    corsConfiguration.withAllowedOrigins(URI.create("http://foo.com"));

    var response = simulator.test("/api/status")
                            .withHeader("Origin", "https://jackinthebox.com")
                            .post()
                            .assertStatusCode(403);

    assertNoCORSHeaders(response);
  }

  @Test
  public void post_validateExcludedURIs_withSimpleContentType() throws Exception {
    // Skip CORS Filter on same origin request of blocked URI patterns
    // No origin header, this is not a CORS request, response should not have a CORS header
    var response = simulator.test("/admin/nested/foo")
                            .post()
                            .assertStatusCode(200);
    assertNoCORSHeaders(response);

    // With origin header, it's still excluded to the action is run
    response = simulator.test("/admin/nested/foo")
                        .withHeader("Origin", "https://jackinthebox.com")
                        .post()
                        .assertStatusCode(200);

    assertNoCORSHeaders(response);
  }

  private void assertNoCORSHeaders(RequestResult response) {
    response.assertHeaderDoesNotContain("Access-Control-Allow-Credentials")
            .assertHeaderDoesNotContain("Access-Control-Allow-Headers")
            .assertHeaderDoesNotContain("Access-Control-Allow-Methods")
            .assertHeaderDoesNotContain("Access-Control-Allow-Origin")
            .assertHeaderDoesNotContain("Access-Control-Expose-Headers")
            .assertHeaderDoesNotContain("Access-Control-Max-Age")
            .assertHeaderDoesNotContain("Vary");
  }

  private void assertUnauthorized() throws IOException, InterruptedException {
    var response = simulator.test("/api/status")
                            .withHeader("Origin", "https://jackinthebox.com")
                            .get()
                            .assertStatusCode(403);

    assertNoCORSHeaders(response);
  }
}

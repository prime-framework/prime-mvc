/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.regex.Pattern;

import io.fusionauth.http.HTTPMethod;
import org.primeframework.mvc.PrimeBaseTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.expectThrows;

/**
 * @author Brian Pontarelli
 */
public class CORSFilterTest extends PrimeBaseTest {
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
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .GET()
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 200);
    assertEquals(response.headers().firstValue("Access-Control-Allow-Credentials").orElse(null), "true");
    assertEquals(response.headers().firstValue("Access-Control-Expose-Headers").orElse(null), "Access-Control-Allow-Origin,Access-Control-Allow-Credentials");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Origin").orElse(null), "https://jackinthebox.com");
    assertEquals(response.headers().firstValue("Vary").orElse(null), "Origin");
    assertNull(response.headers().firstValue("Access-Control-Allow-Methods").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Allow-Headers").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Max-Age").orElse(null));
  }

  @Test
  public void get_reactNativeOrigin() throws Exception {
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .GET()
                   .header("Origin", "file://foo/index.html")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 200);
    assertEquals(response.headers().firstValue("Access-Control-Allow-Credentials").orElse(null), "true");
    assertEquals(response.headers().firstValue("Access-Control-Expose-Headers").orElse(null), "Access-Control-Allow-Origin,Access-Control-Allow-Credentials");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Origin").orElse(null), "file://foo/index.html");
    assertEquals(response.headers().firstValue("Vary").orElse(null), "Origin");
    assertNull(response.headers().firstValue("Access-Control-Allow-Methods").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Allow-Headers").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Max-Age").orElse(null));

    // Handle file://, see https://github.com/FusionAuth/fusionauth-issues/issues/414 & https://bz.apache.org/bugzilla/show_bug.cgi?id=60008
    response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .GET()
                   .header("Origin", "file://")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 200);
    assertEquals(response.headers().firstValue("Access-Control-Allow-Credentials").orElse(null), "true");
    assertEquals(response.headers().firstValue("Access-Control-Expose-Headers").orElse(null), "Access-Control-Allow-Origin,Access-Control-Allow-Credentials");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Origin").orElse(null), "file://");
    assertEquals(response.headers().firstValue("Vary").orElse(null), "Origin");
    assertNull(response.headers().firstValue("Access-Control-Allow-Methods").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Allow-Headers").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Max-Age").orElse(null));
  }

  @Test
  public void get_sameOrigin() throws Exception {
    // Remove all HTTP methods from the CORS configuration, this would otherwise brick the action, but it is same-site
    corsConfiguration.allowedOrigins.clear();

    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .GET()
                   .header("Origin", "http://localhost:9080")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 200);
    assertNoCORSHeaders(response);

    // Same request, different origin - allowed origin, but still missing HTTP POST method from the config, expecting 403
    corsConfiguration.withAllowedOrigins(URI.create("http://jackinthebox.com"))
                     .withAllowedMethods(HTTPMethod.GET);
    response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .POST(BodyPublishers.noBody())
                   .header("Content-Type", "application/json")
                   .header("Origin", "http://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 403);
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
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080" + uri))
                   .GET()
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 200);
    assertNoCORSHeaders(response);

    // Origin header, this is a Simple CORS request, ensure we get a 200 with the correct CORS headers
    response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080" + uri))
                   .GET()
                   .header("Origin", "http://foo.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 200);
    assertNoCORSHeaders(response);
  }

  @DataProvider(name = "get_included_path_pattern")
  private static Object[][] getIncludeExcludes() {
    return new Object[][]{
        {"/foo", false},
        {"/admin/foo", true}
    };
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

    // act
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080" + path))
                   .method("OPTIONS", BodyPublishers.noBody())
                   .header("Access-Control-Request-Method", "GET")
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );

    // assert
    var expectedStatus = expectCorsAllow ? 204 : 403;
    assertEquals(response.statusCode(), expectedStatus);
    if (!expectCorsAllow) {
      return;
    }
    assertEquals(response.headers().firstValue("Access-Control-Allow-Credentials").orElse(null), "true");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Headers").orElse(null), "Accept,Access-Control-Request-Headers,Access-Control-Request-Method,Authorization,Content-Type,Last-Modified,Origin,X-FusionAuth-TenantId,X-Requested-With");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Methods").orElse(null), "GET");
    assertEquals(response.headers().firstValue("Access-Control-Max-Age").orElse(null), "1800");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Origin").orElse(null), "https://jackinthebox.com");
    assertNull(response.headers().firstValue("Access-Control-Expose-Headers").orElse(null));
    assertEquals(response.headers().firstValue("Vary").orElse(null), "Origin");
  }

  @Test(dataProvider = "get_included_path_pattern")
  public void options_included_uri_checker(String path, boolean expectCorsAllow) throws Exception {
    // arrange
    corsConfiguration = new CORSConfiguration().withAllowCredentials(true)
                                               .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                               .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                               .withAllowedOrigins(URI.create("*"))
                                               .withIncludeUriPredicate(u -> u.startsWith("/admin/foo"))
                                               .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                               .withPreflightMaxAgeInSeconds(1800);

    // act
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080" + path))
                   .method("OPTIONS", BodyPublishers.noBody())
                   .header("Access-Control-Request-Method", "GET")
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );

    // assert
    var expectedStatus = expectCorsAllow ? 204 : 403;
    assertEquals(response.statusCode(), expectedStatus);
    if (!expectCorsAllow) {
      return;
    }
    assertEquals(response.headers().firstValue("Access-Control-Allow-Credentials").orElse(null), "true");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Headers").orElse(null), "Accept,Access-Control-Request-Headers,Access-Control-Request-Method,Authorization,Content-Type,Last-Modified,Origin,X-FusionAuth-TenantId,X-Requested-With");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Methods").orElse(null), "GET");
    assertEquals(response.headers().firstValue("Access-Control-Max-Age").orElse(null), "1800");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Origin").orElse(null), "https://jackinthebox.com");
    assertNull(response.headers().firstValue("Access-Control-Expose-Headers").orElse(null));
    assertEquals(response.headers().firstValue("Vary").orElse(null), "Origin");
  }

  @Test(dataProvider = "get_included_path_pattern")
  public void options_excluded_uri_checker(String path, boolean expectCorsAllow) throws Exception {
    // arrange
    corsConfiguration = new CORSConfiguration().withAllowCredentials(true)
                                               .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                               .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                               .withAllowedOrigins(URI.create("*"))
                                               .withExcludeUriPredicate(u -> u.startsWith("/foo"))
                                               .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                               .withPreflightMaxAgeInSeconds(1800);

    // act
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080" + path))
                   .method("OPTIONS", BodyPublishers.noBody())
                   .header("Access-Control-Request-Method", "GET")
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );

    // assert
    var expectedStatus = expectCorsAllow ? 204 : 403;
    assertEquals(response.statusCode(), expectedStatus);
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
  public void included_and_excluded_uri_checker_supplied() {
    var exception = expectThrows(IllegalStateException.class, () -> new CORSConfiguration().withAllowCredentials(true)
                                                                                           .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                                                                           .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                                                                           .withAllowedOrigins(URI.create("*"))
                                                                                           .withIncludeUriPredicate(s -> true)
                                                                                           .withExcludeUriPredicate(s -> true)
                                                                                           .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                                                                           .withPreflightMaxAgeInSeconds(1800));
    assertEquals(exception.getMessage(),
                 "You cannot use both withIncludeUriPredicate and withExcludeUriPredicate. You must use one or the other.");
  }

  @Test
  public void path_and_checker_supplied() {
    var exception = expectThrows(IllegalStateException.class, () -> new CORSConfiguration().withAllowCredentials(true)
                                                                                           .withAllowedMethods(HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS, HTTPMethod.PUT, HTTPMethod.DELETE)
                                                                                           .withAllowedHeaders("Accept", "Access-Control-Request-Headers", "Access-Control-Request-Method", "Authorization", "Content-Type", "Last-Modified", "Origin", "X-FusionAuth-TenantId", "X-Requested-With")
                                                                                           .withAllowedOrigins(URI.create("*"))
                                                                                           .withExcludedPathPattern(Pattern.compile("does not matter"))
                                                                                           .withExcludeUriPredicate(s -> true)
                                                                                           .withExposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                                                                                           .withPreflightMaxAgeInSeconds(1800));
    assertEquals(exception.getMessage(),
                 "You cannot use both a path (withIncludedPathPattern/withExcludedPathPattern) and predicate based (withIncludeUriPredicate/withExcludeUriPredicate). You must use one or the other.");
  }

  @Test
  public void options() throws Exception {
    // Pass through with default configuration
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .method("OPTIONS", BodyPublishers.noBody())
                   .header("Access-Control-Request-Method", "POST")
                   .header("Access-Control-Request-Headers", "X-FusionAuth-TenantId")
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 204);
    assertEquals(response.headers().firstValue("Access-Control-Allow-Credentials").orElse(null), "true");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Headers").orElse(null), "Accept,Access-Control-Request-Headers,Access-Control-Request-Method,Authorization,Content-Type,Last-Modified,Origin,X-FusionAuth-TenantId,X-Requested-With");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Methods").orElse(null), "POST");
    assertEquals(response.headers().firstValue("Access-Control-Max-Age").orElse(null), "1800");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Origin").orElse(null), "https://jackinthebox.com");
    assertNull(response.headers().firstValue("Access-Control-Expose-Headers").orElse(null));
    assertEquals(response.headers().firstValue("Vary").orElse(null), "Origin");
  }

  @Test
  public void options_validateDisallowedHeader() throws Exception {
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .method("OPTIONS", BodyPublishers.noBody())
                   .header("Access-Control-Request-Method", "GET")
                   .header("Access-Control-Request-Headers", "X-Foo")
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 403);
    assertNoCORSHeaders(response);
  }

  @Test
  public void options_validateDisallowedMethod() throws Exception {
    corsConfiguration.withAllowedMethods(HTTPMethod.POST);
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .method("OPTIONS", BodyPublishers.noBody())
                   .header("Access-Control-Request-Method", "GET")
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 403);
    assertNoCORSHeaders(response);
  }

  @Test
  public void options_validateDisallowedOrigin() throws Exception {
    corsConfiguration.withAllowedOrigins(URI.create("http://foo.com"));
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .method("OPTIONS", BodyPublishers.noBody())
                   .header("Access-Control-Request-Method", "GET")
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 403);
    assertNoCORSHeaders(response);
  }

  @Test
  public void options_validateExcludedURIs() throws Exception {
    // Options request of blocked URI pattern bypasses CORS filter and is blocked by Prime MVC with 405
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/admin/foo"))
                   .method("OPTIONS", BodyPublishers.noBody())
                   .header("Origin", "http://foo.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 405);
    assertNoCORSHeaders(response);
  }

  @Test
  public void options_validateWildcardOriginDisallowCredentials() throws Exception {
    corsConfiguration.withAllowedMethods(HTTPMethod.GET)
                     .withAllowedOrigins(URI.create("*"))
                     .withAllowCredentials(false);
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .method("OPTIONS", BodyPublishers.noBody())
                   .header("Access-Control-Request-Method", "GET")
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 204);
    assertNull(response.headers().firstValue("Access-Control-Allow-Credentials").orElse(null));
    assertEquals(response.headers().firstValue("Access-Control-Allow-Origin").orElse(null), "*");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Headers").orElse(null), "Accept,Access-Control-Request-Headers,Access-Control-Request-Method,Authorization,Content-Type,Last-Modified,Origin,X-FusionAuth-TenantId,X-Requested-With");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Methods").orElse(null), "GET");
    assertNull(response.headers().firstValue("Access-Control-Expose-Headers").orElse(null));
    assertEquals(response.headers().firstValue("Access-Control-Max-Age").orElse(null), "1800");
  }

  @Test
  public void post() throws Exception {
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .POST(BodyPublishers.noBody())
                   .header("Content-Type", "application/json")
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 200);
    assertEquals(response.headers().firstValue("Access-Control-Allow-Credentials").orElse(null), "true");
    assertEquals(response.headers().firstValue("Access-Control-Allow-Origin").orElse(null), "https://jackinthebox.com");
    assertNull(response.headers().firstValue("Access-Control-Allow-Headers").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Allow-Methods").orElse(null));
    assertEquals(response.headers().firstValue("Access-Control-Expose-Headers").orElse(null), "Access-Control-Allow-Origin,Access-Control-Allow-Credentials");
    assertNull(response.headers().firstValue("Access-Control-Max-Age").orElse(null));

    // No Content-Type header currently results in an INVALID_CORS handling
    // TODO : Is this correct?
    response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .POST(BodyPublishers.noBody())
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 403);
    assertNoCORSHeaders(response);
  }

  @Test
  public void post_validateDisallowedMethod() throws Exception {
    corsConfiguration.withAllowedMethods(HTTPMethod.GET);

    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .POST(BodyPublishers.noBody())
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 403);
    assertNoCORSHeaders(response);
  }

  @Test
  public void post_validateDisallowedOrigin() throws Exception {
    corsConfiguration.withAllowedOrigins(URI.create("http://foo.com"));

    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .POST(BodyPublishers.noBody())
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 403);
    assertNoCORSHeaders(response);
  }

  @Test
  public void post_validateExcludedURIs_withSimpleContentType() throws Exception {
    // Skip CORS Filter on same origin request of blocked URI patterns
    // No origin header, this is not a CORS request, response should not have a CORS header
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/admin/nested/foo"))
                   .POST(BodyPublishers.noBody())
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 200);
    assertNoCORSHeaders(response);

    // With origin header, it's still excluded to the action is run
    response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/admin/nested/foo"))
                   .POST(BodyPublishers.noBody())
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 200);
    assertNoCORSHeaders(response);
  }

  private void assertNoCORSHeaders(HttpResponse<Void> response) {
    assertNull(response.headers().firstValue("Access-Control-Allow-Credentials").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Allow-Headers").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Allow-Methods").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Expose-Headers").orElse(null));
    assertNull(response.headers().firstValue("Access-Control-Max-Age").orElse(null));
    assertNull(response.headers().firstValue("Vary").orElse(null));
  }

  private void assertUnauthorized() throws IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    HttpResponse<Void> response = client.send(
        HttpRequest.newBuilder(URI.create("http://localhost:9080/api/status"))
                   .GET()
                   .header("Origin", "https://jackinthebox.com")
                   .build(),
        BodyHandlers.discarding()
    );
    assertEquals(response.statusCode(), 403);
    assertNoCORSHeaders(response);
  }
}

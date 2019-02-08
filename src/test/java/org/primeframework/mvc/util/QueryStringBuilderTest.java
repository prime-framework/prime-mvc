/*
 * Copyright (c) 2019, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.util;

import java.util.function.Consumer;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Daniel DeGroff
 */
public class QueryStringBuilderTest {
  @Test
  public void testBuilder() {
    // Just parms
    test(b -> b.with("foo", "bar"), "foo=bar");
    test(b -> b.with("foo", "bar").with("bing", "baz"), "foo=bar&bing=baz");
    test(b -> b.beginQuery().with("foo", "bar").with("bing", "baz"), "?foo=bar&bing=baz");
    test(b -> b.beginFragment().with("foo", "bar").with("bing", "baz"), "#foo=bar&bing=baz");
    test(b -> b.beginQuery().with("foo", "bar").with("bing", "baz").beginFragment().with("joe", "mama").with("boom", "dynamite"), "?foo=bar&bing=baz#joe=mama&boom=dynamite");

    // With a URL
    test("http://acme.com", b -> b.with("foo", "bar"), "http://acme.com?foo=bar");
    test("http://acme.com?foo=bar", b -> b.with("bing", "baz"), "http://acme.com?foo=bar&bing=baz");
    test("http://acme.com?foo=bar&bing=baz", b -> b.with("boom", "dynamite"), "http://acme.com?foo=bar&bing=baz&boom=dynamite");

    // With a URL and a fragment
    test("http://acme.com", b -> b.beginFragment().with("foo", "bar"), "http://acme.com#foo=bar");
    test("http://acme.com#foo=bar", b -> b.with("bing", "baz"), "http://acme.com#foo=bar&bing=baz");
    test("http://acme.com#foo=bar&bing=baz", b -> b.with("boom", "dynamite"), "http://acme.com#foo=bar&bing=baz&boom=dynamite");

    // Query and a fragment, the fragment always follows the query
    test("http://acme.com", b -> b.beginQuery().with("foo", "bar").beginFragment().with("bing", "baz"), "http://acme.com?foo=bar#bing=baz");
    test("http://acme.com", b -> b.beginQuery().with("foo", "bar").with("bing", "baz")
                                  .beginFragment().with("joe", "mama").with("lorem", "ipsum"), "http://acme.com?foo=bar&bing=baz#joe=mama&lorem=ipsum");
    test("http://acme.com?foo=bar", b -> b.beginFragment().with("bing", "baz"), "http://acme.com?foo=bar#bing=baz");

    // Whole URLs w/out a builder with both query and fragment parts
    test("http://acme.com?foo=bar#bing=baz", "http://acme.com?foo=bar#bing=baz");
    test("http://acme.com?foo=bar&lorem=ipsum#bing=baz&joe=mama", "http://acme.com?foo=bar&lorem=ipsum#bing=baz&joe=mama");
  }

  private void test(String uri, String expected) {
    QueryStringBuilder builder = QueryStringBuilder.builder(uri);
    assertEquals(builder.toString(), expected);
  }

  private void test(String uri, Consumer<QueryStringBuilder> consumer, String expected) {
    QueryStringBuilder builder = QueryStringBuilder.builder(uri);
    consumer.accept(builder);
    assertEquals(builder.toString(), expected);
  }

  private void test(Consumer<QueryStringBuilder> consumer, String expected) {
    QueryStringBuilder builder = QueryStringBuilder.builder();
    consumer.accept(builder);
    assertEquals(builder.toString(), expected);
  }
}

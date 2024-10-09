/*
 * Copyright (c) 2019-2024, Inversoft Inc., All Rights Reserved
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
import static org.testng.Assert.fail;

/**
 * @author Daniel DeGroff
 */
public class QueryStringBuilderTest {
  @Test
  public void one() {
    test("http://acme.com", b -> b.with("foo", "bar"), "http://acme.com?foo=bar");
  }

  @Test
  public void testBuilder() {
    // Just parms
    test(null, b -> b.with("foo", "bar"), "foo=bar");
    test(b -> b.with("foo", "bar"), "foo=bar");
    test(b -> b.with("foo", "bar").with("bing", "baz"), "foo=bar&bing=baz");
    test(b -> b.beginQuery().with("foo", "bar").with("bing", "baz"), "?foo=bar&bing=baz");
    test(b -> b.beginFragment().with("foo", "bar").with("bing", "baz"), "#foo=bar&bing=baz");
    test(null, b -> b.beginQuery().with("foo", "bar").with("bing", "baz").beginFragment().with("joe", "mama").with("boom", "dynamite"), "?foo=bar&bing=baz#joe=mama&boom=dynamite");
    test(b -> b.beginQuery().with("foo", "bar").with("bing", "baz").beginFragment().with("joe", "mama").with("boom", "dynamite"), "?foo=bar&bing=baz#joe=mama&boom=dynamite");

    // With a URL
    test("http://acme.com", b -> b.with("foo", "bar"), "http://acme.com?foo=bar");
    test("http://acme.com?foo=bar", b -> b.with("bing", "baz"), "http://acme.com?foo=bar&bing=baz");
    test("http://acme.com?foo=bar&bing=baz", b -> b.with("boom", "dynamite"), "http://acme.com?foo=bar&bing=baz&boom=dynamite");

    // Null parameter
    test("http://acme.com", b -> b.with("foo", (String) null), "http://acme.com");

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

    // Add some URL segments into the mix, some with trailing slash
    test("http://acme.com", b -> b.withSegment("bar").with("foo", "bar"), "http://acme.com/bar?foo=bar");
    test("http://acme.com/", b -> b.withSegment("bar").with("foo", "bar"), "http://acme.com/bar?foo=bar");
    test("http://acme.com/", b -> b.withSegment("bar").withSegment(null).with("foo", "bar"), "http://acme.com/bar?foo=bar");
    test("http://acme.com/", b -> b.withSegment("bar").withSegment("baz").with("foo", "bar"), "http://acme.com/bar/baz?foo=bar");
    test("http://acme.com", b -> b.withSegment("bar").beginQuery().with("foo", "bar").beginFragment().with("bing", "baz"), "http://acme.com/bar?foo=bar#bing=baz");
    test("http://acme.com/", b -> b.withSegment("bar").beginQuery().with("foo", "bar").beginFragment().with("bing", "baz"), "http://acme.com/bar?foo=bar#bing=baz");
    test("http://acme.com/", b -> b.withSegment("bar").withSegment("baz").beginQuery().with("foo", "bar").beginFragment().with("bing", "baz"), "http://acme.com/bar/baz?foo=bar#bing=baz");

    // URL with query term, adding another term
    test("http://acme.com?test=data", b -> b.with("foo", "bar"), "http://acme.com?test=data&foo=bar");
    test("http://acme.com?test=data", b -> b.beginQuery().with("foo", "bar"), "http://acme.com?test=data&foo=bar");

    // URL with fragment, adding more fragment terms
    test("http://acme.com#frag", b -> b.with("foo", "bar"), "http://acme.com#frag&foo=bar");
    test("http://acme.com#frag", b -> b.with("foo", "bar").with("bing", "baz"), "http://acme.com#frag&foo=bar&bing=baz");
    test("http://acme.com#frag", b -> b.beginFragment().with("foo", "bar").with("bing", "baz"), "http://acme.com#frag&foo=bar&bing=baz");

    // Should not include query since no terms were added before beginFragment
    test("http://acme.com", b -> b.beginQuery().beginFragment().with("foo", "bar"), "http://acme.com#foo=bar");

    // URLs contain trailing control characters
    test("http://acme.com?", b -> b.with("foo", "bar"), "http://acme.com?foo=bar");
    test("http://acme.com#test&", b -> b.beginFragment().with("foo", "bar"), "http://acme.com#test&foo=bar");
    test("http://acme.com?test=data&", b -> b.beginQuery().with("foo", "bar"), "http://acme.com?test=data&foo=bar");

    // Begin query and fragement but add no terms
    test("http://acme.com", QueryStringBuilder::beginQuery, "http://acme.com");
    test("http://acme.com", QueryStringBuilder::beginFragment, "http://acme.com");

    try {
      test("http://acme.com?test=data", b -> b.withSegment("bar").with("foo", "bar"), "http://acme.com/bar?test=data&foo=bar");
      fail("Expected this to fail");
    } catch (Exception e) {
      assertEquals(e.getMessage(), "You cannot add a URL segment after you have appended a ? to the end of the URL");
    }

    try {
      test("http://acme.com#frag", b -> b.withSegment("bar").with("foo", "bar"), "http://acme.com/bar#frag");
      fail("Expected this to fail");
    } catch (Exception e) {
      assertEquals(e.getMessage(), "You cannot add a URL segment after you have appended a # to the end of the URL");
    }

    try {
      test("http://acme.com#frag", b -> b.beginQuery().with("foo", "bar"), "http://acme.com/bar");
      fail("Expected this to fail");
    } catch (Exception e) {
      assertEquals(e.getMessage(), "You cannot add a query after a fragment");
    }

    // Expect to explode, if you leave a ? at the end of the initial URL you can't have any segments
    try {
      test("http://acme.com?", b -> b.withSegment("bar").with("foo", "bar"), "http://acme.com?foo=bar");
      fail("Expected this to fail");
    } catch (IllegalStateException ignore) {
    }
  }

  private void test(String uri, String expected) {
    QueryStringBuilder builder = QueryStringBuilder.builder(uri);
    assertEquals(builder.build(), expected);
  }

  private void test(String uri, Consumer<QueryStringBuilder> consumer, String expected) {
    QueryStringBuilder builder = QueryStringBuilder.builder(uri);
    consumer.accept(builder);
    assertEquals(builder.build(), expected);
  }

  private void test(Consumer<QueryStringBuilder> consumer, String expected) {
    QueryStringBuilder builder = QueryStringBuilder.builder();
    consumer.accept(builder);
    assertEquals(builder.build(), expected);
  }
}

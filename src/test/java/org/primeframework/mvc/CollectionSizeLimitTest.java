/*
 * Copyright (c) 2026, Inversoft Inc., All Rights Reserved
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

import org.testng.annotations.Test;

/**
 * Tests collection size limit enforcement across various parameter input styles: repeated query params,
 * indexed params (e.g. field[0], field[1]), comma-delimited values, and POST body params.
 */
public class CollectionSizeLimitTest extends PrimeBaseTest {
  @Test
  public void repeatedQueryParams_withinLimit() throws Exception {
    // Repeated query params backing to a list succeed
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesList", "a")
        .withURLParameter("namesList", "b")
        .withURLParameter("namesList", "c")
        .get()
        .assertStatusCode(200));
  }

  @Test
  public void repeatedQueryParams_atLimit() throws Exception {
    // Repeated query params backing to a list succeed at the limit
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesList", "a")
        .withURLParameter("namesList", "b")
        .withURLParameter("namesList", "c")
        .withURLParameter("namesList", "d")
        .withURLParameter("namesList", "e")
        .withURLParameter("namesList", "f")
        .withURLParameter("namesList", "g")
        .withURLParameter("namesList", "h")
        .withURLParameter("namesList", "i")
        .withURLParameter("namesList", "j")
        .get()
        .assertStatusCode(200));
  }

  @Test
  public void repeatedQueryParams_exceedsLimit() throws Exception {
    // Repeated query params backing to a list fail when over the max
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesList", "a")
        .withURLParameter("namesList", "b")
        .withURLParameter("namesList", "c")
        .withURLParameter("namesList", "d")
        .withURLParameter("namesList", "e")
        .withURLParameter("namesList", "f")
        .withURLParameter("namesList", "g")
        .withURLParameter("namesList", "h")
        .withURLParameter("namesList", "i")
        .withURLParameter("namesList", "j")
        .withURLParameter("namesList", "k")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void repeatedQueryParams_stringArray_exceedsLimit() throws Exception {
    // String[] with repeated params also enforces the limit
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesArray", "a")
        .withURLParameter("namesArray", "b")
        .withURLParameter("namesArray", "c")
        .withURLParameter("namesArray", "d")
        .withURLParameter("namesArray", "e")
        .withURLParameter("namesArray", "f")
        .withURLParameter("namesArray", "g")
        .withURLParameter("namesArray", "h")
        .withURLParameter("namesArray", "i")
        .withURLParameter("namesArray", "j")
        .withURLParameter("namesArray", "k")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void indexedQueryParams_withinLimit() throws Exception {
    // Indexed query parameter works
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesArray[0]", "foo")
        .withURLParameter("namesArray[1]", "bar")
        .withURLParameter("namesArray[2]", "baz")
        .get()
        .assertStatusCode(200));
  }

  @Test
  public void indexedQueryParams_atLimit() throws Exception {
    // Indexed query parameter works
    // Index 9 means array length 10 — exactly at the limit
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesArray[0]", "a")
        .withURLParameter("namesArray[9]", "j")
        .get()
        .assertStatusCode(200));
  }

  @Test
  public void indexedQueryParams_exceedsLimit() throws Exception {
    // Indexed query parameter works, fails if exceeded
    // Index 10 means array length 11 — exceeds limit of 10
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesArray[0]", "foo")
        .withURLParameter("namesArray[1]", "bar")
        .withURLParameter("namesArray[10]", "boom")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void indexedQueryParams_sparseWithGap_exceedsLimit() throws Exception {
    // Only 3 values provided, but index 10 forces array of length 11
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesArray[0]", "a")
        .withURLParameter("namesArray[5]", "b")
        .withURLParameter("namesArray[10]", "c")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void indexedQueryParams_singleHighIndex_exceedsLimit() throws Exception {
    // A single param at a high index — still exceeds the limit
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesArray[500]", "boom")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void commaDelimited_withinLimit() throws Exception {
    // List of ids succeed when added as a parameter to a backing id that isn't a string
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("ids", "1,2,3,4,5")
        .get()
        .assertStatusCode(200));
  }

  @Test
  public void commaDelimited_exceedsLimit() throws Exception {
    // List of ids fail when added as a parameter to a backing id that isn't a string
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("ids", "1,2,3,4,5,6,7,8,9,10,11")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void commaDelimited_stringBacking() throws Exception {
    // String[] backing does not split comma-delimited values; treated as a single element, so this should succeed
    test.simulate(() -> simulator.test("/collection-size-limit")
            .withURLParameter("namesArray", "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25")
            .get()
            .assertStatusCode(200));
  }

  @Test
  public void listField_repeatedParams_withinLimit() throws Exception {
    // Repeated parameters succeed when under limit
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesList", "Alice")
        .withURLParameter("namesList", "Bob")
        .withURLParameter("namesList", "Charlie")
        .get()
        .assertStatusCode(200));
  }

  @Test
  public void listField_repeatedParams_exceedsLimit() throws Exception {
    // Too many parameters fails on limit
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesList", "a")
        .withURLParameter("namesList", "b")
        .withURLParameter("namesList", "c")
        .withURLParameter("namesList", "d")
        .withURLParameter("namesList", "e")
        .withURLParameter("namesList", "f")
        .withURLParameter("namesList", "g")
        .withURLParameter("namesList", "h")
        .withURLParameter("namesList", "i")
        .withURLParameter("namesList", "j")
        .withURLParameter("namesList", "k")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void listField_indexedParams_withinLimit() throws Exception {
    // URL parameter with index should pass if under limit
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesList[0]", "Alice")
        .withURLParameter("namesList[1]", "Bob")
        .get()
        .assertStatusCode(200));
  }

  @Test
  public void listField_indexedParams_exceedsLimit() throws Exception {
    // URL parameter with index should fail if over limit
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesList[0]", "Alice")
        .withURLParameter("namesList[10]", "Boom")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void postBody_repeatedParams_exceedsLimit() throws Exception {
    // Repeated parameter over limit fails
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withParameter("namesList", "a")
        .withParameter("namesList", "b")
        .withParameter("namesList", "c")
        .withParameter("namesList", "d")
        .withParameter("namesList", "e")
        .withParameter("namesList", "f")
        .withParameter("namesList", "g")
        .withParameter("namesList", "h")
        .withParameter("namesList", "i")
        .withParameter("namesList", "j")
        .withParameter("namesList", "k")
        .post()
        .assertStatusCode(400));
  }

  @Test
  public void postBody_indexedParams_exceedsLimit() throws Exception {
    // Expansion past collection size fails
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withParameter("namesArray[0]", "foo")
        .withParameter("namesArray[10]", "bar")
        .post()
        .assertStatusCode(400));
  }

  @Test
  public void negativeIndex() throws Exception {
    // Invalid index should not work
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesArray[-1]", "bad")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void indexedOnListField_negativeIndex() throws Exception {
    // Invalid index should not work, 400
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesList[-1]", "bad")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void mixedIndexedAndRepeated() throws Exception {
    // Some params indexed, some comma-delimited — the ids value expands beyond the limit
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("namesArray[0]", "first")
        .withURLParameter("ids", "1,2,3,4,5,6,7,8,9,10,11")
        .get()
        .assertStatusCode(400));
  }

  @Test
  public void commaDelimited_trailingComma_atLimit_shouldSucceed() throws Exception {
    // 10 values with a trailing comma — the trailing comma should be ignored, not counted as an 11th element.
    // The old splitLength() trimmed trailing delimiters before counting, so this was exactly at the limit (10).
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("ids", "1,2,3,4,5,6,7,8,9,10,")
        .get()
        .assertStatusCode(200));
  }

  @Test
  public void commaDelimited_leadingComma_atLimit_shouldSucceed() throws Exception {
    // Leading comma produces an empty string element that counts toward the limit. splitLength counts it correctly
    // (same as split(",").length). 1 empty + 9 values = 10 elements, exactly at the limit.
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("ids", ",1,2,3,4,5,6,7,8,9")
        .get()
        .assertStatusCode(200));
  }

  @Test
  public void commaDelimited_leadingAndTrailingComma_atLimit_shouldSucceed() throws Exception {
    // Leading comma counts as an element, trailing comma does not. splitLength trims trailing delimiters but
    // preserves leading ones. 1 empty + 9 values = 10 elements, exactly at the limit.
    test.simulate(() -> simulator.test("/collection-size-limit")
        .withURLParameter("ids", ",1,2,3,4,5,6,7,8,9,")
        .get()
        .assertStatusCode(200));
  }
}

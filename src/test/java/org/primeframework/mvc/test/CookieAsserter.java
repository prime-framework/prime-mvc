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
package org.primeframework.mvc.test;

import java.util.function.Consumer;

import io.fusionauth.http.Cookie;
import io.fusionauth.http.Cookie.SameSite;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Daniel DeGroff
 */
public class CookieAsserter {
  public Cookie actual;

  public CookieAsserter(Cookie actual) {
    this.actual = actual;
  }

  public CookieAsserter assertHTTPOnly(boolean expected) {
    assertEquals(actual.httpOnly, expected);
    return this;
  }

  public CookieAsserter assertMaxAge(Long maxAge) {
    assertEquals(actual.maxAge, maxAge);
    return this;
  }

  public CookieAsserter assertMaxAgeNotEqualTo(Long maxAge) {
    assertNotEquals(actual.maxAge, maxAge);
    return this;
  }

  public CookieAsserter assertPath(String expected) {
    assertEquals(actual.path, expected);
    return this;
  }

  public CookieAsserter assertSameSite(SameSite expected) {
    assertEquals(actual.sameSite, expected);
    return this;
  }

  public CookieAsserter assertSecure(boolean expected) {
    assertEquals(actual.secure, expected);
    return this;
  }

  public CookieAsserter assertValue(String expected) {
    assertEquals(actual.value, expected);
    return this;
  }

  public CookieAsserter assertValueNotEmpty() {
    assertTrue(actual.value != null && actual.value.trim().length() > 0);
    return this;
  }

  public CookieAsserter custom(Consumer<Cookie> consumer) {
    consumer.accept(actual);
    return this;
  }
}

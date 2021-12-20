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
package org.primeframework.mvc;

import java.util.Locale;

import org.testng.annotations.Test;

/**
 * @author Brian Pontarelli
 */
@Test
public class LocaleActionTest extends PrimeBaseTest {
  @Test
  public void getFromBrowser() {
    simulator.test("/locale")
             .withHeader("Accept-Language", Locale.CHINESE.toString())
             .get()
             .assertBodyContains("zh");
  }

  @Test
  public void getFromCookie() {
    // The cookie overrides the header
    simulator.test("/locale")
             .withHeader("Accept-Language", Locale.CHINESE.toString())
             .withCookie("prime-locale", "fr")
             .get()
             .assertBodyContains("fr");
  }

  @Test
  public void set() {
    simulator.test("/locale")
             .withParameter("locale", "zh")
             .post()
             .assertBodyContains("zh")
             .assertCookie("prime-locale", "zh");
  }

  @Test
  public void setDeleteCookie() {
    simulator.test("/locale")
             .post()
             .assertBodyContains("empty")
             .assertCookieWasDeleted("prime-locale");
  }
}

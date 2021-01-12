/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
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
package org.apache.commons.lang3;

import java.util.Locale;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author Daniel DeGroff
 */
public class LocaleUtilsTest {

  private static void assertValidToLocale(final String localeString, final String language, final String country) {
    final Locale locale = LocaleUtils.toLocale(localeString);
    assertNotNull("valid locale", locale);
    assertEquals(language, locale.getLanguage());
    assertEquals(country, locale.getCountry());
    //variant is empty
    assertTrue(locale.getVariant() == null || locale.getVariant().isEmpty());
  }

  // See https://github.com/FusionAuth/fusionauth-issues/issues/978
  // See https://issues.apache.org/jira/browse/LANG-1312
  // See https://github.com/apache/commons-lang/pull/239/files
  @Test
  public void testLanguageAndUNM49Numeric3AreaCodeLang1312() {
    assertValidToLocale("en_001", "en", "001");
    assertValidToLocale("en_150", "en", "150");
    assertValidToLocale("ar_001", "ar", "001");
    assertValidToLocale("es_419", "es", "419");
  }
}

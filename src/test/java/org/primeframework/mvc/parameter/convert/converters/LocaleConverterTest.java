/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter.convert.converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.primeframework.mvc.parameter.convert.GlobalConverter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * This tests the locale converter.
 *
 * @author Brian Pontarelli
 */
public class LocaleConverterTest {
  @Test(dataProvider = "localeData")
  public void allLocalesFromBCP47Strings(Locale locale) {
    GlobalConverter converter = new LocaleConverter();
    Locale localeOut = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray(locale.toLanguageTag()));
    assertEquals(locale.getLanguage(), localeOut.getLanguage());
    assertEquals(locale.getCountry(), localeOut.getCountry());
    assertEquals(locale.getVariant(), localeOut.getVariant());
  }

  @Test(dataProvider = "localeData")
  public void allLocalesFromStrings(Locale locale) {
    GlobalConverter converter = new LocaleConverter();
    Locale localeOut = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray(locale.toString()));
    assertEquals(locale.getLanguage(), localeOut.getLanguage());
    assertEquals(locale.getCountry(), localeOut.getCountry());
    assertEquals(locale.getVariant(), localeOut.getVariant());
  }

  @Test
  public void fromStrings() {
    GlobalConverter converter = new LocaleConverter();
    Locale locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray((String) null));
    assertNull(locale);

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray(""));
    assertEquals(locale.getLanguage(), "");

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray("en"));
    assertEquals(locale.getLanguage(), "en");

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray("en_US"));
    assertEquals(locale.getLanguage(), "en");
    assertEquals(locale.getCountry(), "US");

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray("en", "US"));
    assertEquals(locale.getLanguage(), "en");
    assertEquals(locale.getCountry(), "US");

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray("en_US_UTF8"));
    assertEquals(locale.getLanguage(), "en");
    assertEquals(locale.getCountry(), "US");
    assertEquals(locale.getVariant(), "UTF8");

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray("en", "US", "UTF8"));
    assertEquals(locale.getLanguage(), "en");
    assertEquals(locale.getCountry(), "US");
    assertEquals(locale.getVariant(), "UTF8");

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray("es", "419", "UTF8"));
    assertEquals(locale.getLanguage(), "es");
    assertEquals(locale.getCountry(), "419");
    assertEquals(locale.getVariant(), "UTF8");

    locale = (Locale) converter.convertFromStrings(Locale.class, null, "testExpr", ArrayUtils.toArray("es", "AR", "UTF8"));
    assertEquals(locale.getLanguage(), "es");
    assertEquals(locale.getCountry(), "AR");
    assertEquals(locale.getVariant(), "UTF8");
  }

  @DataProvider(name = "localeData")
  public Object[] localeData() {
    List<Locale> locales = new ArrayList<>();
    Collections.addAll(locales, Locale.getAvailableLocales());
    locales.removeIf((locale) -> locale.getLanguage().isEmpty() || locale.hasExtensions() || !locale.getScript().isEmpty() || locale.toString().equals("no_NO_NY"));

    return locales.toArray();
  }

  @Test
  public void toStrings() {
    GlobalConverter converter = new LocaleConverter();
    String str = converter.convertToString(Locale.class, null, "testExpr", null);
    assertNull(str);

    str = converter.convertToString(Locale.class, null, "testExpr", Locale.US);
    assertEquals(str, "en_US");
  }
}

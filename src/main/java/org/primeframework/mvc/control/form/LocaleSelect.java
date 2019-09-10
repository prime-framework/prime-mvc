/*
 * Copyright (c) 2015, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.control.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.inject.Inject;
import org.apache.commons.lang3.LocaleUtils;
import org.primeframework.mvc.control.annotation.ControlAttribute;
import org.primeframework.mvc.control.annotation.ControlAttributes;

/**
 * This class is the control for a Locale select box.
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
    required = {
        @ControlAttribute(name = "name")
    },
    optional = {
        @ControlAttribute(name = "disabled", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "includeCountries", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "multiple", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "preferredLocales"),
        @ControlAttribute(name = "readonly", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "required", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "size", types = {int.class, Number.class}),
        @ControlAttribute(name = "tabindex", types = {int.class, Number.class})
    }
)
public class LocaleSelect extends Select {
  @Inject
  public LocaleSelect() {
  }

  /**
   * Adds the countries Map and then calls super.
   */
  @Override
  protected Map<String, Object> makeParameters() {
    LinkedHashMap<String, String> locales = new LinkedHashMap<>();
    String preferred = (String) attributes.get("preferredLocales");
    if (preferred != null) {
      String[] parts = preferred.split(",");
      for (String part : parts) {
        Locale locale = LocaleUtils.toLocale(part);
        locales.put(locale.toString(), locale.getDisplayName(locale));
      }
    }

    boolean includeCountries = attributes.containsKey("includeCountries") ? (Boolean) attributes.get("includeCountries") : true;
    List<Locale> allLocales = new ArrayList<>();
    Collections.addAll(allLocales, Locale.getAvailableLocales());
    allLocales.removeIf((locale) -> locale.getLanguage().isEmpty() || locale.hasExtensions() || !locale.getScript().isEmpty() ||
        !locale.getVariant().isEmpty() || (!includeCountries && !locale.getCountry().isEmpty()));
    allLocales.sort(Comparator.comparing(one -> one.getDisplayName(localeProvider.get())));

    for (Locale locale : allLocales) {
      if (!locales.containsKey(locale.getCountry())) {
        locales.put(locale.toString(), locale.getDisplayName(localeProvider.get()));
      }
    }

    attributes.put("items", locales);

    return super.makeParameters();
  }
}
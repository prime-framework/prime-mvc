/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.mvc.result.form.control;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jcatapult.mvc.result.control.annotation.ControlAttributes;
import org.jcatapult.mvc.result.control.annotation.ControlAttribute;

import static net.java.lang.StringTools.*;

/**
 * <p>
 * This class is the control for a select box.
 * </p>
 *
 * @author Brian Pontarelli
 */
@ControlAttributes(
    required = {
        @ControlAttribute(name = "name")
    },
    optional = {
        @ControlAttribute(name = "disabled", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "multiple", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "readonly", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "required", types = {boolean.class, Boolean.class}),
        @ControlAttribute(name = "size", types = {int.class, Integer.class}),
        @ControlAttribute(name = "tabindex", types = {int.class, Integer.class}),
        @ControlAttribute(name = "includeBlank", types = {boolean.class, Boolean.class})
    }
)
public class CountriesSelect extends Select {
    /**
     * <p>
     * Adds the countries Map and then calls super.
     * </p>
     *
     */
    @Override
    protected Map<String, Object> makeParameters() {
        LinkedHashMap<String, String> countries = new LinkedHashMap<String, String>();

        if (attributes.containsKey("includeBlank") && (Boolean) attributes.get("includeBlank")) {
            countries.put("", "");
        }

        String preferred = (String) attributes.get("preferredCodes");
        if (preferred != null) {
            String[] parts = preferred.split(",");
            for (String part : parts) {
                Locale locale = new Locale("", part);
                countries.put(part, locale.getDisplayCountry(locale));
            }
        }

        SortedSet<Locale> alphabetical = new TreeSet<Locale>(new LocaleComparator(locale));
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            if (!isTrimmedEmpty(locale.getCountry()) && !isTrimmedEmpty(locale.getDisplayCountry(locale))) {
                alphabetical.add(locale);
            }
        }

        for (Locale locale : alphabetical) {
            if (!countries.containsKey(locale.getCountry())) {
                countries.put(locale.getCountry(), locale.getDisplayCountry(this.locale));
            }
        }

        attributes.put("items", countries);

        return super.makeParameters();
    }

    public static class LocaleComparator implements Comparator<Locale> {
        private Locale displayLocale;

        public LocaleComparator(Locale displayLocale) {
            this.displayLocale = displayLocale;
        }

        public int compare(Locale l1, Locale l2) {
            return l1.getDisplayCountry(displayLocale).compareTo(l2.getDisplayCountry(displayLocale));
        }
    }
}
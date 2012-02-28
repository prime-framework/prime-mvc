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
package org.primeframework.locale;

import java.util.Locale;

import com.google.inject.ImplementedBy;
import com.google.inject.Provider;

/**
 * <p>
 * This is a provider that handles Locale information. The user might
 * select a new Locale from the browser, the application might change the
 * Locale, or the Locale might already be persisted in the users session.
 * </p>
 *
 * @author  Brian Pontarelli
 */
@ImplementedBy(DefaultLocaleStore.class)
public interface LocaleStore extends Provider<Locale> {
    /**
     * Stores a new Locale.
     *
     * @param   locale The new Locale.
     */
    void set(Locale locale);
}
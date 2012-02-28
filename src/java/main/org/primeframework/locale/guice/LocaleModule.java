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
 *
 */
package org.primeframework.locale.guice;

import java.util.Locale;

import org.primeframework.locale.DefaultLocaleStore;
import org.primeframework.locale.annotation.CurrentLocale;

import com.google.inject.AbstractModule;

/**
 * <p>
 * This class is a Guice module for injecting the current Locale into
 * classes.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class LocaleModule extends AbstractModule {
    /**
     * Sets up the Locale so that it can be injected when the {@link CurrentLocale} annotation is
     * used. The injection is handled by the {@link DefaultLocaleStore} class.
     */
    protected void configure() {
        bind(Locale.class).annotatedWith(CurrentLocale.class).toProvider(DefaultLocaleStore.class);
    }
}
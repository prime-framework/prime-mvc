/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.locale.guice;

import com.google.inject.AbstractModule;
import org.primeframework.mvc.locale.DefaultLocaleProvider;
import org.primeframework.mvc.locale.LocaleProvider;

/**
 * This class is a Guice module for the Prime MVC Locale handling.
 *
 * @author Brian Pontarelli
 */
public class LocaleModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(LocaleProvider.class).to(DefaultLocaleProvider.class);
  }
}

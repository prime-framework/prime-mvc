/*
 * Copyright (c) 2019-2023, Inversoft Inc., All Rights Reserved
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
package org.example.action;

import java.util.Locale;

import com.google.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.locale.LocaleProvider;

/**
 * @author Brian Pontarelli
 */
@Action
public class LocaleAction {
  public final LocaleProvider localeProvider;

  public Locale locale;

  @Inject
  public LocaleAction(LocaleProvider localeProvider) {
    this.localeProvider = localeProvider;
  }

  public String get() {
    locale = localeProvider.get();
    return "input";
  }

  public String post() {
    localeProvider.set(locale);
    return "input";
  }
}

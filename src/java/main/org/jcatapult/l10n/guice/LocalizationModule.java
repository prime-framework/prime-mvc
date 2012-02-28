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
 *
 */
package org.jcatapult.l10n.guice;

import java.util.ResourceBundle;

import org.jcatapult.l10n.WebControl;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * <p>
 * This class sets up the localization classes.
 * </p>
 *
 * @author Brian Pontarelli
 */
public class LocalizationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ResourceBundle.Control.class).to(WebControl.class).in(Singleton.class);
    }
}
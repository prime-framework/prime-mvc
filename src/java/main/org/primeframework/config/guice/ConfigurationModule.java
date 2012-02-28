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
package org.primeframework.config.guice;

import org.apache.commons.configuration.Configuration;
import org.primeframework.config.EnvironmentAwareConfiguration;
import org.primeframework.servlet.ServletObjectsHolder;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * <p>
 * This is a Guice module that sets up the apache commons configuration, which
 * is deprecated at this point.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class ConfigurationModule extends AbstractModule {
    /**
     * If there is a ServletContext and if there is, it sets up the Apache Commons configuration.
     */
    @Override
    protected void configure() {
        if (ServletObjectsHolder.getServletContext() != null) {
            configureConfiguration();
        }
    }

    /**
     * Configures the Apache Commons {@link org.apache.commons.configuration.Configuration} for injection.
     * This is deprecated.
     */
    protected void configureConfiguration() {
        // Setup the configuration
        bind(Configuration.class).to(EnvironmentAwareConfiguration.class).in(Singleton.class);
    }
}
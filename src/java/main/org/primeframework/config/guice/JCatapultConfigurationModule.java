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
package org.primeframework.config.guice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import org.primeframework.guice.NamedAnnotation;

import com.google.inject.AbstractModule;
import com.google.inject.Key;

/**
 * <p>
 * This is a Guice module that is used by most of the framework to get
 * at the JCatapult configuration files (i.e. jcatapult.properties and
 * jcatapult-default.properties).
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class JCatapultConfigurationModule extends AbstractModule {
    private static final Logger logger = Logger.getLogger(JCatapultConfigurationModule.class.getName());

    /**
     * Configures the Guice injector for using the jcatapult configuration files.
     */
    @Override
    protected void configure() {
        configureProperties();
    }

    /**
     * Loads the default properties from <strong>jcatapult-default.properties</strong> and then loads
     * any custom properties from <strong>jcatapult.properties</strong>. This file doesn't have to
     * exist, but the default does.
     */
    protected void configureProperties() {
        // Load the default properties
        Properties defaultProperties = new Properties();
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "jcatapult-default.properties");
            if (is == null) {
                throw new IOException();
            }

            logger.fine("Loading default jcatapult properties");
            defaultProperties.load(is);
            logger.fine("Defaults are " + defaultProperties);
        } catch (IOException e) {
            logger.severe("Unable to find jcatapult-default.properties in classpath. This file must " +
                "exist in the classpath and is usually inside the jcatapult-core JAR file. It might " +
                "have been removed.");
            System.exit(1);
        }

        // Bind the JCatapult properties file and the default
        Properties properties = new Properties(defaultProperties);
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jcatapult.properties");
            if (is == null) {
                throw new IOException();
            }

            properties.load(is);
        } catch (IOException e) {
            logger.fine("Unable to find jcatapult.properties in classpath. All of the defaults will be used.");
        }

        Enumeration<?> keys = properties.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            bind(Key.get(String.class, new NamedAnnotation(key))).toInstance(properties.getProperty(key));
        }
    }
}
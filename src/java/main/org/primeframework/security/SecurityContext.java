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
package org.primeframework.security;

import org.primeframework.security.spi.SecurityContextProvider;

import com.google.inject.Inject;

/**
 * <p>
 * This class is a ThreadLocal holder that manages an SPI for getting the
 * security credentials.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class SecurityContext {
    protected static SecurityContextProvider provider;

    /**
     * Returns the current provider.
     *
     * @return The provider.
     */
    public static SecurityContextProvider getProvider() {
        return provider;
    }

    /**
     * Sets a new provider.
     *
     * @param   provider The new provider to use.
     */
    @Inject
    public static void setProvider(SecurityContextProvider provider) {
        SecurityContext.provider = provider;
    }

    /**
     * @return  The currently logged in user's name or some default. If the provider has not been
     *          setup, this method returns the String "anonymous". This is done so that applications
     *          that want to use interfaces such as auditable can do so without requiring an entire
     *          security framework in order to run correctly and to test.
     */
    public static String getCurrentUsername() {
        if (provider == null) {
            return "anonymous";
        }
        return provider.getCurrentUsername();
    }

    /**
     * @return  The currently logged in user object. If there is no user logged in or there is no
     *          security provider, this returns null.
     */
    public static Object getCurrentUser() {
        if (provider == null) {
            return null;
        }
        return provider.getCurrentUser();
    }
}
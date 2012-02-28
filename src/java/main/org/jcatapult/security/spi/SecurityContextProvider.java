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
package org.jcatapult.security.spi;

/**
 * <p>
 * This interface defines the SPI for the SecurityContext. This can be
 * implemented in order to handle fecthing of the Security credentials
 * from any type of store.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public interface SecurityContextProvider {
    /**
     * Returns the user name of the currently logged in user or some type of default.
     *
     * @return  The current user name or a default.
     */
    String getCurrentUsername();

    /**
     * @return  The currently logged in user object, if there is one. Otherwise, this must return null.
     */
    Object getCurrentUser();
}
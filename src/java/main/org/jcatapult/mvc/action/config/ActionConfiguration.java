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
package org.jcatapult.mvc.action.config;

/**
 * <p>
 * This interface defines the public API that describes an action
 * configuration.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public interface ActionConfiguration {
    /**
     * @return  The action class or null if the configuration is for an class-less action.
     */
    Class<?> actionClass();

    /**
     * @return  The URI that the action is mapped to.
     */
    String uri();

    /**
     * Determines if this configuration can handle the given URI. Configuration objects provide
     * additional handling for URI parameters and other cases and this method uses the full incoming
     * URI to determine if the configuration can handle it.
     *
     * @param   uri The full incoming URI.
     * @return  True if this configuration can handle the URI, false if not.
     */
    boolean canHandle(String uri);

    /**
     * @return  The URI parameter mapping pattern.
     */
    String uriParameterPattern();
}
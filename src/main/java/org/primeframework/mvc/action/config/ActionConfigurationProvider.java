/*
 * Copyright (c) 2001-2017, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.config;

import java.util.List;

import org.primeframework.mvc.action.ActionInvocation;

/**
 * This interface defines the mechanism used to load and cache action configuration. This should be flexible enough to
 * support new actions being added, actions being updated and actions being removed during development.
 *
 * @author Brian Pontarelli
 */
public interface ActionConfigurationProvider {
  /**
   * Return a list of mapped URIs from the action configuration.
   *
   * @return the list of mapped action URIs.
   */
  List<ActionConfiguration> getActionConfigurations();

  /**
   * Locates the action configuration for the given URI.
   *
   * @param uri The URI.
   * @return The action initial pieces of the ActionInvocation or null if there is no action associated with the URI.
   */
  ActionInvocation lookup(String uri);
}
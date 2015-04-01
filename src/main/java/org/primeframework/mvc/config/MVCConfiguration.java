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
package org.primeframework.mvc.config;

/**
 * The main configuration interface for Prime that outlines all of the configurable values for the framework. This
 * interface can easily be implemented to suit any needs in your application.
 *
 * @author Brian Pontarelli
 */
public interface MVCConfiguration {
  /**
   * @return The login URI that is used for the Prime MVC security handling. Defaults to {@code /login}
   */
  String loginURI();

  /**
   * @return The number of seconds to check for Freemarker template updates (max integer means never and 0 means
   *         always).
   */
  int templateCheckSeconds();

  /**
   * @return The number of seconds to check for message bundles updates (max integer means never and 0 means always).
   */
  int l10nReloadSeconds();

  /**
   * @return The types of files that are allowed to be uploaded.
   */
  String[] fileUploadAllowedTypes();

  /**
   * @return The maximum size for an uploaded file.
   */
  long fileUploadMaxSize();

  /**
   * @return Whether or not static resource loading is enabled.
   */
  boolean staticResourcesEnabled();

  /**
   * @return The static resource prefixes.
   */
  String[] staticResourcePrefixes();

  /**
   * @return Whether or not runtime exceptions should be re-thrown out of Prime MVC.
   */
  boolean propagateRuntimeExceptions();

  /**
   * @return Whether or not unknown parameters should be allowed or if they should throw an exception.
   */
  boolean allowUnknownParameters();

  /**
   * Set to true if actions should ignore empty parameters.  Defaults to false
   *
   * @return true or false
   */
  boolean ignoreEmptyParameters();

  /**
   * @return True if empty HTTP request parameters should be considered null values by the conversion system.
   */
  boolean emptyParametersAreNull();

  /**
   * @return the result code to use when exceptions get thrown.  Defaults to 'error'
   */
  String exceptionResultCode();
}
